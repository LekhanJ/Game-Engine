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

    public LevelEditorScene() {

    }

    @Override
    public void init() {
        loadResources();

        this.camera = new Camera(new Vector3f());
        this.gameObjects = new ArrayList<>();
        this.renderer = new Renderer();

        SpriteSheet sprites = AssetPool.getSpriteSheet("assets/images/MarioSS.png");

        GameObject mario = new GameObject("Mario", new Transform(new Vector2f(100, 100), new Vector2f(256, 256)));
        mario.addComponent(new SpriteRenderer(sprites.getSprite(2)));
        this.addGameObjectToScene(mario);

        GameObject goomba = new GameObject("Goomba", new Transform(new Vector2f(400, 100), new Vector2f(128, 128)));
        goomba.addComponent(new SpriteRenderer(sprites.getSprite(5)));
        this.addGameObjectToScene(goomba);
    }

    // Makes sure that we are creating the shader, compiling and linking it as well.
    private void loadResources() {
        AssetPool.getShader("assets/shaders/default.glsl");
        AssetPool.addSpriteSheet(
                "assets/images/MarioSS.png",
                new SpriteSheet(AssetPool.getTexture("assets/images/MarioSS.png"), 17, 16, 14, 13)
        );
    }

    @Override
    public void update(float dt) {
        for (GameObject go : this.gameObjects) {
            go.update(dt);
        }

        this.renderer.render();
    }
}
