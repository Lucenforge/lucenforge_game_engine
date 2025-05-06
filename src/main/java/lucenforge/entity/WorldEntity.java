package lucenforge.entity;

import org.joml.Vector3f;

public class WorldEntity {

    protected Vector3f position = new Vector3f(0, 0, 0);
    protected Vector3f rotation = new Vector3f(0, 0, 0);
    protected Vector3f scale = new Vector3f(1, 1, 1);

    protected boolean toLookAtTarget = false;
    protected Vector3f lookAtPos;
    protected WorldEntity target = null;

    protected Vector3f lookDirection = new Vector3f(0, 0, -1);

    public Vector3f position(){
        return position;
    }

    public void setPosition(Vector3f position){
        this.position = position;
    }

    public void lookAt(Vector3f lookAtPos){
        this.lookAtPos = lookAtPos;
    }
    public void lookAt(WorldEntity target){
        this.target = target;
        this.lookAtPos = target.position();
    }

    public Vector3f getRotation(){
        float yaw = (float) Math.atan2(lookDirection.z, lookDirection.x);
        float pitch = (float) Math.asin(lookDirection.y);
        float roll = 0; // Roll is not used in this case
        return new Vector3f((float) Math.toDegrees(pitch), (float) Math.toDegrees(yaw), 0);
    }

    public void setRotation(Vector3f angles){
        float yaw = (float) Math.toRadians(angles.y);
        float pitch = (float) Math.toRadians(angles.x);
        lookDirection.x = (float) Math.cos(yaw) * (float) Math.cos(pitch);
        lookDirection.y = (float) Math.sin(pitch);
        lookDirection.z = (float) Math.sin(yaw) * (float) Math.cos(pitch);
        lookDirection.normalize();
    }

    public void setScale(float scale){
        this.scale = new Vector3f(scale, scale, scale);
    }
    public void setScale(Vector3f scale){
        this.scale = scale;
    }
}
