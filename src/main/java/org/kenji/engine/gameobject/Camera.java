package org.kenji.engine.gameobject;

/*
    So, the things we are working with is
    The View Matrix (a.k.a the Look-at Matrix) -> It tells where the camera is in relation to the world
    The Projection Matrix -> It defines the projection mode (Orthographic or Perspective) which tells us how big we want the screen space to be
    The aPos -> The position of all the objects on the screen

    So we do ->  Projection Matrix * View Matrix * aPos ->  Which converts world coordinates to normalized device coordinates
                 <-------- Multiplication order -------
*/

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {

    // The two matrices that together define how the world is seen through this camera
    private Matrix4f projectionMatrix, viewMatrix;

    // The camera's 2D position in the world (we use 2D since this is an orthographic/2D game)
    public Vector2f position;

    public Camera(Vector2f position) {
        // Store where the camera starts in the world
        this.position = position;

        // Allocate both matrices (they start as identity matrices — like a "do nothing" state)
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();

        // Set up the projection right away so the camera is ready to use
        adjustProjection();
    }

    // Sets up an Orthographic projection — meaning no perspective distortion,
    // objects stay the same size regardless of their depth (good for 2D games)
    public void adjustProjection() {
        // Reset the projection matrix to identity before applying any projection
        projectionMatrix.identity();

        // Define the visible area of the screen:
        // Left=0, Right=32*40=1280, Bottom=0, Top=32*21=672, Near=0, Far=100
        // This means 1 unit in world space = 1 pixel in a 1280x672 game world
        projectionMatrix.ortho(0.0f, 32.0f * 40.0f, 0.0f, 32.0f * 21.0f, 0.0f, 100.0f);
    }

    public Matrix4f getViewMatrix() {
        // The direction the camera is looking — straight into the screen (negative Z axis)
        Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);

        // "Up" for the camera — tells OpenGL which way is up so the image isn't upside down
        Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);

        // Reset the view matrix to identity before recalculating (avoids stacking transforms)
        this.viewMatrix.identity();

        // Build the view matrix using lookAt:
        // - Eye position: the camera sits at (position.x, position.y, 20) — slightly in front of the scene
        // - Target: where the camera looks at — cameraFront shifted by the camera's x/y position
        //           so the camera always looks straight ahead from wherever it is in the world
        // - Up vector: keeps the camera oriented correctly (Y axis points up)
        this.viewMatrix = viewMatrix.lookAt(
                new Vector3f(position.x, position.y, 20.0f),
                cameraFront.add(position.x, position.y, 0.0f),
                cameraUp
        );

        // Return the finished view matrix to be used in the shader
        return this.viewMatrix;
    }

    // Simple getter — returns the projection matrix so the shader can use it
    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }
}