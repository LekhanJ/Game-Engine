package org.kenji.engine.renderer;

import org.joml.*;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class Shader {

    // The ID OpenGL gives us to refer to the fully linked shader program on the GPU
    private int shaderProgramId;

    // The raw GLSL source code strings extracted from the .glsl file
    private String vertexSource;
    private String fragmentSource;

    // Path to the .glsl file (kept for error messages)
    private String filepath;

    // Tracks whether this shader is currently active on the GPU — prevents redundant glUseProgram calls
    private boolean isBeingUsed;

    public Shader(String filepath) {
        this.filepath = filepath;

        try {
            // Read the entire .glsl file into one big string
            String source = new String(Files.readAllBytes(Paths.get(filepath)));

            // Split the file on the "#type" keyword — this gives us the individual shader blocks
            // e.g. "#type vertex" and "#type fragment" act as section dividers in our custom format
            String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");

            // ─── Find the first shader type label ────────────────────────────

            // Jump past the "#type" keyword (5 chars) + 1 space = index 6
            int index = source.indexOf("#type") + 6;

            // Find the end of that same line so we can extract just the type word
            int eol = source.indexOf("\n", index);

            // Extract the type word, e.g. "vertex" or "fragment"
            String firstPattern = source.substring(index, eol).trim();

            // ─── Find the second shader type label ────────────────────────────

            // Search for the next "#type" keyword starting from after the first line
            index = source.indexOf("#type", eol) + 6;
            eol = source.indexOf("\n", index);
            String secondPattern = source.substring(index, eol).trim();

            // ─── Assign each block to the correct source variable ─────────────

            // splitString[0] is everything before the first "#type" (usually empty)
            // splitString[1] is the block after the first "#type"
            // splitString[2] is the block after the second "#type"

            if (firstPattern.equals("vertex")) {
                vertexSource = splitString[1];
            } else if (firstPattern.equals("fragment")) {
                fragmentSource = splitString[1];
            } else {
                throw new IOException("Unexpected token: " + firstPattern);
            }

            if (secondPattern.equals("vertex")) {
                vertexSource = splitString[2];
            } else if (secondPattern.equals("fragment")) {
                fragmentSource = splitString[2];
            } else {
                throw new IOException("Unexpected token: " + secondPattern);
            }

        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error: Could not open file for shader: " + filepath;
        }
    }

    public void compile() {

        int vertexId, fragmentId;

        // ─── STEP 1: Compile the vertex shader ───────────────────────────────

        // Ask OpenGL to create an empty shader of type VERTEX and give us its ID
        vertexId = glCreateShader(GL_VERTEX_SHADER);

        // Upload our vertex shader source code to the GPU
        glShaderSource(vertexId, vertexSource);

        // Tell the GPU to compile that source code
        glCompileShader(vertexId);

        // Ask OpenGL whether the compilation succeeded
        int success = glGetShaderi(vertexId, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            // Get how long the error message is so we can allocate the right buffer
            int len = glGetShaderi(vertexId, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: " + filepath + "\n\tVertex shader compilation failed.");
            // Print the actual error message from the GPU driver
            System.out.println(glGetShaderInfoLog(vertexId, len));
            // Crash the program so the bug doesn't go unnoticed
            assert false : "";
        }

        // ─── STEP 2: Compile the fragment shader ─────────────────────────────

        // Create an empty fragment shader on the GPU and get its ID
        fragmentId = glCreateShader(GL_FRAGMENT_SHADER);

        // Upload the fragment shader source code to the GPU
        glShaderSource(fragmentId, fragmentSource);

        // Compile the fragment shader
        glCompileShader(fragmentId);

        // Check if the fragment shader compiled successfully
        success = glGetShaderi(fragmentId, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentId, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: " + filepath + "\n\tFragment shader compilation failed.");
            System.out.println(glGetShaderInfoLog(fragmentId, len));
            assert false : "";
        }

        // ─── STEP 3: Link both shaders into one shader program ────────────────

        // Create an empty shader program on the GPU (this will be the full pipeline)
        shaderProgramId = glCreateProgram();

        // Attach the compiled vertex shader to the program
        glAttachShader(shaderProgramId, vertexId);

        // Attach the compiled fragment shader to the program
        glAttachShader(shaderProgramId, fragmentId);

        // Link them together — this wires the vertex output into the fragment input
        glLinkProgram(shaderProgramId);

        // Check if linking succeeded
        success = glGetProgrami(shaderProgramId, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(shaderProgramId, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: " + filepath + "\n\tShader program linking failed.");
            System.out.println(glGetProgramInfoLog(shaderProgramId, len));
            assert false : "";
        }
    }

    public void use() {
        // Only activate if not already active — avoids unnecessary GPU state changes
        if (!isBeingUsed) {
            // Activate our shader program so the GPU uses our vertex + fragment shaders
            glUseProgram(shaderProgramId);
            isBeingUsed = true;
        }
    }

    public void detach() {
        // Deactivate the shader program (bind to 0 = no shader active)
        glUseProgram(0);
        isBeingUsed = false;
    }

    // ─── Uniform Upload Methods ───────────────────────────────────────────────
    // Uniforms are variables declared in the shader (e.g. "uniform mat4 uProjection")
    // These methods let us write values into them from Java each frame

    public void uploadMat4f(String varName, Matrix4f mat4f) {
        // Find the location (slot number) of the uniform variable by its name in the shader source
        // e.g. "uProjection" or "uView" — returns -1 if the name doesn't exist in the shader
        int varLocation = glGetUniformLocation(shaderProgramId, varName);

        // The shader must be active before we can write to its uniforms
        use();

        // Allocate a Java-side float buffer with exactly 16 slots (a 4x4 matrix = 16 floats)
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);

        // Write the matrix values from the JOML Matrix4f object into the float buffer
        // JOML fills it in column-major order, which is exactly what OpenGL expects
        mat4f.get(matBuffer);

        // Upload the buffer to the GPU uniform slot
        // 'false' = do NOT transpose — JOML already outputs column-major which OpenGL wants
        glUniformMatrix4fv(varLocation, false, matBuffer);
    }

    public void uploadMat3f(String varName, Matrix3f mat3f) {
        // Find the uniform slot by name in the shader
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();

        // Allocate a float buffer for a 3x3 matrix (9 floats)
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);

        // Write the JOML Matrix3f values into the buffer in column-major order
        mat3f.get(matBuffer);

        // Upload the 3x3 matrix to the GPU
        glUniformMatrix3fv(varLocation, false, matBuffer);
    }

    public void uploadVec4f(String varName, Vector4f vec4f) {
        // Find the uniform slot by name in the shader
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();

        // Upload all 4 components (x, y, z, w) of the vector directly — no buffer needed for vectors
        glUniform4f(varLocation, vec4f.x, vec4f.y, vec4f.z, vec4f.w);
    }

    public void uploadVec3f(String varName, Vector3f vec3f) {
        // Find the uniform slot by name in the shader
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();

        // Upload all 3 components (x, y, z) of the vector
        glUniform3f(varLocation, vec3f.x, vec3f.y, vec3f.z);
    }

    public void uploadVec2f(String varName, Vector2f vec2f) {
        // Find the uniform slot by name in the shader
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();

        // Upload both components (x, y) of the vector
        glUniform2f(varLocation, vec2f.x, vec2f.y);
    }

    public void uploadFloat(String varName, float val) {
        // Find the uniform slot by name in the shader
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();

        // Upload a single float value
        glUniform1f(varLocation, val);
    }

    public void uploadInt(String varName, int val) {
        // Find the uniform slot by name in the shader
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();

        // Upload a single integer value
        glUniform1i(varLocation, val);
    }

    public void uploadTexture(String varName, int slot) {
        // Find the uniform slot by name in the shader
        int varLocation = glGetUniformLocation(shaderProgramId, varName);
        use();

        // Tell the shader which texture slot (0–31) to sample from
        // This matches the slot number passed to glActiveTexture(GL_TEXTURE0 + slot)
        glUniform1i(varLocation, slot);
    }
}