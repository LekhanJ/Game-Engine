package org.kenji.engine.renderer;

import org.kenji.components.SpriteRenderer;
import org.kenji.engine.gameobject.GameObject;

import java.util.ArrayList;
import java.util.List;

/*
    VBO (Vertex Buffer Object) -> Holds raw data of your geometry like vertex positions, colors, normals, and texture coordinates.
    EBO (Element Buffer Object) -> Holds a list of indices. It tells OpenGL which vertices from your VBO to connect to form shapes, allowing you to reuse vertices and save memory.
    VAO (Vertex Array Object) -> Acts as a state tracker or preset wrapper. It remembers which VBOs and EBOs are connected and dictates how the shader program should read the data.
 */

public class Renderer {

    private final int MAX_BATCH_SIZE = 1000;
    private List<RenderBatch> batches;

    public Renderer() {
        this.batches = new ArrayList<>();
    }

    public void add(GameObject go) {
        SpriteRenderer sprite = go.getComponent(SpriteRenderer.class);
        if (sprite != null) {
            add(sprite);
        }
    }

    private void add(SpriteRenderer sprite) {
        boolean added = false;
        for (RenderBatch batch : batches) {
            if (batch.hasRoom()) {
                Texture texture = sprite.getTexture();
                if (texture == null || (batch.hasTexture(texture) || batch.hasTextureRoom())) {
                    batch.addSprite(sprite);
                    added = true;
                    break;
                }
            }
        }

        if (!added) {
            RenderBatch newBatch = new RenderBatch(MAX_BATCH_SIZE);
            newBatch.start();
            batches.add(newBatch);
            newBatch.addSprite(sprite);
        }
    }

    public void render() {
        for (RenderBatch batch : batches) {
            batch.render();
        }
    }
}
