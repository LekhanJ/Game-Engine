package org.kenji.engine.scenemanager;

import org.kenji.engine.Window;
import org.kenji.engine.listener.KeyListener;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;

public class LevelEditorScene extends Scene {

    private boolean isChanging = false;
    private float timeToChangeScene;

    public LevelEditorScene() {
        System.out.println("Level Editor Scene");
    }

    @Override
    public void update(float dt) {
        if (!isChanging && KeyListener.isKeyBeginPress(GLFW_KEY_SPACE)) {
            isChanging = true;
            timeToChangeScene = 10.0f;
        }

        if (isChanging) {
            timeToChangeScene -= dt;

            if (timeToChangeScene <= 0) {
                isChanging = false;
                Window.changeScene(1);
            }
        }
    }
}
