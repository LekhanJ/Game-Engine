package org.kenji.engine.scenemanager;

import org.kenji.engine.gameobject.Camera;
import org.kenji.engine.gameobject.GameObject;
import org.kenji.engine.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

/*
    Scene is the base class for all scenes in the engine (e.g. LevelEditorScene, GameScene).
    A scene owns all the GameObjects currently active, tracks the camera,
    and defines the game loop hooks: init(), start(), and update().

    The flow for a scene is:
        1. init()  — set up shaders, textures, camera, geometry
        2. start() — call start() on all GameObjects (and their components)
        3. update(dt) — called every frame to update all logic and render everything
*/
public abstract class Scene {

    // The renderer used to render everything in the scene
    protected Renderer renderer = new Renderer();

    // The camera used to view this scene
    protected Camera camera;

    // Tracks whether the scene has been started
    private boolean isRunning = false;

    // All GameObjects that belong to this scene — updated and rendered each frame
    protected List<GameObject> gameObjects = new ArrayList<>();

    public Scene() {

    }

    // Override in subclasses to set up the scene's camera, shaders, textures, and geometry
    public void init() {

    }

    /*
        Called by the Window class after init() finishes.
        Calls start() on every registered GameObject, which in turn
        calls start() on each of their components.
        Once this is done the scene is marked as running.
    */
    public void start() {
        for (GameObject go : gameObjects) {
            go.start(); // Triggers start() on all components of each GameObject
            this.renderer.add(go);
        }
        isRunning = true; // Mark the scene as live so new objects get started immediately
    }

    /*
        Registers a GameObject with this scene so it gets updated and rendered.

        If the scene hasn't started yet, just add it to the list —
        it will get start() called when the scene starts.

        If the scene is already running, add it AND immediately call start() on it
        so it isn't skipped for its first frame.
    */
    public void addGameObjectToScene(GameObject go) {
        if (!isRunning) {
            // Scene not started yet — just queue it up
            gameObjects.add(go);
        } else {
            // Scene already running — add it and start it right away
            gameObjects.add(go);
            go.start();
            this.renderer.add(go);
        }
    }

    // Called every frame by the game loop — subclasses must implement their own update logic
    public abstract void update(float dt);

    // Get the camera of the currently running scene
    public Camera camera() {
        return this.camera;
    }
}