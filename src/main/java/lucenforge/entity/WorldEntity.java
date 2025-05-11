package lucenforge.entity;

import lucenforge.files.Log;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class WorldEntity {

    protected Vector3f position = new Vector3f(0, 0, 0);
    protected Vector3f rotation = new Vector3f(0, 0, 0);
    protected Vector3f scale = new Vector3f(1, 1, 1);

    // Position
    public Vector3f position(){
        return position;
    }
    public void setPosition(Vector3f position){
        this.position = position;
    }

    // Rotation
    public void rotate(Vector3f angles){
        setRotation(new Vector3f(this.rotation).add(angles));
    }
    public void setRotation(Vector3f angles){
        this.rotation = angles;
    }

    // Scale
    public void scale(float scale){
        this.scale = new Vector3f(scale, scale, scale);
    }
    public void scale(Vector3f scale){
        this.scale = scale;
    }
}
