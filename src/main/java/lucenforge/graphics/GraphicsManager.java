package lucenforge.graphics;

import lucenforge.Engine;
import lucenforge.files.FileTools;
import lucenforge.files.Log;
import lucenforge.output.Window;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.opengl.GL11.glViewport;

public class GraphicsManager {

    // Lookup table for shaders
    public static HashMap<String, Shader> masterShaders;
    // List of all render layers
    private static final ArrayList<RenderLayer> renderLayers = new ArrayList<>();

    // Initialize the renderer
    public static void init(Window window) {

        // Initialize OpenGL
        glfwMakeContextCurrent(window.id());
        GL.createCapabilities();
        glViewport(0, 0, window.width(), window.height());

        // Initialize shaders
        loadShaderFiles();
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

    // Loads all shaders from the shaders directory
    private static void loadShaderFiles(){
        if(masterShaders != null)
            return;
        masterShaders = new HashMap<>();
        //Check if the shaders directory exists, if not, create it
        FileTools.createDirectory("src/main/resources/shaders");
        //Get list of all files in the shaders directory with the extension .vert.glsl or .frag.glsl
        ArrayList<Path> vertFiles = FileTools.getFilesInDir("src/main/resources/shaders", ".vert.glsl");
        ArrayList<Path> fragFiles = FileTools.getFilesInDir("src/main/resources/shaders", ".frag.glsl");
        assert(vertFiles.size() == fragFiles.size()) : "Number of vertex and fragment shaders do not match!";
        //Check that the vertex and fragment shader names match
        for(int i = 0; i < vertFiles.size(); i++){
            String vertFilePath = vertFiles.get(i).toString();
            String fragFilePath = fragFiles.get(i).toString();
            String vertFileName = vertFilePath.substring(vertFilePath.lastIndexOf("\\") + 1, vertFilePath.indexOf("."));
            String fragFileName = fragFilePath.substring(fragFilePath.lastIndexOf("\\") + 1, fragFilePath.indexOf("."));
            //Check that the vertex and fragment shader names match
            if(!vertFileName.equals(fragFileName)) {
                Log.writeln(Log.WARNING, "Vertex and fragment shader names do not match; Skipping: ("
                        + vertFileName + ", " + fragFileName + ")");
                return;
            }
            //Read the shader files
            String vertFileContents = FileTools.readFile(vertFilePath);
            String fragFileContents = FileTools.readFile(fragFilePath);
            //Create the shader program
            Shader shader = new Shader(vertFileContents, fragFileContents);
            //Load it into the shader lookup table
            masterShaders.put(vertFileName, shader);
            Log.writeln(Log.DEBUG, "Shader loaded: " + fragFileName);
        }
    }

    // Convert pixel coordinates to normalized device coordinates
    public static Vector2f pxToRaw(Vector2i p){
        float x = ((float)p.x / Engine.getWindow().width()) * 2 - 1;
        float y = ((float)p.y / Engine.getWindow().height()) * 2 - 1;
        return new Vector2f(x, y);
    }
    // Convert pixel size to normalized device size todo: make square
    public static float pxToRaw(float p){
        float x = (p / Engine.getWindow().width()) * 2;
        float y = (p / Engine.getWindow().height()) * 2;
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
