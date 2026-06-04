package org.kenji.components;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.kenji.engine.gameobject.Component;
import org.kenji.engine.renderer.Texture;

/*
    SpriteRenderer is responsible for drawing a 2D image (sprite) for the GameObject it's attached to.
    Right now it just has placeholder print statements, but eventually this is where
    you'd handle things like which texture to draw, what color tint to apply, etc.
*/
public class SpriteRenderer extends Component {

    private Vector4f color;
    private Vector2f[] textCoordinates;
    private Texture texture;

    public SpriteRenderer(Vector4f color) {
        this.color = color;
        this.texture = null;
    }

    public SpriteRenderer(Texture texture) {
        this.texture = texture;
        this.color = new Vector4f(1, 1, 1, 1);
    }

    // Called once when the scene starts — good place to load sprite assets in the future
    @Override
    public void start() {

    }

    // Called every frame — this is where sprite drawing logic will eventually live
    @Override
    public void update(float dt) {

    }

    public Vector4f getColor() {
        return this.color;
    }

    public Texture getTexture() {
        return this.texture;
    }

    public Vector2f[] getTextCoordinates() {
        Vector2f[] texCoords = {
                new Vector2f(1, 1),
                new Vector2f(1, 0),
                new Vector2f(0, 0),
                new Vector2f(0, 1)
        };
        return texCoords;
    }
}