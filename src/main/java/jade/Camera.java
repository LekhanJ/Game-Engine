package jade;
/*
    So, the things we are working with is
    The View Matrix (a.k.a the Look-at Matrix),  -> It tells where the camera is in relation to the world
    The Projection Matrix,  -> It defines the projection mode (Orthographic or Perspective) which tells us how big we want the screen space to be
    The aPos,  -> The position of all the objects on the screen

    So we do ->  Projection Matrix * View Matrix * aPos  ->  Which converts world coordinates to normalised device coordinates
                 <-------- Multiplication order -------
*/
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera {
    private Matrix4f projectionMatrix, viewMatrix;
    private Vector2f position;

    public Camera(Vector2f position) {
        this.position = position;
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        adjustProjection();
    }

    public void adjustProjection() {
        projectionMatrix.identity();
        projectionMatrix.ortho(0.0f, 32.0f * 40.0f, 0.0f, 32.0f * 21.0f, 0.0f, 100.0f);
    }

    public Matrix4f getViewMatrix() {
        Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f cameraUp = new Vector3f(0.0f, 1.0f, 0.0f);
        this.viewMatrix.identity();
        this.viewMatrix = viewMatrix.lookAt(
                new Vector3f(position.x, position.y, 20.0f), // Where the camera is located
                cameraFront.add(position.x, position.y, 0.0f), // Where the camera looking towards
                cameraUp  // The direction of up
        );
        return this.viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public Vector2f getPosition() {
        return this.position;
    }
}
