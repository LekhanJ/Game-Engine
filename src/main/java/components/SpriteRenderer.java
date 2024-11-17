package components;

import jade.Component;

public class SpriteRenderer extends Component {

    private boolean firstTime = false;

    @Override
    public void start() {
        if (gameObject.getComponents(SpriteRenderer.class) != null) {
            System.out.println("Found Sprite Renderer!");
        }
    }

    @Override
    public void update(float dt) {
        if (!firstTime) {
            System.out.println("I am updating");
            firstTime = true;
        }
    }
}
