package lucenforge.graphics.primitives.mesh;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Objects;

public class Vertex {

    public Vector3f position;
    public ArrayList<Vector2f> textureCoords;
    public Vector3f normal;

    public Vertex(Vector3f position){
        this(position, null);
    }

    public Vertex(Vector3f position, Vector3f normal) {
        this.position = new Vector3f(position); // ← clone
        this.normal = normal != null ? new Vector3f(normal) : null; // ← clone
    }

    public void addTextureCoordinate(Vector2f texture) {
        if(texture == null) return;
        if (this.textureCoords == null) this.textureCoords = new ArrayList<>();
        this.textureCoords.add(new Vector2f(texture)); // ← clone
    }

    public int getFloatStride(){
        int stride = 3; // 3 floats for position
        stride += textureCoords != null ? 2 : 0; // 2 floats for texture coordinates
        stride += normal != null ? 3 : 0; // 3 floats for normal
        return stride;
//        return 3+2+3; // 3 floats for position, 2 for texture coordinates, 3 for normal
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Vertex other)) return false;
        if (this == obj) return true;
        boolean pos = (position == null && other.position == null) || (Objects.equals(position, other.position));
        boolean nor = (normal == null && other.normal == null) || (Objects.equals(normal, other.normal));
        return pos && nor;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        if (position != null)
            hash = 31 * hash + Float.floatToIntBits(position.x)
                    + Float.floatToIntBits(position.y)
                    + Float.floatToIntBits(position.z);

        if (normal != null)
            hash = 31 * hash + Float.floatToIntBits(normal.x)
                    + Float.floatToIntBits(normal.y)
                    + Float.floatToIntBits(normal.z);

        return hash;
    }

}
