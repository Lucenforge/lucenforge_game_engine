package lucenforge.graphics;

import lucenforge.files.Log;
import lucenforge.graphics.shaders.Shader;
import lucenforge.output.Monitor;
import lucenforge.output.Window;
import lucenforge.files.Properties;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;

public class GraphicsManager {

    // Lookup table for shaders
    public static HashMap<String, Shader> masterShaders = new HashMap<>();
    // List of all render layers
    private static final ArrayList<RenderLayer> renderLayers = new ArrayList<>();

    private static final float[] fpsRecord = new float[100];
    private static int fpsRecordIndex = 0;
    private static long targetFrameTime;
    private static long nextFrameTargetTimestamp;
    private static long lastFrameTimestamp;
    private static long lastLastFrameTimestamp;
    private static boolean shouldRender = false;

    // Initialize the renderer
    public static void init(Window window) {
        Window.set(window);

        // Initialize OpenGL
        glViewport(0, 0, window.width(), window.height());  // Set the viewport to the window size
        glEnable(GL_MULTISAMPLE); // Enable anti-aliasing (multisampling)
        // Make internal geometry "mostly transparent"
        glPolygonMode(GL_BACK, GL_LINE); //GL_FRONT_AND_BACK

        Log.writeln(Log.SYSTEM, "Graphics: OpenGL version: " + glGetString(GL_VERSION));
        Log.writeln(Log.SYSTEM, "Graphics: Renderer: " + glGetString(GL_RENDERER) + ", Vendor: " + glGetString(GL_VENDOR));

        // Init frame time
        // Target FPS: -1 = no limit, 0 = monitor, >0 = that num
        long targetFPS = Properties.getInt("graphics", "target_fps", 0);
        if(targetFPS == -1)
            targetFrameTime = 0;
        else if(targetFPS == 0){
            Monitor monitor = window.monitor();
            targetFrameTime = (int) Math.floor(1000d / monitor.refreshRate());
        }else
            targetFrameTime = (int) Math.floor(1000d / targetFPS);
        nextFrameTargetTimestamp = targetFrameTime + System.currentTimeMillis();
        Log.writeln(Log.SYSTEM, "Targeting " + Math.round(1000f/ targetFrameTime) + " fps");

        // Init texture preferences
        Texture.init(false);
    }

    // Get the FPS for the last frame
    public static float getFPS(){
        return 1000f/(lastFrameMillis());
    }
    public static float getAvgFPS(){
        float total = 0;
        for(float fps : fpsRecord){
            total += fps;
        }
        return total/fpsRecord.length;
    }
    public static long lastFrameMillis(){
        return lastFrameTimestamp - lastLastFrameTimestamp;
    }

    public static boolean shouldRender(){
        return shouldRender;
    }

    public static void update(){
        long currentTimeMillis = System.currentTimeMillis();
        if(currentTimeMillis >= nextFrameTargetTimestamp) {
            lastLastFrameTimestamp = lastFrameTimestamp;
            nextFrameTargetTimestamp += targetFrameTime;
            shouldRender = true;

            lastFrameTimestamp = currentTimeMillis;
            fpsRecord[fpsRecordIndex] = getFPS();
            fpsRecordIndex = (fpsRecordIndex + 1) % fpsRecord.length;
        }else{
            shouldRender = false;
        }
    }

    // Register a render layer
    public static void registerRenderLayer(RenderLayer layer) {
        if (layer == null) {
            Log.writeln(Log.ERROR, "Attempted to register a null render layer!");
            return;
        }
        renderLayers.add(layer);
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
