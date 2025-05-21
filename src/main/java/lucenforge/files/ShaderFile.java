package lucenforge.files;

import lucenforge.graphics.GraphicsManager;
import lucenforge.graphics.shaders.Shader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

public class ShaderFile {

    // Load a shader from the shaders directory
    public static Shader loadShader(String name){
        //Load the shader from the shaders directory
        String vertFilePath = "src/main/resources/shaders/" + name + ".vert.glsl";
        String fragFilePath = "src/main/resources/shaders/" + name + ".frag.glsl";
        //Read the shader files
        String vertFileContents = FileTools.readFile(vertFilePath);
        String fragFileContents = FileTools.readFile(fragFilePath);
        //Create the shader program
        Shader shader = new Shader(name, vertFileContents, fragFileContents);
        //Load it into the shader lookup table
        GraphicsManager.masterShaders.put(name, shader);
        return shader;
    }

    private ShaderFile(){}
}
