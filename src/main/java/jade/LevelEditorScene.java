package jade;

import org.lwjgl.BufferUtils;
import renderer.Shader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glGenVertexArrays;

public class LevelEditorScene extends Scene {

    private String vertexShaderSrc = "#version 460 core\n" +
            "\n" +
            "layout (location=0) in vec3 aPos;\n" +
            "layout (location=1) in vec4 aColor;\n" +
            "\n" +
            "out vec4 fColor;\n" +
            "\n" +
            "void main() {\n" +
            "    fColor = aColor;\n" +
            "    gl_Position = vec4(aPos, 1.0);\n" +
            "}";

    private String fragmentShaderSrc = "#version 460 core\n" +
            "\n" +
            "in vec4 fColor;\n" +
            "\n" +
            "out vec4 color;\n" +
            "\n" +
            "void main() {\n" +
            "    color = fColor;\n" +
            "}";

    private int vertexID, fragmentID, shaderProgram;

    private float[] vertexArray = {
        // Position                // Color
        0.5f, -0.5f, 0.0f,         1.0f, 0.0f, 0.0f, 1.0f, // Bottom Right  0           1-----------2
        -0.5f, 0.5f, 0.0f,         0.0f, 1.0f, 0.0f, 1.0f, // Top Left      1           |           |
        0.5f, 0.5f, 0.0f,          0.0f, 0.0f, 1.0f, 1.0f, // Top Right     2           |           |
        -0.5f, -0.5f, 0.0f,        1.0f, 1.0f, 0.0f, 1.0f, // Bottom Left   3           3-----------0
    };

    // Must be in counter-clockwise order
    private int[] elementArray = {
        /*
            1-----------2
            |  \        |
            |     \     |
            |        \  |
            3-----------0
        */
        2, 1, 0, // Top right triangle
        0, 1, 3  // Bottom left triangle
    };

    private int VAO_ID, VBO_ID, EBO_ID;

    private Shader defaultShader;

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        defaultShader = new Shader("assets/shaders/default.glsl");
        // ------------------------------------------------------------
        // Compile and link shaders
        // ------------------------------------------------------------
        defaultShader.compileAndLink();

        // ---------------------------------------------------------------------
        // Generate VAO, VBO, and EBO buffer objects, and send to GPU
        // ---------------------------------------------------------------------

        // VBO (Vertex Buffer Object) holds the data of vertices, like their coordinates and color [Vx, Vy, Vz, r, g, b, ...]
        // EBO (Element Buffer Object) holds the data of how the vertices should be connected to each other like their order of forming lines in between [0, 1, 2, 0, 3, 1, ...]
        // VAO (Vertex Array Object) usd to bind VBO and EBO, it holds the data that helps GPU know how to use VBO and EBO together

        VAO_ID = glGenVertexArrays();
        glBindVertexArray(VAO_ID);

        // Create a float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        // Create VBO and upload the vertex buffer
        VBO_ID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO_ID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Create the indices and upload
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        EBO_ID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO_ID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        // Add the vertex attribute pointers
        int positionsSize = 3; // i.e, x, y, z
        int colorSize = 4; // i.e, r, g, b, a
        int floatSizeBytes = 4; // So there are 4 bytes for 1 float
        int vertexSizeBytes = (positionsSize + colorSize) * floatSizeBytes;
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionsSize * floatSizeBytes);
        glEnableVertexAttribArray(1);
    }

    @Override
    public void update(float dt) {
        // Use shader program
        defaultShader.use();
        // Bind the VAO that we are using
        glBindVertexArray(VAO_ID);

        // Enable the vertex attribute pointers
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

        // Unbind everything
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);

        defaultShader.detach();
    }
}
