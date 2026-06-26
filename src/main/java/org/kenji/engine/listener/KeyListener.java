package org.kenji.engine.listener;

import org.kenji.engine.Window;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public final class KeyListener {
    private static final boolean[] keyPressed = new boolean[350];
    private static final boolean[] keyBeginPress = new boolean[350];

    private KeyListener() {

    }

    // Callback for key actions
    public static void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW_PRESS) {
            if (!keyPressed[key]) {
                keyBeginPress[key] = true;
            }
            keyPressed[key] = true;
        } else if (action == GLFW_RELEASE) {
            keyPressed[key] = false;
        }
        Window.getEditorLayer().getGlfw().keyCallback(window, key, scancode, action, mods);
    }

    public static void charCallback(long window, int codepoint) {
        Window.getEditorLayer().getGlfw().charCallback(window, codepoint);
    }

    // Reset per-frame key input after game loop finishes processing the frame
    public static void endFrame() {
        Arrays.fill(keyBeginPress, false);
    }

    // Getters
    public static boolean isKeyPressed(int keyCode) {
        return keyPressed[keyCode];
    }

    public static boolean isKeyBeginPress(int keyCode) {
        return keyBeginPress[keyCode];
    }
}
