package org.kenji.engine.renderer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class Shader {

    private int shaderProgramId;

    private String vertexSource;
    private String fragmentSource;
    private String filepath;

    public Shader(String filepath) {
        this.filepath = filepath;

        try {

            String source = new String(Files.readAllBytes(Paths.get(filepath)));
            String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");

            // Find the first pattern after #type 'pattern'
            int index = source.indexOf("#type") + 6;
            int eol = source.indexOf("\n", index);
            String firstPattern = source.substring(index, eol).trim();

            // Find the second pattern after #type 'pattern'
            index = source.indexOf("#type", eol) + 6;
            eol = source.indexOf("\n", index);
            String secondPattern = source.substring(index, eol).trim();

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
        // Activate our shader program so the GPU uses our vertex + fragment shaders
        glUseProgram(shaderProgramId);
    }

    public void detach() {
        // Deactivate the shader program (bind to 0 = no shader active)
        glUseProgram(0);
    }

    // Uploading values into uniform variables/locations

    public void uploadMat4f(String varName, Matrix4f mat4) {
        // Find the location (slot number) of the uniform variable by its name in the shader source
        // e.g. "uProjection" or "uView" — returns -1 if the name doesn't exist in the shader
        int varLocation = glGetUniformLocation(shaderProgramId, varName);

        // Allocate a Java-side float buffer with exactly 16 slots (a 4x4 matrix = 16 floats)
        FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);

        // Write the matrix values from the JOML Matrix4f object into the float buffer
        // JOML fills it in column-major order, which is exactly what OpenGL expects
        mat4.get(matBuffer);

        // Upload the buffer to the GPU, writing it into the uniform slot we found above
        // 'false' means the matrix should NOT be transposed — it's already in the right layout
        glUniformMatrix4fv(varLocation, false, matBuffer);
    }
}
