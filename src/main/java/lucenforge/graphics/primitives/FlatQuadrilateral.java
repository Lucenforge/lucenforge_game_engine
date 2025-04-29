package lucenforge.graphics.primitives;

import lucenforge.graphics.Mesh;
import org.joml.Vector2f;

public class FlatQuadrilateral extends Mesh {

    public void init(Vector2f p1, Vector2f p2, Vector2f p3, Vector2f p4, Usage usage){
        // Define the vertices of the quadrilateral
        float[] verts = {
                p1.x, p1.y, 0.0f,
                p2.x, p2.y, 0.0f,
                p3.x, p3.y, 0.0f,
                p4.x, p4.y, 0.0f
        };
        // Define the indices for the two triangles that make up the quadrilateral
        int[] indices = {
                0, 1, 3,  // First Triangle
                1, 2, 3   // Second Triangle
        };
        init(verts, indices, usage);
    }

    public void update(Vector2f p1, Vector2f p2, Vector2f p3, Vector2f p4) {
        // Update the vertices of the quadrilateral
        float[] verts = {
                p1.x, p1.y, 0.0f,
                p2.x, p2.y, 0.0f,
                p3.x, p3.y, 0.0f,
                p4.x, p4.y, 0.0f
        };
        // Update the vertex buffer with the new vertices
        super.update(verts);
    }
}
