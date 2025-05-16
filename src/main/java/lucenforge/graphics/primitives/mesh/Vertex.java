package lucenforge.graphics.primitives.mesh;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Objects;

public class Vertex {

    Vector3f position;
    Vector2f texture;
    Vector3f normal;

    public Vertex(Vector3f position){
        this(position, null, null);
    }

    public Vertex(Vector3f position, Vector2f texture, Vector3f normal) {
        this.position = position;
        this.texture = texture;
        this.normal = normal;
    }

    public int getFloatStride(){
        int stride = 3; // 3 floats for position
        stride += texture != null ? 2 : 0; // 2 floats for texture coordinates
        stride += normal != null ? 3 : 0; // 3 floats for normal
        return stride;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vertex other)) return false;
        if (this == obj) return true;
        return position.equals(other.position) &&
                texture.equals(other.texture) &&
                normal.equals(other.normal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, texture, normal);
    }

}
