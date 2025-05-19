package lucenforge.graphics.primitives.mesh;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Objects;

public class Vertex {

    public Vector3f position;
    public Vector2f texture;
    public Vector3f normal;

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
        boolean pos = (position == null && other.position == null) || (Objects.equals(position, other.position));
        boolean tex = (texture == null && other.texture == null) || (Objects.equals(texture, other.texture));
        boolean nor = (normal == null && other.normal == null) || (Objects.equals(normal, other.normal));
        return pos && tex && nor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, texture, normal);
    }

}
