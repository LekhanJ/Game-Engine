package org.kenji.engine.scenemanager;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LevelEditorScene extends Scene {

    // The source code of the vertex shader — runs once per vertex on the GPU
    private String vertexShaderSrc = "#version 330 core\n" +
            "layout (location=0) in vec3 aPos;\n" +   // Input: vertex position (x, y, z) from slot 0
            "layout (location=1) in vec4 aColor;\n" + // Input: vertex color (r, g, b, a) from slot 1
            "\n" +
            "out vec4 fColor;\n" +                    // Output: pass the color to the fragment shader
            "\n" +
            "void main() {\n" +
            "    fColor = aColor;\n" +                // Forward the color to the next shader stage
            "    gl_Position = vec4(aPos, 1.0);\n" +  // Set the final screen position of this vertex
            "}";

    // The source code of the fragment shader — runs once per pixel on the GPU
    private String fragmentShaderSrc = "#version 460 core\n" +
            "\n" +
            "in vec4 fColor;\n" +      // Input: color received from the vertex shader
            "\n" +
            "out vec4 color;\n" +      // Output: the final color written to the screen
            "\n" +
            "void main() {\n" +
            "    color = fColor;\n" +  // Just paint this pixel with the interpolated vertex color
            "}";

    // IDs that OpenGL uses to track the compiled shaders and the linked shader program
    private int vertexId, fragmentId, shaderProgram;

    // The raw vertex data: each row = one vertex, storing [x, y, z, r, g, b, a]
    private float[] vertexArray = {
            // Position              // Color (r, g, b, a)
            0.5f, -0.5f, 0.0f,       1.0f, 0.0f, 0.0f, 0.0f,  // Bottom right  0
            -0.5f, 0.5f, 0.0f,       0.0f, 1.0f, 0.0f, 0.0f,  // Top left      1
            0.5f, 0.5f, 0.0f,        0.0f, 0.0f, 1.0f, 0.0f,  // Top right     2
            -0.5f, -0.5f, 0.0f,      1.0f, 1.0f, 0.0f, 0.0f,  // Bottom left   3
    };

    // Indices telling OpenGL which 3 vertices form each triangle (counter-clockwise order)
    private int[] elementArray = {
            2, 1, 0,  // Top right triangle
            0, 1, 3,  // Bottom left triangle
    };

    // OpenGL object IDs for the VAO, VBO, and EBO (explained below where they're created)
    private int vaoId, vboId, eboId;

    public LevelEditorScene() {

    }

    @Override
    public void init() {

        // ─── STEP 1: Compile the vertex shader ───────────────────────────────

        // Ask OpenGL to create an empty shader of type VERTEX and give us its ID
        vertexId = glCreateShader(GL_VERTEX_SHADER);

        // Upload our vertex shader source code to the GPU
        glShaderSource(vertexId, vertexShaderSrc);

        // Tell the GPU to compile that source code
        glCompileShader(vertexId);

        // Ask OpenGL whether the compilation succeeded
        int success = glGetShaderi(vertexId, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            // Get how long the error message is so we can allocate the right buffer
            int len = glGetShaderi(vertexId, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: defaultShader.glsl\n\tVertex shader compilation failed.");
            // Print the actual error message from the GPU driver
            System.out.println(glGetShaderInfoLog(vertexId, len));
            // Crash the program so the bug doesn't go unnoticed
            assert false : "";
        }

        // ─── STEP 2: Compile the fragment shader ─────────────────────────────

        // Create an empty fragment shader on the GPU and get its ID
        fragmentId = glCreateShader(GL_FRAGMENT_SHADER);

        // Upload the fragment shader source code to the GPU
        glShaderSource(fragmentId, fragmentShaderSrc);

        // Compile the fragment shader
        glCompileShader(fragmentId);

        // Check if the fragment shader compiled successfully
        success = glGetShaderi(fragmentId, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentId, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: defaultShader.glsl\n\tFragment shader compilation failed.");
            System.out.println(glGetShaderInfoLog(fragmentId, len));
            assert false : "";
        }

        // ─── STEP 3: Link both shaders into one shader program ────────────────

        // Create an empty shader program on the GPU (this will be the full pipeline)
        shaderProgram = glCreateProgram();

        // Attach the compiled vertex shader to the program
        glAttachShader(shaderProgram, vertexId);

        // Attach the compiled fragment shader to the program
        glAttachShader(shaderProgram, fragmentId);

        // Link them together — this wires the vertex output into the fragment input
        glLinkProgram(shaderProgram);

        // Check if linking succeeded
        success = glGetProgrami(shaderProgram, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: defaultShader.glsl\n\tShader program linking failed.");
            System.out.println(glGetProgramInfoLog(shaderProgram, len));
            assert false : "";
        }

        // ─── STEP 4: Create the VAO, VBO, EBO and upload geometry ────────────

        /*
            VBO (Vertex Buffer Object) -> Holds raw data of your geometry like vertex positions, colors, normals, and texture coordinates.
            EBO (Element Buffer Object) -> Holds a list of indices. It tells OpenGL which vertices from your VBO to connect to form shapes, allowing you to reuse vertices and save memory.
            VAO (Vertex Array Object) -> Acts as a state tracker or preset wrapper. It remembers which VBOs and EBOs are connected and dictates how the shader program should read the data.
        */

        // Create the VAO and get its ID — everything below will be remembered by this VAO
        vaoId = glGenVertexArrays();

        // Bind (activate) the VAO so all subsequent VBO/EBO setup is recorded into it
        glBindVertexArray(vaoId);

        // Create a Java-side float buffer and fill it with our vertex data
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip(); // flip() resets position to 0 so OpenGL reads from the start

        // Create the VBO on the GPU and get its ID
        vboId = glGenBuffers();

        // Bind the VBO as the current array buffer (vertex data source)
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        // Upload the vertex float buffer to the GPU — GL_STATIC_DRAW means we won't change it often
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Create a Java-side int buffer and fill it with our index data
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip(); // flip() so OpenGL reads from position 0

        // Create the EBO on the GPU and get its ID
        eboId = glGenBuffers();

        // Bind the EBO as the current element (index) buffer
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);

        // Upload the index data to the GPU
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        // ─── STEP 5: Tell OpenGL how to read the vertex buffer ────────────────

        // Number of floats used for position (x, y, z)
        int positionsSize = 3;

        // Number of floats used for color (r, g, b, a)
        int colorSize = 4;

        // A single float takes 4 bytes in memory
        int floatSizeBytes = 4;

        // Total bytes for one full vertex: (3 position + 4 color) * 4 bytes = 28 bytes
        int vertexSizeBytes = (positionsSize + colorSize) * floatSizeBytes;

        // Tell OpenGL: attribute slot 0 = position, 3 floats, stride = full vertex size, starts at byte 0
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);

        // Enable attribute slot 0 so the shader can read the position data
        glEnableVertexAttribArray(0);

        // Tell OpenGL: attribute slot 1 = color, 4 floats, stride = full vertex size, starts after the 3 position floats
        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionsSize * floatSizeBytes);

        // BUG: This should be glEnableVertexAttribArray(1) — currently enabling slot 0 twice instead of enabling slot 1
        glEnableVertexAttribArray(0);
    }

    @Override
    public void update(float dt) {

        // Activate our shader program so the GPU uses our vertex + fragment shaders
        glUseProgram(shaderProgram);

        // Bind our VAO — this restores all the VBO/EBO layout settings we configured in init()
        glBindVertexArray(vaoId);

        // Enable the position attribute (slot 0) so the shader receives vertex positions
        glEnableVertexAttribArray(0);

        // Enable the color attribute (slot 1) so the shader receives vertex colors
        glEnableVertexAttribArray(1);

        // Draw the geometry using the EBO indices — draws 2 triangles = 1 quad
        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

        // Disable the attribute slots now that drawing is done
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        // Unbind the VAO (bind to 0 = no VAO active) — good practice to avoid accidental changes
        glBindVertexArray(0);

        // Deactivate the shader program (bind to 0 = no shader active)
        glUseProgram(0);
    }
}