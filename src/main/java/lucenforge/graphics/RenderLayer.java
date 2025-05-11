package lucenforge.graphics;

import lucenforge.Engine;
import lucenforge.files.Log;
import lucenforge.graphics.primitives.mesh.Mesh;
import lucenforge.output.Window;

import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;

public class RenderLayer implements Renderable{

    // Lookup table for shaders
    private final HashMap<String, Shader> shaders = new HashMap<>();
    // Batches of meshes that have the same shader (shader batching)
    private final HashMap<Shader, ArrayList<Mesh>> shaderMeshBatches = new HashMap<>();
    // List of renderables that don't have an associated shader
    private final ArrayList<Renderable> shaderlessRenderables = new ArrayList<>();

    private Camera camera;

    public RenderLayer(){
        GraphicsManager.registerRenderLayer(this);
    }

    // Adds a mesh to the render batches
    public void add(Mesh mesh) {
        Shader shader = mesh.shader();
        if(shader == null) {
            Log.writeln(Log.ERROR, "Mesh not added to render layer; Mesh has no shader set!");
            return;
        }
        String shaderName = shader.name();
        // Check if the shader exists in the lookup table
        if (!shaders.containsKey(shaderName)){
            // If it doesn't, try to get it from the master shaders
            if(GraphicsManager.masterShaders.containsKey(shaderName)){
                // Add the shader to the local lookup table
                shaders.put(shaderName, GraphicsManager.masterShaders.get(shaderName));
                // Add the shader to the render batch
                shaderMeshBatches.put(shaders.get(shaderName), new ArrayList<>());
            } else {
                Log.writeln(Log.ERROR, "Shader not found in master lookup: " + shaderName);
                return;
            }
        }
        // Get the shader from the local lookup table
        ArrayList<Mesh> meshes = shaderMeshBatches.get(shader);
        if (meshes == null) {
            Log.writeln(Log.ERROR, "No render batch found for shader: " + shaderName);
            return;
        }
        meshes.add(mesh);
    }
    // Adds a Renderable to the shaderless list
    public void add(Renderable renderable){
        shaderlessRenderables.add(renderable);
    }

    // Sets the camera for this render layer
    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    // Render Loop Iteration: Clears the screen and prepares for the next frame
    public void render(){render(true);}
    public void render(boolean clearDepth) {
        // Render when the FPS target is reached
        if(!GraphicsManager.shouldRender()){
            return;
        }
        // Clear the screen
        if(clearDepth)
            Engine.clearDepthBuffer();
        // Enable blending todo make these settable
        glEnable(GL_BLEND);
        // Enable depth testing
        glEnable(GL_DEPTH_TEST);
        // Disable culling (for 2D rendering, we want to render all faces)
//        glDisable(GL_CULL_FACE);
        // Set the alpha bit in color to blend like expected
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Go through the shaderless group and render that
        for(Renderable renderable : shaderlessRenderables){
            renderable.render();
        }

        // Go through each shader and render the meshes
        for(Shader shader : shaderMeshBatches.keySet()) {
            // Get the meshes for this shader
            ArrayList<Mesh> meshes = shaderMeshBatches.get(shader);

            shader.bind();

            // Set the camera parameters
            if(shader.isUniformRequired("projection"))
                shader.getParam("projection").set(camera.getProjectionMatrix());
            if(shader.isUniformRequired("view"))
                shader.getParam("view").set(camera.getViewMatrix());
            if(shader.isUniformRequired("aspectRatio"))
                shader.getParam("aspectRatio").set(Window.getAspectRatio());
            if(shader.isUniformRequired("cameraPos"))
                shader.getParam("cameraPos").set(camera.position());

            // Set the shader parameters for each mesh
            for (Mesh mesh : meshes) {
                mesh.render();
            }
            shader.unbind();
        }
    }

    // Cleans up all shaders and meshes
    @Override
    public void cleanup() {
        for(Shader shader : shaders.values()) {
            //Clean up meshes
            for(Mesh mesh : shaderMeshBatches.get(shader)) {
                mesh.cleanup();
            }
            //Clean up shaderless
            for(Renderable renderable : shaderlessRenderables){
                renderable.cleanup();
            }
            //Clean up shader
            shader.cleanup();
        }
    }


}