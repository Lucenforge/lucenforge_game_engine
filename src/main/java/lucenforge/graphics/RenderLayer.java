package lucenforge.graphics;

import lucenforge.files.FileTools;
import lucenforge.files.Log;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;

public class RenderLayer {

    // The shader programs to use for rendering
    private final HashMap<Shader, ArrayList<Mesh>> shaderBatches = new HashMap<>();
    // Lookup table for shaders
    private final HashMap<String, Shader> shaders = new HashMap<>();

    public RenderLayer(){
        GraphicsManager.registerRenderLayer(this);
    }

    // Render Loop Iteration: Clears the screen and prepares for the next frame
    public void render() {
        // Enable blending todo make these settable
        glEnable(GL_BLEND);
        // Enable depth testing
        glEnable(GL_DEPTH_TEST);
        // Disable culling (for 2D rendering, we want to render all faces)
//        glDisable(GL_CULL_FACE);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        // Go through each shader and render the meshes
        for(Shader shader : shaderBatches.keySet()) {
            // Get the uniform for the color ID
//            int flatColorID = shader.getUniformID("flatColor");
            // Get the meshes for this shader
            ArrayList<Mesh> meshes = shaderBatches.get(shader);
            shader.bind();
            for (Mesh mesh : meshes) {
//                // Set the color uniform todo make this settable
//                Vector4f color = mesh.getColor();
////                glUniform4f(flatColorID, color.x, color.y, color.z, color.w);
//
                Matrix4f model = new Matrix4f()
                        .identity()
                        .scale(0.75f); // Shrinks the mesh to fit inside NDC
//                // todo make this settable
                shader.setUniform("model", model);
                shader.setUniform("lightDir", new Vector3f(0.5f, -1f, 0.3f).normalize());
                shader.setUniform("baseColor", mesh.getColor());

                if(shader.checkUniformsSet())
                    mesh.render();
                else
                    Log.writeln(Log.ERROR, "Uniforms not set for shader: " + shader.getName());
            }
            shader.unbind();
        }
    }

    public void add(String meshName, Mesh.Usage usage, String shaderName){
        Mesh mesh = FileTools.getMeshFile(meshName, usage);
        add(mesh, shaderName);
    }

    // Adds a mesh to the render batch for the given shader
    public void add(Mesh mesh, String shaderName) {
        // Check if the shader exists in the lookup table
        if (!shaders.containsKey(shaderName)){
            // If it doesn't, try to get it from the master shaders
            if(GraphicsManager.masterShaders.containsKey(shaderName)){
                // Add the shader to the local lookup table
                shaders.put(shaderName, GraphicsManager.masterShaders.get(shaderName));
                // Add the shader to the render batch
                shaderBatches.put(shaders.get(shaderName), new ArrayList<>());
            } else {
                Log.writeln(Log.ERROR, "Shader not found in master lookup: " + shaderName);
                return;
            }
        }
        // Get the shader from the local lookup table
        Shader shader = shaders.get(shaderName);
        ArrayList<Mesh> meshes = shaderBatches.get(shader);
        if (meshes == null) {
            Log.writeln(Log.ERROR, "No render batch found for shader: " + shaderName);
            return;
        }
        meshes.add(mesh);
    }


    // Cleans up all shaders and meshes
    public void cleanup() {
        for(Shader shader : shaders.values()) {
            //Clean up meshes
            for(Mesh mesh : shaderBatches.get(shader)) {
                mesh.cleanup();
            }
            //Clean up shader
            shader.cleanup();
        }
    }


}