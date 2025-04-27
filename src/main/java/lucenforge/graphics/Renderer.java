package lucenforge.graphics;

import lucenforge.Engine;
import lucenforge.files.FileTools;
import lucenforge.files.Log;
import lucenforge.output.Window;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.*;

public class Renderer {

    // The shader programs to use for rendering
    private static final HashMap<ShaderProgram, ArrayList<Mesh>> renderBatches = new HashMap<>();
    // Lookup table for shaders
    private static final HashMap<String, ShaderProgram> shaders = new HashMap<>();

    // Initialize the renderer
    public static void init(Window window) {
        // Initialize OpenGL
        glfwMakeContextCurrent(window.id());
        GL.createCapabilities();
        glViewport(0, 0, window.width(), window.height());

        initShaders(); // Initialize shaders

        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Enable blending
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    // Render Loop Iteration: Clears the screen and prepares for the next frame
    public static void nextFrame() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        for(ShaderProgram shader : renderBatches.keySet()) {
            // Get the uniform for the color ID
            int flatColorID = shader.getUniformID("flatColor");
            // Get the meshes for this shader
            ArrayList<Mesh> meshes = renderBatches.get(shader);
            shader.bind();
            for (Mesh mesh : meshes) {
                // Set the color uniform
                Vector4f color = mesh.getColor();
                glUniform4f(flatColorID, color.x, color.y, color.z, color.w);
                mesh.render();
            }
            shader.unbind();
        }
    }

    // Adds a mesh to the render batch for the given shader
    public static void addToRenderBatch(String shaderName, Mesh mesh) {
        ShaderProgram shader = shaders.get(shaderName);
        if (shader == null) {
            Log.writeln(Log.WARNING, "Shader not found; Skipping: " + shaderName);
            return;
        }
        ArrayList<Mesh> meshes = renderBatches.get(shader);
        if (meshes == null) {
            Log.writeln(Log.ERROR, "No render batch found for shader: " + shaderName);
            return;
        }
        meshes.add(mesh);
    }

    // Loads all shaders from the shaders directory
    private static void initShaders(){
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
            ShaderProgram shader = new ShaderProgram(vertFileContents, fragFileContents);
            //Load it into the shader lookup table
            shaders.put(vertFileName, shader);
            //Add the shader to the render batch
            renderBatches.put(shader, new ArrayList<>());
            Log.writeln(Log.DEBUG, "Shader loaded: " + fragFileName);
        }
    }

    // Cleans up all shaders and meshes
    public static void cleanup() {
        for(ShaderProgram shader : shaders.values()) {
            //Clean up meshes
            for(Mesh mesh : renderBatches.get(shader)) {
                mesh.cleanup();
            }
            //Clean up shader
            shader.cleanup();
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
        int windowWidth = Engine.getWindow().width();
        int windowHeight = Engine.getWindow().height();
        float x = (p / Engine.getWindow().width()) * 2;
        float y = (p / Engine.getWindow().height()) * 2;
        return (x + y)/2;
    }

    private Renderer() {}
}