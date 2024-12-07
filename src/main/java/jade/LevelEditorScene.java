package jade;

import components.Sprite;
import components.SpriteRenderer;
import components.SpriteSheet;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import renderer.Renderer;
import util.AssetPool;

import java.util.ArrayList;

public class LevelEditorScene extends Scene {

    private GameObject mario;
    private SpriteSheet sprites;

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        loadResources();

        this.camera = new Camera(new Vector3f());
        this.gameObjects = new ArrayList<>();
        this.renderer = new Renderer();

        sprites = AssetPool.getSpriteSheet("assets/images/MarioSS.png");

        mario = new GameObject("Mario", new Transform(new Vector2f(100, 100), new Vector2f(100, 100)));
        mario.addComponent(new SpriteRenderer(sprites.getSprite(1)));
        this.addGameObjectToScene(mario);
    }

    // Makes sure that we are creating the shader, compiling and linking it as well.
    private void loadResources() {
        AssetPool.getShader("assets/shaders/default.glsl");
        AssetPool.addSpriteSheet(
                "assets/images/MarioSS.png",
                new SpriteSheet(AssetPool.getTexture("assets/images/MarioSS.png"), 17, 16, 14, 13)
        );
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
