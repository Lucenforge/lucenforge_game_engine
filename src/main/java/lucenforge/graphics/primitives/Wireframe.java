package lucenforge.graphics.primitives;

import lucenforge.graphics.primitives.mesh.Mesh;
import lucenforge.graphics.primitives.mesh.MeshGroup;
import lucenforge.graphics.primitives.mesh.Vertex;
import org.joml.Vector3i;

import java.util.ArrayList;

public class Wireframe extends MeshGroup {

    public Wireframe(Mesh target, Mesh.Usage usage){
        float width = 0.01f;
        ArrayList<Vertex> vertices = target.vertices();
        ArrayList<Vector3i> faces = target.faces();
        for(Vector3i face : faces){
            Line line1 = new Line();
            Line line2 = new Line();
            Line line3 = new Line();
            line1.init(vertices.get(face.x).position, vertices.get(face.y).position, width, usage);
            line2.init(vertices.get(face.y).position, vertices.get(face.z).position, width, usage);
            line3.init(vertices.get(face.z).position, vertices.get(face.x).position, width, usage);
            addMesh(line1);
            addMesh(line2);
            addMesh(line3);
        }
    }

}
