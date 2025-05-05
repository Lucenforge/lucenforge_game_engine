package lucenforge.graphics;

import lucenforge.entity.WorldEntity;
import lucenforge.output.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera extends WorldEntity {

    boolean isOrtho = false;
    float orthoHeight = 1.0f;
    float viewAngle = 70.0f;

    public Matrix4f getViewMatrix(){
        if (toLookAtTarget) {
            return new Matrix4f().lookAt(position, lookAtPos, new Vector3f(0, 1, 0));
        }else{
            return new Matrix4f()
                .lookAlong(lookDirection, new Vector3f(0, 1, 0))
                .translate(-position.x, -position.y, -position.z);
        }
    }

    public Matrix4f getProjectionMatrix() {
        float aspectRatio = Window.getAspectRatio();
        if (isOrtho) {
            return new Matrix4f().ortho(-aspectRatio * orthoHeight, aspectRatio * orthoHeight, -orthoHeight, orthoHeight, -1.0f, 1000.0f);
        } else {
            return new Matrix4f().perspective((float) Math.toRadians(viewAngle), aspectRatio, 0.01f, 1000.0f);
        }
    }

    public void setOrthoHeight(float size){
        this.orthoHeight = size/2.0f; //Height in meters
    }

    public void setViewAngle(float angle){
        this.viewAngle = angle;
    }
}
