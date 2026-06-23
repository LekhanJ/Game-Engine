package org.kenji.engine.renderer;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.kenji.components.SpriteRenderer;
import org.kenji.engine.Window;
import org.kenji.util.AssetPool;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class RenderBatch implements Comparable<RenderBatch> {

    // Vertex
    // Pos              Color                         Texture Coordinates     Texture Id
    // float, float,    float, float, float, float    float, float            float

    // Quad
    // int, int, int, int, int, int
    // <- 1st Tri ->  <- 2nd Tri ->
    // 1st Tri + 2nd Tri = 1 Quad

    private final int POSITION_SIZE = 2;
    private final int COLOR_SIZE = 4;
    private final int TEX_COORDS_SIZE = 2;
    private final int TEX_ID_SIZE = 1;

    private final int POSITION_OFFSET = 0;
    private final int COLOR_OFFSET = POSITION_OFFSET + POSITION_SIZE * Float.BYTES;
    private final int TEX_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
    private final int TEX_ID_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES;

    private final int VERTEX_SIZE = 9;
    private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    private SpriteRenderer[] sprites;
    private Shader shader;
    private List<Texture> textures;

    private int numSprites;
    private boolean hasRoom;
    private float[] vertices;
    private int[] texSlots = {0, 1, 2, 3, 4, 5, 6, 7};
    private int vaoId, vboId;
    private int maxBatchSize;
    private int zIndex;

    public RenderBatch(int maxBatchSize, int zIndex) {
        this.zIndex = zIndex;
        shader = AssetPool.getShader("assets/shaders/default.glsl");

        this.sprites = new SpriteRenderer[maxBatchSize];
        this.maxBatchSize = maxBatchSize;

        // 4 vertices quads
        vertices = new float[maxBatchSize * 4 * VERTEX_SIZE];

        this.numSprites = 0;
        this.hasRoom = true;

        this.textures = new ArrayList<>();
    }

    public void start() {
        // Generate and bind a Vertex Array Object
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Allocate space for vertices
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

        // Create and upload indices buffer
        int eboId = glGenBuffers();
        int[] indices = generateIndices();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Enable the buffer attribute pointers
        glVertexAttribPointer(0, POSITION_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POSITION_OFFSET);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ID_OFFSET);
        glEnableVertexAttribArray(3);
    }

    public void addSprite(SpriteRenderer sprite) {
        // Get index and add renderObject
        int index = this.numSprites;
        this.sprites[index] = sprite;
        this.numSprites++;

        if (sprite.getTexture() != null) {
            if (!textures.contains(sprite.getTexture())) {
                textures.add(sprite.getTexture());
            }
        }

        // Add properties to local vertices array
        loadVertexProperties(index);

        if (numSprites >= this.maxBatchSize) {
            this.hasRoom = false;
        }
    }

    private int[] generateIndices() {
        // 6 indices per quad (3 per triangle)
        int[] elements = new int[6 * maxBatchSize];
        for (int i=0; i < maxBatchSize; i++) {
            loadElementIndices(elements, i);
        }

        return elements;
    }

    private void loadElementIndices(int[] elements, int index) {
        int offsetArrayIndex = 6 * index;
        int offset = 4 * index;

        /*
            3---------------0       7---------------4
            |            /  |       |            /  |
            |       /       |       |       /       |
            |  /            |       |  /            |
            2---------------1       6---------------5
        */

        // Quad 1                   Quad 2
        // 3, 2, 0, 0, 2, 1         7, 6, 4, 4, 6, 5

        // Triangle 1
        elements[offsetArrayIndex] = offset + 3;
        elements[offsetArrayIndex + 1] = offset + 2;
        elements[offsetArrayIndex + 2] = offset;
        // Triangle 2
        elements[offsetArrayIndex + 3] = offset;
        elements[offsetArrayIndex + 4] = offset + 2;
        elements[offsetArrayIndex + 5] = offset + 1;
    }

    public void render() {
        boolean rebufferData = false;
        for (int i=0; i < numSprites; i++) {
            SpriteRenderer spr = sprites[i];
            if (spr.isDirty()){
                loadVertexProperties(i);
                spr.setClean();
                rebufferData = true;
            }
        }

        if (rebufferData) {
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        }

        // User shader
        shader.use();
        shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
        shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());

        // Bind all textures
        for (int i=0; i < textures.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + i + 1); // 0th slot reserve for color
            textures.get(i).bind();
        }

        shader.uploadIntArray("uTextures", texSlots);

        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, this.numSprites * 6, GL_UNSIGNED_INT, 0);

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        for (int i=0; i < textures.size(); i++) {
            textures.get(i).unbind();
        }
        shader.detach();
    }

    public void loadVertexProperties(int index) {
        SpriteRenderer sprite = this.sprites[index];

        // Find offset within array (4 vertices per sprite)
        int offset = index * 4 * VERTEX_SIZE;

        Vector4f color = sprite.getColor();
        Vector2f[] texCoords = sprite.getTextCoordinates();

        int texId = 0;
        if (sprite.getTexture() != null) {
            for (int i=0; i < textures.size(); i++) {
                if (textures.get(i) == sprite.getTexture()) {
                    texId = i + 1; // Oth slot reserve for color
                    break;
                }
            }
        }

        // Add vertices with the appropriate properties
        /*
            (0,1,r,g,b,a,ux,uy,id)                (1,1,r,g,b,a,ux,uy,id)
                                 +----------------+
                                 |                |
                                 |                |
                                 |                |
                                 +----------------+
            (0,0,r,g,b,a,ux,uy,id)                (1,0,r,g,b,a,ux,uy,id)
        */

        float xAdd = 1.0f;
        float yAdd = 1.0f;
        for (int i=0; i<4; i++) {
            switch (i) {
                case 1:
                    yAdd = 0.0f;
                    break;
                case 2:
                    xAdd = 0.0f;
                    break;
                case 3:
                    yAdd = 1.0f;
                    break;
                default:
                    break;
            }

            // Load position
            vertices[offset] = sprite.gameObject.transform.position.x +
                    (xAdd * sprite.gameObject.transform.scale.x);
            vertices[offset + 1] = sprite.gameObject.transform.position.y +
                    (yAdd * sprite.gameObject.transform.scale.y);

            // Load color
            vertices[offset + 2] = color.x;
            vertices[offset + 3] = color.y;
            vertices[offset + 4] = color.z;
            vertices[offset + 5] = color.w;

            // Load texture coordinates
            vertices[offset + 6] = texCoords[i].x;
            vertices[offset + 7] = texCoords[i].y;

                            // Load texture id
            vertices[offset + 8] = texId;

            offset += VERTEX_SIZE;
        }
    }

    public boolean hasRoom() {
        return this.hasRoom;
    }

    public boolean hasTextureRoom() {
        return this.textures.size() < 8;
    }

    public boolean hasTexture(Texture texture) {
        return this.textures.contains(texture);
    }

    public int zIndex() {
        return this.zIndex;
    }

    @Override
    public int compareTo(RenderBatch o) {
        return Integer.compare(this.zIndex, o.zIndex());
    }
}
