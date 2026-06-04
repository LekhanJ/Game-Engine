package org.kenji.engine.scenemanager;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.kenji.components.SpriteRenderer;
import org.kenji.engine.gameobject.Camera;
import org.kenji.engine.gameobject.GameObject;
import org.kenji.engine.gameobject.Transform;
import org.kenji.util.AssetPool;

public class LevelEditorScene extends Scene {

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        this.camera = new Camera(new Vector2f(-250, 0));

        GameObject mario = new GameObject(
                "Mario",
                new Transform(
                        new Vector2f(100, 100),
                        new Vector2f(256, 256)
                )
        );
        mario.addComponent(
                new SpriteRenderer(
                        AssetPool.getTexture("assets/images/mario.png")
                )
        );
        this.addGameObjectToScene(mario);

        loadResources();
    }

    private void loadResources() {
        AssetPool.getShader("assets/shaders/default.glsl");

    }

    @Override
    public void update(float dt) {
        for (GameObject go : this.gameObjects) {
            go.update(dt);
        }
        this.renderer.render();
    }
}