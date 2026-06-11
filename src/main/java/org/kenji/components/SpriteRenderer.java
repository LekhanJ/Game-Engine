package org.kenji.components;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.kenji.engine.gameobject.Component;
import org.kenji.engine.gameobject.Transform;
import org.kenji.engine.renderer.Texture;

/*
    SpriteRenderer is responsible for drawing a 2D image (sprite) for the GameObject it's attached to.
    Right now it just has placeholder print statements, but eventually this is where
    you'd handle things like which texture to draw, what color tint to apply, etc.
*/
public class SpriteRenderer extends Component {

    private Vector4f color;
    private Sprite sprite;
    private Transform lastTransform;

    private boolean isDirty = false;

    public SpriteRenderer(Vector4f color) {
        this.color = color;
        this.sprite = new Sprite(null);
    }

    public SpriteRenderer(Sprite sprite) {
        this.sprite = sprite;
        this.color = new Vector4f(1, 1, 1, 1);
    }

    // Called once when the scene starts — good place to load sprite assets in the future
    @Override
    public void start() {
        this.lastTransform = gameObject.transform.copy();
    }

    // Called every frame — this is where sprite drawing logic will eventually live
    @Override
    public void update(float dt) {
        if (!this.lastTransform.equals(this.gameObject.transform)) {
            this.gameObject.transform.copy(this.lastTransform);
            isDirty = true;
        }
    }

    public Vector4f getColor() {
        return this.color;
    }

    public Texture getTexture() {
        return sprite.getTexture();
    }

    public Vector2f[] getTextCoordinates() {
        return sprite.texCoords();
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
        this.isDirty = true;
    }

    public void setColor(Vector4f color) {
        if (!this.color.equals(color)) {
            this.isDirty = true;
            this.color.set(color);
        }
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    public void setClean() {
        if (isDirty)
            this.isDirty = false;
    }
}