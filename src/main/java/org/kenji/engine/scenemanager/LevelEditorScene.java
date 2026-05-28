package org.kenji.engine.scenemanager;

import org.joml.Vector2f;
import org.kenji.engine.gameobject.Camera;
import org.kenji.engine.renderer.Shader;
import org.kenji.engine.renderer.Texture;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class LevelEditorScene extends Scene {

    // The raw vertex data sent to the GPU
    // Each vertex has 9 floats: [x, y, z,  r, g, b, a,  u, v]
    //   x, y, z = position in world space
    //   r, g, b, a = vertex color
    //   u, v = UV texture coordinates (0–1 range, maps to a point on the texture)
    private float[] vertexArray = {
            // Position               // Color (r, g, b, a)      // UV Coordinates
            50.5f, -50.5f, 0.0f,      1.0f, 0.0f, 0.0f, 0.0f,    1, 1,  // Bottom right  0
            -50.5f, 50.5f, 0.0f,      0.0f, 1.0f, 0.0f, 0.0f,    0, 0,  // Top left      1
            50.5f, 50.5f, 0.0f,       0.0f, 0.0f, 1.0f, 0.0f,    1, 0,  // Top right     2
            -50.5f, -50.5f, 0.0f,     1.0f, 1.0f, 0.0f, 0.0f,    0, 1   // Bottom left   3
    };

    // Indices telling OpenGL which 3 vertices form each triangle (counter-clockwise order)
    private int[] elementArray = {
            2, 1, 0,  // Top right triangle
            0, 1, 3,  // Bottom left triangle
    };

    // OpenGL object IDs for the VAO, VBO, and EBO
    private int vaoId, vboId, eboId;

    // The shader program that will process our vertices and color the pixels
    private Shader defaultShader;

    // The texture we will map onto the quad
    private Texture testTexture;

    public LevelEditorScene() {

    }

    @Override
    public void init() {

        // Create the camera starting at world position (0, 0)
        this.camera = new Camera(new Vector2f());

        // Offset the camera so the quad appears roughly centered on screen
        camera.position.x = -500.0f;
        camera.position.y = -500.0f;

        // Point to the combined .glsl shader file (contains both vertex and fragment source)
        defaultShader = new Shader("assets/shaders/default.glsl");

        // Parse and compile both shader stages from that file
        defaultShader.compile();

        // Load the texture image from disk and upload it to the GPU
        this.testTexture = new Texture("assets/images/mario.png");

        // ─── Create the VAO, VBO, EBO and upload geometry ────────────────────

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

        // ─── Tell OpenGL how to read each vertex from the buffer ──────────────

        // Number of floats for each attribute in one vertex
        int positionsSize = 3; // x, y, z
        int colorSize     = 4; // r, g, b, a
        int uvSize        = 2; // u, v

        // Total byte size of one full vertex: (3 + 4 + 2) * 4 bytes = 36 bytes
        // This "stride" tells OpenGL how many bytes to skip to get from one vertex to the next
        int vertexSizeBytes = (positionsSize + colorSize + uvSize) * Float.BYTES;

        // Attribute slot 0 = position: 3 floats, starting at byte 0 of each vertex
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0); // Enable so the shader can actually read it

        // Attribute slot 1 = color: 4 floats, starting after the 3 position floats
        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionsSize * Float.BYTES);
        glEnableVertexAttribArray(1); // Enable so the shader can actually read it

        // Attribute slot 2 = UV coords: 2 floats, starting after the 3 position + 4 color floats
        glVertexAttribPointer(2, uvSize, GL_FLOAT, false, vertexSizeBytes, (positionsSize + colorSize) * Float.BYTES);
        glEnableVertexAttribArray(2); // Enable so the shader can actually read it
    }

    @Override
    public void update(float dt) {

        // Activate our shader program so the GPU uses our vertex + fragment shaders
        defaultShader.use();

        // Tell the shader to sample from texture slot 0
        defaultShader.uploadTexture("TEX_SAMPLER", 0);

        // Make texture slot 0 the active slot on the GPU side
        glActiveTexture(GL_TEXTURE0);

        // Bind our texture into slot 0 so the shader can sample it
        testTexture.bind();

        // Upload the projection matrix — defines the visible area / coordinate space
        defaultShader.uploadMat4f("uProjection", camera.getProjectionMatrix());

        // Upload the view matrix — defines where the camera is looking from in the world
        defaultShader.uploadMat4f("uView", camera.getViewMatrix());

        // Bind our VAO — this restores all the VBO/EBO layout settings we configured in init()
        glBindVertexArray(vaoId);

        // Enable the position attribute (slot 0) so the shader receives vertex positions
        glEnableVertexAttribArray(0);

        // Enable the color attribute (slot 1) so the shader receives vertex colors
        glEnableVertexAttribArray(1);

        // Draw the geometry using the EBO indices — 6 indices = 2 triangles = 1 quad
        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

        // Disable the attribute slots now that drawing is done
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        // Unbind the VAO (0 = no VAO active) — good practice to avoid accidental changes
        glBindVertexArray(0);

        // Deactivate the shader program
        defaultShader.detach();
    }
}