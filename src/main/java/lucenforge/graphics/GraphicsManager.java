package lucenforge.graphics;

import lucenforge.files.FileTools;
import lucenforge.files.Log;
import lucenforge.output.Window;
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;

public class GraphicsManager {

    // Lookup table for shaders
    public static HashMap<String, Shader> masterShaders;
    // List of all render layers
    private static final ArrayList<RenderLayer> renderLayers = new ArrayList<>();

    // Initialize the renderer
    public static void init(Window window) {

        // Initialize OpenGL
        glViewport(0, 0, window.width(), window.height());  // Set the viewport to half the window size
        glEnable(GL_MULTISAMPLE); // Enable anti-aliasing (multisampling)
        int samples = glGetInteger(GL_SAMPLES);
        Log.writeln(Log.DEBUG, "Anti-aliasing enabled with " + samples + " samples per pixel.");

        Log.writeln(Log.DEBUG, "Graphics: OpenGL version: " + glGetString(GL_VERSION));
        Log.writeln(Log.DEBUG, "Graphics: Renderer: " + glGetString(GL_RENDERER));
        Log.writeln(Log.DEBUG, "Graphics: Vendor: " + glGetString(GL_VENDOR));

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
