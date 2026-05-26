package org.kenji.engine.scenemanager;

import org.joml.Vector2f;
import org.kenji.engine.gameobject.Camera;
import org.kenji.engine.listener.KeyListener;
import org.kenji.engine.renderer.Shader;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LevelEditorScene extends Scene {

    // The raw vertex data: each row = one vertex, storing [x, y, z, r, g, b, a]
    private float[] vertexArray = {
            // Position                // Color (r, g, b, a)
            50.5f, -50.5f, 0.0f,       1.0f, 0.0f, 0.0f, 0.0f,  // Bottom right  0
            -50.5f, 50.5f, 0.0f,       0.0f, 1.0f, 0.0f, 0.0f,  // Top left      1
            50.5f, 50.5f, 0.0f,        0.0f, 0.0f, 1.0f, 0.0f,  // Top right     2
            -50.5f, -50.5f, 0.0f,      1.0f, 1.0f, 0.0f, 0.0f,  // Bottom left   3
    };

    // Indices telling OpenGL which 3 vertices form each triangle (counter-clockwise order)
    private int[] elementArray = {
            2, 1, 0,  // Top right triangle
            0, 1, 3,  // Bottom left triangle
    };

    // OpenGL object IDs for the VAO, VBO, and EBO (explained below where they're created)
    private int vaoId, vboId, eboId;

    private Shader defaultShader;

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        this.camera = new Camera(new Vector2f()); // Camera at 0,0 position

        // Select the shader file
        defaultShader = new Shader("assets/shaders/default.glsl");
        // Compile the shader
        defaultShader.compile();

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
        // Activate shader program
        defaultShader.use();

        // Upload the uniform values to the uniform variables in the default shader
        defaultShader.uploadMat4f("uProjection", camera.getProjectionMatrix());
        defaultShader.uploadMat4f("uView", camera.getViewMatrix());

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

        defaultShader.detach();
    }
}