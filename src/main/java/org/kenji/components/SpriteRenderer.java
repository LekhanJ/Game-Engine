package org.kenji.components;

import org.kenji.engine.gameobject.Component;

/*
    SpriteRenderer is responsible for drawing a 2D image (sprite) for the GameObject it's attached to.
    Right now it just has placeholder print statements, but eventually this is where
    you'd handle things like which texture to draw, what color tint to apply, etc.
*/
public class SpriteRenderer extends Component {

    private boolean isFirstTime = false;

    // Called once when the scene starts — good place to load sprite assets in the future
    @Override
    public void start() {
        System.out.println("I am starting");
    }

    // Called every frame — this is where sprite drawing logic will eventually live
    @Override
    public void update(float dt) {
        if (!isFirstTime) {
            System.out.println("I am updating");
            isFirstTime = true;
        }
    }
}