package lucenforge.graphics.primitives;

import lucenforge.graphics.primitives.mesh.Mesh;
import lucenforge.graphics.primitives.mesh.MeshGroup;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;

public class Wireframe extends MeshGroup {

    public Wireframe(Mesh target, Mesh.Usage usage){
        float width = 0.01f;
        ArrayList<Vector3f> verts = target.vertices();
        ArrayList<Vector3i> indexes = target.indices();
        for(Vector3i index : indexes){
            Line line1 = new Line();
            Line line2 = new Line();
            Line line3 = new Line();
            line1.init(verts.get(index.x), verts.get(index.y), width, usage);
            line2.init(verts.get(index.y), verts.get(index.z), width, usage);
            line3.init(verts.get(index.z), verts.get(index.x), width, usage);
            addMesh(line1);
            addMesh(line2);
            addMesh(line3);
        }
    }

}
