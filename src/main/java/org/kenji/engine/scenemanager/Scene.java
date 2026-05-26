package org.kenji.engine.scenemanager;

import org.kenji.engine.gameobject.Camera;

public abstract class Scene {

    protected Camera camera;

    public Scene() {

    }

    public void init() {

    }

    public abstract void update(float dt);
}
