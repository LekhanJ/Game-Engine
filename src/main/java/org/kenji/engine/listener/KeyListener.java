package org.kenji.engine.listener;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyListener {
    private static KeyListener instance;
    private boolean keyPressed[] = new boolean[350];
    private boolean keyBeginPress[] = new boolean[350];

    private KeyListener() {

    }

    public static KeyListener get() {
        if (KeyListener.instance == null) {
            KeyListener.instance = new KeyListener();
        }
        return KeyListener.instance;
    }

    // Callback for key actions
    public static void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            if (!get().keyPressed[key]) {
                get().keyBeginPress[key] = true;
            }
            get().keyPressed[key] = true;
        } else if (action == GLFW_RELEASE) {
            get().keyPressed[key] = false;
        }
    }

    // Reset per-frame key input after game loop finishes processing the frame
    public static void endFrame() {
        for (int i = 0; i < get().keyBeginPress.length; i++) {
            get().keyBeginPress[i] = false;
        }
    }

    // Getters
    public static boolean isKeyPressed(int keyCode) {
        return get().keyPressed[keyCode];
    }

    public static boolean isKeyBeginPress(int keyCode) {
        return get().keyBeginPress[keyCode];
    }
}
