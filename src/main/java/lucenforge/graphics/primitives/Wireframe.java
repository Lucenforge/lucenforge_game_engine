package lucenforge.graphics.primitives;

import lucenforge.graphics.primitives.mesh.Mesh;
import lucenforge.graphics.primitives.mesh.MeshGroup;
import lucenforge.graphics.primitives.mesh.Vertex;
import lucenforge.graphics.shaders.Shader;
import org.joml.Vector3i;

import java.util.ArrayList;

public class Wireframe extends MeshGroup {

    public Wireframe(Mesh target){
        float width = 0.01f;
        ArrayList<Vertex> vertices = target.vertices();
        ArrayList<Vector3i> faces = target.faces();
        for(Vector3i face : faces){
            Line line1 = new Line(width, vertices.get(face.x).position, vertices.get(face.y).position);
            Line line2 = new Line(width, vertices.get(face.y).position, vertices.get(face.z).position);
            Line line3 = new Line(width, vertices.get(face.z).position, vertices.get(face.x).position);
            addMesh(line1);
            addMesh(line2);
            addMesh(line3);
        }
    }

}
