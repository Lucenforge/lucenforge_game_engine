package lucenforge.graphics.primitives.mesh;

import lucenforge.files.Log;
import lucenforge.graphics.shaders.Shader;
import lucenforge.graphics.shaders.ShaderParameter;

import java.util.ArrayList;

public class MeshGroup extends Mesh{

    protected final ArrayList<Mesh> meshes = new ArrayList<>();

    public void addMesh(Mesh mesh){
        mesh.setParent(this);
        // Initialize the mesh if this MeshGroup has a shader and usage defined
        if(shader() != null && usage() != null){
            mesh.init(usage(), shader());
        }else if(shader() != null) {
            // If the shader is defined but not the usage, just set the shader
            mesh.setShader(shader());
        }
        // Set all parameters of this MeshGroup to the new mesh
        for(ShaderParameter param : getParams().values()){
            mesh.setParam(param.name(), param.getValue());
        }
        // Add the mesh to the list
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
        super.cleanup();
    }

    @Override
    public void setShader(Shader shader){
        super.setShader(shader);
        for(Mesh mesh : meshes){
            mesh.setShader(shader);
        }
    }

    @Override
    public void setShader(String shaderName){
        super.setShader(shaderName);
        for(Mesh mesh : meshes){
            mesh.setShader(shaderName);
        }
    }

    @Override
    public Shader shader(){
        // If this MeshGroup has a shader, return it
        if(super.shader() != null) {
            return super.shader();
        }
        // Otherwise, return the shader of the first mesh that has one
        if(!meshes.isEmpty()) {
            Shader meshShader = meshes.get(0).shader();
            if(meshShader != null) {
                // Also set this MeshGroup's shader for future calls
                setShader(meshShader);
                return meshShader;
            }
        }
        // No shader found
        return null;
    }

    @Override
    public void setParam(String name, Object value){
        if(shader() != null && shader().isUniformRequired(name)){
            super.setParam(name, value);
        }
        for(Mesh mesh : meshes){
            mesh.setParam(name, value);
        }
    }

    @Override
    public void init(Usage usage, Shader shader){
        super.setShader(shader);
        super.setUsage(usage);
        for(Mesh mesh : meshes){
            mesh.init(usage, shader);
        }
    }

}
