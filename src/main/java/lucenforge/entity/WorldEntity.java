package lucenforge.entity;

import org.joml.Vector3f;

public class WorldEntity {

    private Vector3f position = new Vector3f(0, 0, 0);
    private Vector3f rotation = new Vector3f(0, 0, 0);
    private Vector3f scale    = new Vector3f(1, 1, 1);

    private WorldEntity parent;

    public void setParent(WorldEntity parent){
        this.parent = parent;
    }

    // Position
    public Vector3f position(){
        if(this.parent != null)
            return new Vector3f(parent.position()).add(this.position);
        return position;
    }
    public void setPosition(Vector3f position){
        this.position = position;
    }
    public void translate(Vector3f diff){this.position.add(diff);}

    // Rotation
    public Vector3f rotation(){
        if(this.parent != null)
            return this.parent.rotation().add(rotation);
        return rotation;
    }
    public void rotate(Vector3f angles){
        setRotation(new Vector3f(this.rotation).add(angles));
    }
    public void setRotation(Vector3f angles){
        this.rotation = angles;
    }

    // Scale
    public Vector3f scale(){
        if(this.parent != null)
            return new Vector3f(parent.scale()).mul(this.scale);
        return scale;
    }
    public void setScale(float scale){
        this.scale = new Vector3f(scale, scale, scale);
    }
    public void setScale(Vector3f scale){
        this.scale = scale;
    }
}
