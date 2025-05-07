package lucenforge.graphics.primitives;

import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;

public class Wireframe extends MeshGroup {

    public Wireframe(Mesh target, Mesh.Usage usage){
        float width = 0.01f;
        Vector3f[] verts = target.getVertices();
        Vector3i[] indexes = target.getIndices();
        for(Vector3i index : indexes){
            Line line1 = new Line();
            Line line2 = new Line();
            Line line3 = new Line();
            line1.init(verts[index.x], verts[index.y], width, usage);
            line2.init(verts[index.y], verts[index.z], width, usage);
            line3.init(verts[index.z], verts[index.x], width, usage);
            addMesh(line1);
            addMesh(line2);
            addMesh(line3);
        }
    }

}
