package lucenforge.graphics.primitives;

import lucenforge.graphics.primitives.mesh.Mesh;
import lucenforge.graphics.primitives.mesh.Vertex;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;

public class Quadrilateral extends Mesh {

    public Quadrilateral(){}
    public Quadrilateral(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4){
        setCorners(p1, p2, p3, p4);
    }

    public void setCorners(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4){
        // Define the vertices of the quadrilateral
        ArrayList<Vertex> verts = new ArrayList<>();
        verts.add(new Vertex(new Vector3f(p1.x, p1.y, p1.z)));
        verts.add(new Vertex(new Vector3f(p2.x, p2.y, p2.z)));
        verts.add(new Vertex(new Vector3f(p3.x, p3.y, p3.z)));
        verts.add(new Vertex(new Vector3f(p4.x, p4.y, p4.z)));

        // Add UV coordinates
//        float width = p3.distance(p2);
        float width = 1;
//        float height = p2.distance(p1);
        float height = 1;
        verts.get(0).addTextureCoordinate(new Vector2f(0, 0));           // bottom-left
        verts.get(1).addTextureCoordinate(new Vector2f(0, height));      // top-left
        verts.get(2).addTextureCoordinate(new Vector2f(width, height));  // top-right
        verts.get(3).addTextureCoordinate(new Vector2f(width, 0));       // bottom-right

        // Define the indices for the two triangles that make up the quadrilateral
        ArrayList<Vector3i> faces = new ArrayList<Vector3i>();
        faces.add(new Vector3i(0, 1, 2));  // Lower-left triangle
        faces.add(new Vector3i(0, 2, 3));  // Upper-right triangle

        computeNormals(false, verts, faces);

        super.setTopology(verts, faces);
    }

    public void update(Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4) {
        // Update the vertices of the quadrilateral
        ArrayList<Vertex> verts = new ArrayList<>();
        verts.add(new Vertex(new Vector3f(p1.x, p1.y, p1.z)));
        verts.add(new Vertex(new Vector3f(p2.x, p2.y, p2.z)));
        verts.add(new Vertex(new Vector3f(p3.x, p3.y, p3.z)));
        verts.add(new Vertex(new Vector3f(p4.x, p4.y, p4.z)));

        computeNormals(false, verts, faces());

        // Update the vertex buffer with the new vertices
        super.updateVerts(verts);
    }
}
