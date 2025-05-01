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

    // Adds a mesh to the render batch for the given shader
    public void add(Mesh mesh) {
        Shader shader = mesh.shader();
        String shaderName = shader.name();
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
        ArrayList<Mesh> meshes = shaderBatches.get(shader);
        if (meshes == null) {
            Log.writeln(Log.ERROR, "No render batch found for shader: " + shaderName);
            return;
        }
        meshes.add(mesh);
    }


    // Render Loop Iteration: Clears the screen and prepares for the next frame
    public void render() {
        // Enable blending todo make these settable
        glEnable(GL_BLEND);
        // Enable depth testing
        glEnable(GL_DEPTH_TEST);
        // Disable culling (for 2D rendering, we want to render all faces)
        glDisable(GL_CULL_FACE);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        // Go through each shader and render the meshes
        for(Shader shader : shaderBatches.keySet()) {
            // Get the meshes for this shader
            ArrayList<Mesh> meshes = shaderBatches.get(shader);
            shader.bind();
            for (Mesh mesh : meshes) {
//                // Set the color uniform todo make this settable
                if(shader.checkThatUniformsAreSet()) {
                    mesh.render();
                }else{
                    Log.writeln(Log.ERROR, "Shader " + shader.name() + " has not set all required uniforms!");
                }
            }
            shader.unbind();
        }
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