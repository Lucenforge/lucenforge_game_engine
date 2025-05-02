package lucenforge.graphics;

import lucenforge.output.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    private Vector3f position;
    private Vector3f target;
    boolean isOrtho = false;
    float orthoSize = 1.0f;

    public Matrix4f getViewMatrix(){
        return new Matrix4f().lookAt(position, target, new Vector3f(0, 1, 0));
    }

    public Matrix4f getProjectionMatrix() {
        float aspectRatio = Window.getAspectRatio();
        if (isOrtho) {
            return new Matrix4f().ortho(-aspectRatio * orthoSize, aspectRatio * orthoSize, -orthoSize, orthoSize, -1.0f, 1000.0f);
        } else {
            return new Matrix4f().perspective((float) Math.toRadians(70), aspectRatio, 0.01f, 1000.0f);
        }
    }


    public void setPosition(Vector3f position){
        this.position = position;
    }
    public void setTarget(Vector3f target){
        this.target = target;
    }
    public void setOrthoSize(float size){
        this.orthoSize = size;
    }
}
