package lucenforge.graphics;

import lucenforge.files.FileTools;
import lucenforge.files.Log;
import lucenforge.output.Window;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;

public class GraphicsManager {

    // Lookup table for shaders
    public static HashMap<String, Shader> masterShaders;
    // Lookup table for meshes
    public static HashMap<String, Mesh> masterMeshes;
    // List of all render layers
    private static final ArrayList<RenderLayer> renderLayers = new ArrayList<>();

    // Initialize the renderer
    public static void init(Window window) {

        // Initialize OpenGL
        glViewport(0, 0, window.width(), window.height());  // Set the viewport to half the window size
        glEnable(GL_MULTISAMPLE); // Enable anti-aliasing (multisampling)

        Log.writeln(Log.DEBUG, "Graphics: OpenGL version: " + glGetString(GL_VERSION));
        Log.writeln(Log.DEBUG, "Graphics: Renderer: " + glGetString(GL_RENDERER));
        Log.writeln(Log.DEBUG, "Graphics: Vendor: " + glGetString(GL_VENDOR));

        // Load shaders and meshes
        masterShaders = FileTools.loadShaderFiles();
        masterMeshes = FileTools.loadMeshFiles();
    }

    // Register a render layer
    public static void registerRenderLayer(RenderLayer layer) {
        if (layer == null) {
            Log.writeln(Log.ERROR, "Attempted to register a null render layer!");
            return;
        }
        renderLayers.add(layer);
        Log.writeln(Log.DEBUG, "Render layer registered: " + layer.getClass().getSimpleName());
    }

    // Convert pixel coordinates to normalized device coordinates
    public static Vector2f pxToNDC(Vector2i p){
        float x = ((float)p.x / Window.getDim().x) * 2 - 1;
        float y = 1 - ((float)p.y / Window.getDim().y) * 2;
//        float y = ((float)p.y / Engine.getWindow().height()) * 2 - 1;
        return new Vector2f(x, y);
    }
    // Convert pixel size to normalized device size todo: make square
    public static float pxToNDC(int p){
        float x = ((float) p / Window.getDim().x) * 2;
        float y = ((float) p / Window.getDim().y) * 2;
        return (x + y)/2;
    }

    public static void cleanup() {
        // Cleanup all render layers
        for (RenderLayer layer : renderLayers) {
            layer.cleanup();
        }
        renderLayers.clear();
        Log.writeln(Log.DEBUG, "All render layers cleaned up.");
    }

    private GraphicsManager(){} // Prevent instantiation
}
