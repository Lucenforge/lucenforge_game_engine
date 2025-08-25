package lucenforge.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class WorldEntity {

    private Vector3f position = new Vector3f(0, 0, 0);
    private Vector3f rotation = new Vector3f(0, 0, 0);
    private Vector3f scale    = new Vector3f(1, 1, 1);

    private WorldEntity parent;

    public void setParent(WorldEntity parent){
        this.parent = parent;
    }
    public WorldEntity parent(){return parent;}

    // Position
    public Vector3f position(){
        return position;
    }
    public void setPosition(Vector3f position){
        this.position = position;
    }
    public void translate(Vector3f diff){this.position.add(diff);}

    // Rotation
    public Vector3f rotation(){
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
        return scale;
    }
    public void setScale(float scale){
        this.scale = new Vector3f(scale, scale, scale);
    }
    public void setScale(Vector3f scale){
        this.scale = scale;
    }

    // Get Model Matrix for rendering
    public Matrix4f getModelMatrix() {
        Matrix4f local = new Matrix4f()
                .identity()
                .translate(position())
                .rotateY((float)Math.toRadians(rotation().y))
                .rotateZ((float)Math.toRadians(rotation().z))
                .rotateX((float)Math.toRadians(rotation().x))
                .scale(scale());

        if (parent() != null) {
            return new Matrix4f(parent.getModelMatrix()).mul(local);
        }
        return local;
    }
}
