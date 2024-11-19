package jade;

import components.SpriteRenderer;
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
        this.gameObjects = new ArrayList<>();
        this.camera = new Camera(new Vector3f());
        this.renderer = new Renderer();

        GameObject mario = new GameObject("Mario", new Transform(new Vector2f(100, 100), new Vector2f(256, 256)));
        mario.addComponent(new SpriteRenderer(AssetPool.getTexture("assets/images/mario.png")));
        this.addGameObjectToScene(mario);

        GameObject goomba = new GameObject("Goomba", new Transform(new Vector2f(400, 100), new Vector2f(128, 128)));
        goomba.addComponent(new SpriteRenderer(AssetPool.getTexture("assets/images/goomba.png")));
        this.addGameObjectToScene(goomba);

        loadResources();
    }

    // Makes sure that we are creating the shader, compiling and linking it as well.
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
