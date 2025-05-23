package lucenforge.graphics.primitives.mesh;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Objects;

public class Vertex {

    public Vector3f position;
    public Vector2f texture;
    public Vector3f normal;

    public Vertex(Vector3f position){
        this(position, null, null);
    }

    public Vertex(Vector3f position, Vector2f texture, Vector3f normal) {
        this.position = new Vector3f(position); // ← clone
        this.texture = texture != null ? new Vector2f(normal) : null; // ← clone
        this.normal = normal != null ? new Vector3f(normal) : null; // ← clone
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
        int hash = 7;

        if (position != null)
            hash = 31 * hash + Float.floatToIntBits(position.x)
                    + Float.floatToIntBits(position.y)
                    + Float.floatToIntBits(position.z);

        if (texture != null)
            hash = 31 * hash + Float.floatToIntBits(texture.x)
                    + Float.floatToIntBits(texture.y);

        if (normal != null)
            hash = 31 * hash + Float.floatToIntBits(normal.x)
                    + Float.floatToIntBits(normal.y)
                    + Float.floatToIntBits(normal.z);

        return hash;
    }

}
