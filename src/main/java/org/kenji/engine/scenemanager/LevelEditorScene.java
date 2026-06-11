package org.kenji.engine.scenemanager;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.kenji.components.Sprite;
import org.kenji.components.SpriteRenderer;
import org.kenji.components.Spritesheet;
import org.kenji.engine.gameobject.Camera;
import org.kenji.engine.gameobject.GameObject;
import org.kenji.engine.gameobject.Transform;
import org.kenji.util.AssetPool;

public class LevelEditorScene extends Scene {

    private GameObject mario;
    private Spritesheet sprites;

    public LevelEditorScene() {

    }

    @Override
    public void init() {

        loadResources();

        this.camera = new Camera(new Vector2f(-250, 0));

        sprites = AssetPool.getSpritesheet("assets/images/MarioSS.png");

        mario = new GameObject(
                "Mario",
                new Transform(
                        new Vector2f(100, 100),
                        new Vector2f(256, 256)
                )
        );
        mario.addComponent(
                new SpriteRenderer(sprites.getSprite(2))
        );
        this.addGameObjectToScene(mario);
    }

    private void loadResources() {
        AssetPool.getShader("assets/shaders/default.glsl");
        AssetPool.addSpritesheet("assets/images/MarioSS.png",
                new Spritesheet(
                        AssetPool.getTexture("assets/images/MarioSS.png"),
                        16, 16, 12, 14
                ));
    }

    private int spriteIndex = 0;
    private float spriteFlipTime = 0.2f;
    private float spriteFlipTimeLeft = 0.0f;

    @Override
    public void update(float dt) {
        spriteFlipTimeLeft -= dt;
        if (spriteFlipTimeLeft <= 0) {
            spriteFlipTimeLeft = spriteFlipTime;
            spriteIndex++;
            if (spriteIndex > 11) {
                spriteIndex = 0;
            }
            mario.getComponent(SpriteRenderer.class).setSprite(sprites.getSprite(spriteIndex));
        }

        for (GameObject go : this.gameObjects) {
            go.update(dt);
        }

        this.renderer.render();
    }
}