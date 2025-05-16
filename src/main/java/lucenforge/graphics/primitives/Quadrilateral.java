package lucenforge.graphics.primitives;

import lucenforge.graphics.primitives.mesh.Mesh;
import lucenforge.graphics.primitives.mesh.Vertex;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;

public class Quadrilateral extends Mesh {

    public void init(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4, Usage usage){
        // Define the vertices of the quadrilateral
        ArrayList<Vertex> verts = new ArrayList<>();
        verts.add(new Vertex(new Vector3f(p1.x, p1.y, p1.z)));
        verts.add(new Vertex(new Vector3f(p2.x, p2.y, p2.z)));
        verts.add(new Vertex(new Vector3f(p3.x, p3.y, p3.z)));
        verts.add(new Vertex(new Vector3f(p4.x, p4.y, p4.z)));

        // Define the indices for the two triangles that make up the quadrilateral
        ArrayList<Vector3i> faces = new ArrayList<Vector3i>();
        faces.add(new Vector3i(0, 1, 3));  // First Triangle
        faces.add(new Vector3i(1, 2, 3));  // Second Triangle

        super.init(verts, faces, usage);
    }

    public void update(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4) {
        // Update the vertices of the quadrilateral
        ArrayList<Vertex> verts = new ArrayList<>();
        verts.add(new Vertex(new Vector3f(p1.x, p1.y, p1.z)));
        verts.add(new Vertex(new Vector3f(p2.x, p2.y, p2.z)));
        verts.add(new Vertex(new Vector3f(p3.x, p3.y, p3.z)));
        verts.add(new Vertex(new Vector3f(p4.x, p4.y, p4.z)));

        // Update the vertex buffer with the new vertices
        super.updateVerts(verts);
    }
}
