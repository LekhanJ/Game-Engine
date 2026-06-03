package org.kenji.engine.gameobject;

/*
    A Component is a reusable piece of behaviour that can be attached to any GameObject.
    Think of it like a Lego brick — a GameObject is the base, and components are the
    bricks you snap onto it to give it abilities (rendering, physics, audio, etc.)

    This is the base class — every component type (SpriteRenderer, FontRenderer, etc.)
    extends this and provides its own implementation of update().
*/
public abstract class Component {

    // A reference back to the GameObject this component is attached to.
    // Set automatically by GameObject.addComponent() so the component knows its owner.
    public GameObject gameObject = null;

    // Called once when the scene starts
    public void start() {}

    // Called every frame
    public abstract void update(float dt);
}