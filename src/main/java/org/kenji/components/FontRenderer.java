package org.kenji.components;

import org.kenji.engine.gameobject.Component;

/*
    FontRenderer is responsible for rendering text for the GameObject it's attached to.
    Currently it checks whether a SpriteRenderer is also present on the same object —
    a pattern you'd use when one component depends on or works alongside another.
*/
public class FontRenderer extends Component {

    // Called once when the scene starts
    @Override
    public void start() {
        // Check if the same GameObject also has a SpriteRenderer attached.
        // This shows how components can communicate with each other via their shared gameObject reference.
        if (gameObject.getComponent(SpriteRenderer.class) != null) {
            // A SpriteRenderer exists on this object — FontRenderer can work alongside it
            System.out.println("Found Font Renderer");
        }
    }

    // Called every frame — text rendering logic will go here in the future
    @Override
    public void update(float dt) {

    }
}