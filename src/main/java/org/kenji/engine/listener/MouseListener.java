package org.kenji.engine.listener;

import org.kenji.engine.Window;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public final class MouseListener {
    private static double scrollX, scrollY;
    private static double xPos, yPos, lastX, lastY;
    private static final boolean[] mouseButtonPressed = new boolean[3];
    private static boolean isDragging;

    private MouseListener() {
        scrollX = 0.0;
        scrollY = 0.0;
        xPos = 0.0;
        yPos = 0.0;
        lastX = 0.0;
        lastY = 0.0;
    }

    // Callback for cursor position
    public static void mousePosCallback(long window, double xpos, double ypos) {
        lastX = xPos;
        lastY = yPos;
        xPos = xpos;
        yPos = ypos;
        // If any of the mouse button is pressed and the cursor is moving then the mouse is dragging
        isDragging = mouseButtonPressed[0] || mouseButtonPressed[1] || mouseButtonPressed[2];
        Window.getEditorLayer().getGlfw().cursorPosCallback(window, xpos, ypos);
    }

    // Callback for mouse button. mods is modifiers, it represents when you press some other key while pressing mouse button
    public static void mouseButtonCallback(long window, int button, int action, int mods) {
        if (action == GLFW_PRESS && button < mouseButtonPressed.length) {
            mouseButtonPressed[button] = true;
        } else if (action == GLFW_RELEASE && button < mouseButtonPressed.length) {
            mouseButtonPressed[button] = false;
            isDragging = false;
        }
        Window.getEditorLayer().getGlfw().mouseButtonCallback(window, button, action, mods);
    }

    // Callback for mouse scroll
    public static void mouseScrollCallback(long window, double xOffset, double yOffset) {
        scrollX = xOffset;
        scrollY = yOffset;
        Window.getEditorLayer().getGlfw().scrollCallback(window, xOffset, yOffset);
    }

    // Reset per-frame mouse input after game loop finishes processing the frame
    public static void endFrame() {
        scrollX = 0;
        scrollY = 0;
        lastX = xPos;
        lastY = yPos;
    }

    // Getters
    public static float getX() {
        return (float)xPos;
    }

    public static float getY() {
        return (float)yPos;
    }

    public static float getDx() {
        return (float)(lastX - xPos);
    }

    public static float getDy() {
        return (float)(lastY - yPos);
    }

    public static float getScrollX() {
        return (float)scrollX;
    }

    public static float getScrollY() {
        return (float)scrollY;
    }

    public static boolean isDragging() {
        return isDragging;
    }

    public static boolean mouseButtonDown(int button) {
        if (button < mouseButtonPressed.length) {
            return mouseButtonPressed[button];
        } else {
            return false;
        }
    }
}
