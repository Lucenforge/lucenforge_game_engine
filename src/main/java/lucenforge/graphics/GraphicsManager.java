package lucenforge.graphics;

import lucenforge.files.FileTools;
import lucenforge.files.Log;
import lucenforge.files.Properties;
import lucenforge.graphics.primitives.Mesh;
import lucenforge.output.Window;
import lucenforge.physics.Physics;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

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

    private static Window window;

    // Initialize the renderer
    public static void init(Window window) {
        GraphicsManager.window = window;

        // Initialize OpenGL
        glViewport(0, 0, window.width(), window.height());  // Set the viewport to the window size
        glEnable(GL_MULTISAMPLE); // Enable anti-aliasing (multisampling)

        Log.writeln(Log.SYSTEM, "Graphics: OpenGL version: " + glGetString(GL_VERSION));
        Log.writeln(Log.SYSTEM, "Graphics: Renderer: " + glGetString(GL_RENDERER) + ", Vendor: " + glGetString(GL_VENDOR));

        // Load shaders and meshes
        masterShaders = FileTools.loadShaderFiles();
        masterMeshes = FileTools.loadMeshFiles();
    }

    // Get the FPS for the last frame
    public static float getFPS(){
        return 1f/(Physics.lastFrameMillis()/1000f);
    }

    public static boolean shouldRender(){
        float maxFrameTime = 1f/Properties.get("graphics", "max_fps", 60);
//        Log.writeln(Log.DEBUG, "Max frame time: " + maxFrameTime + ", Current frame time: " + Physics.currentFrameSeconds());
        return (Physics.currentFrameSeconds() > maxFrameTime);
    }

    // Register a render layer
    public static void registerRenderLayer(RenderLayer layer) {
        if (layer == null) {
            Log.writeln(Log.ERROR, "Attempted to register a null render layer!");
            return;
        }
        renderLayers.add(layer);
    }

    // Convert pixel coordinates to normalized device coordinates
    public static Vector3f pxToNDC(Vector2i p){
        return pxToNDC(new Vector3i(p.x, p.y, 0));
    }
    public static Vector3f pxToNDC(Vector3i p){
        float x = Window.getAspectRatio() * (((float) p.x / Window.getDim().x) * 2 - 1);
        float y = 1 - ((float) p.y / Window.getDim().y) * 2;
        float z = pxToNDC(p.z);
        return new Vector3f(x, y, z);
    }
    // Convert pixel size to normalized device size todo: make square
    public static float pxToNDC(int p){
        return ((float) p / Window.getDim().y) * 2;
    }
    public static Vector2i ndcToPx(Vector3f p){
        int x = (int) ((p.x + 1) / 2 * Window.getDim().x);
        int y = (int) ((1 - p.y) / 2 * Window.getDim().y);
        return new Vector2i(x, y);
    }

    public static void cleanup() {
        // Cleanup all render layers
        for (RenderLayer layer : renderLayers) {
            layer.cleanup();
        }
        renderLayers.clear();
    }

    private GraphicsManager(){} // Prevent instantiation
}
