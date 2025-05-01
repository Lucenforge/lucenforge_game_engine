package lucenforge.graphics.primitives;

import lucenforge.graphics.Renderable;

import java.util.ArrayList;

public class MeshGroup extends Mesh{

    ArrayList<Mesh> meshes = new ArrayList<>();

    public void addMesh(Mesh mesh){
        meshes.add(mesh);
    }

    @Override
    public void render() {
        for(Mesh mesh : meshes){
            mesh.render();
        }
    }

    @Override
    public void cleanup(){
        for(Mesh mesh : meshes){
            mesh.cleanup();
        }
    }

}
