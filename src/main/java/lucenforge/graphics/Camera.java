package lucenforge.graphics;

import lucenforge.entity.WorldEntity;
import lucenforge.files.Log;
import lucenforge.output.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera extends WorldEntity {

    protected Vector3f lookDirection = new Vector3f(0, 0, -1);
    protected boolean shouldLookAtTarget = false;
    protected Vector3f lookAtPos;
    protected WorldEntity lookAtTarget = null;

    float maxPitch = (float) Math.toRadians(89.0);

    boolean isOrtho = false;
    float orthoHeight = 1.0f;
    float viewAngle = 70.0f;

    public Matrix4f getViewMatrix(){
        if (shouldLookAtTarget) {
            // Look at object
            if(lookAtTarget == null)
                return new Matrix4f().lookAt(position, lookAtPos, new Vector3f(0, 1, 0));
            // Look at position
            else
                return new Matrix4f().lookAt(position, lookAtTarget.position(), new Vector3f(0, 1, 0));
        }else{
            // Look in direction
            return new Matrix4f()
                .lookAlong(lookDirection, new Vector3f(0, 1, 0))
                .translate(-position.x, -position.y, -position.z);
        }
    }

    //todo make lookAt also change the rotation for controls issues

    public Matrix4f getProjectionMatrix() {
        float aspectRatio = Window.getAspectRatio();
        if (isOrtho) {
            return new Matrix4f().ortho(-aspectRatio * orthoHeight, aspectRatio * orthoHeight, -orthoHeight, orthoHeight, -1.0f, 1000.0f);
        } else {
            return new Matrix4f().perspective((float) Math.toRadians(viewAngle), aspectRatio, 0.01f, 1000.0f);
        }
    }

    public void lookAt(Vector3f lookAtPos){
        this.lookAtPos = lookAtPos;
        shouldLookAtTarget = true;
    }
    public void lookAt(WorldEntity target){
        this.lookAtTarget = target;
        shouldLookAtTarget = true;
    }

    @Override
    public void rotate(Vector3f rotation){
//        Log.writeln(Log.DEBUG, "Camera: rotate before: " + rotation + " " + this.rotation);
        super.rotate(rotation);
//        Log.writeln(Log.DEBUG, "Camera: rotate after: " + rotation + " " + this.rotation);
        lookToward(this.rotation);
    }

    @Override
    public void setRotation(Vector3f rotation){
//        Log.writeln(Log.DEBUG, "Camera: set rotation before: " + rotation + " " + this.rotation);
        super.setRotation(rotation);
//        Log.writeln(Log.DEBUG, "Camera: set rotation after: " + rotation + " " + this.rotation);
        lookToward(this.rotation);
    }

    public void lookToward(Vector3f angles){
        float yaw = (float) Math.toRadians(angles.y);
        float pitch = (float) Math.toRadians(angles.x);
        pitch = Math.max(-maxPitch, Math.min(maxPitch, pitch));
        lookDirection.x = (float) Math.cos(yaw) * (float) Math.cos(pitch);
        lookDirection.y = (float) Math.sin(pitch);
        lookDirection.z = (float) Math.sin(yaw) * (float) Math.cos(pitch);
        lookDirection.normalize();
    }

    public void setOrthoHeight(float size){
        this.orthoHeight = size/2.0f; //Height in meters
    }

    public void setViewAngle(float angle){
        this.viewAngle = angle;
    }

}
