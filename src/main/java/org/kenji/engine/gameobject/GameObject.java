package org.kenji.engine.gameobject;

import java.util.ArrayList;
import java.util.List;

/*
    A GameObject is a container — it has a name and a list of components.
    By itself it does nothing; you give it behaviour by attaching components to it.

    This is the core building block of the engine's Entity-Component System (ECS).
    Instead of deep inheritance chains (Player extends Character extends Entity...),
    you compose behaviour by mixing and matching components.

    Example:
        GameObject player = new GameObject("Player");
        player.addComponent(new SpriteRenderer());
        player.addComponent(new RigidBody());
*/
public class GameObject {

    // A human-readable name for this object, useful for debugging
    private String name;

    // The list of all components currently attached to this GameObject
    private List<Component> components;

    public GameObject(String name) {
        this.name = name;
        // Start with an empty component list — components are added via addComponent()
        this.components = new ArrayList<>();
    }

    /*
        Finds and returns the first component on this GameObject that matches the given type.
        Uses generics so the returned value is already the correct type — no casting needed at the call site.

        Example usage:
            SpriteRenderer sr = myObj.getComponent(SpriteRenderer.class);

        Returns null if no matching component is found.
    */
    public <T extends Component> T getComponent(Class<T> componentClass) {
        for (Component c : components) {
            // isAssignableFrom checks if c is the same type as, or a subclass of, componentClass
            if (componentClass.isAssignableFrom(c.getClass())) {
                try {
                    // Safely cast the component to the requested type and return it
                    return componentClass.cast(c);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    assert false : "Error: Casting component";
                }
            }
        }
        // No component of that type was found on this GameObject
        return null;
    }

    /*
        Finds and removes the first component that matches the given type.
        Uses an index-based loop so we can safely remove while iterating.

        Example usage:
            myObj.removeComponent(SpriteRenderer.class);
    */
    public <T extends Component> void removeComponent(Class<T> componentClass) {
        for (int i = 0; i < components.size(); i++) {
            Component c = components.get(i);
            // Check if this component matches the type we want to remove
            if (componentClass.isAssignableFrom(c.getClass())) {
                components.remove(i); // Remove it from the list
                return;               // Stop after removing the first match
            }
        }
    }

    /*
        Attaches a component to this GameObject.
        Also sets the component's gameObject reference back to this,
        so the component knows which object it belongs to.
    */
    public void addComponent(Component c) {
        this.components.add(c);
        c.gameObject = this; // Give the component a reference back to its owner
    }

    /*
        Called every frame by the scene.
        Delegates to each component's update() so all behaviour runs each frame.
        dt = delta time passed down from the game loop.
    */
    public void update(float dt) {
        for (int i = 0; i < components.size(); i++) {
            components.get(i).update(dt);
        }
    }

    /*
        Called once when the scene starts.
        Delegates to each component's start() so they can all do their one-time setup.
    */
    public void start() {
        for (int i = 0; i < components.size(); i++) {
            components.get(i).start();
        }
    }
}