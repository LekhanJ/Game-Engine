package jade;

import java.util.ArrayList;
import java.util.List;
/*
    Lifecycle -->
    Is the scene running?
    if (yes) {
        1. Create all GameObjects and for each GameObject add all components
        2. Start all GameObjects and for each component, start the component
        3. Run the scene
    } else {
        1. Create the GameObject and add all its components
        2. Add the GameObject to scene and start the GameObject
    }
*/
public class GameObject {

    private String name;
    private List<Component> components;
    public Transform transform;
    private int zIndex;

    public GameObject(String name) {
        init(name, new Transform(), 0);
    }

    public GameObject(String name, Transform transform, int zIndex) {
        init(name, transform, zIndex);
    }

    public void init(String name, Transform transform, int zIndex) {
        this.name = name;
        this.components = new ArrayList<>();
        this.transform = transform;
        this.zIndex = zIndex;
    }

    public <T extends Component> T getComponent(Class<T> componentClass) {
        for (Component c : components) {
            if (componentClass.isAssignableFrom(c.getClass())) {
                try {
                    return componentClass.cast(c);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    assert false : "ERROR: Casting component.";
                }
            }
        }
        return null;
    }

    public <T extends Component> void removeComponent(Class<T> componentClass) {
        for (int i=0; i < components.size(); i++) {
            Component c = components.get(i);
            if (componentClass.isAssignableFrom(c.getClass())) {
                components.remove(i);
                return;
            }
        }
    }

    public void addComponent(Component c) {
        this.components.add(c);
        c.gameObject = this;
    }

    public void update(float dt) {
        for (int i=0; i < components.size(); i++) {
            components.get(i).update(dt);
        }
    }

    public void start() {
        for (int i=0; i < components.size(); i++) {
            components.get(i).start();
        }
    }

    public int zIndex() {
        return this.zIndex;
    }
}
