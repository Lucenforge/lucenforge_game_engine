package lucenforge.graphics.shaders;

import lucenforge.files.Log;

import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    //The shader's name
    private final String name;
    // The OpenGL program ID
    private final int programId;
    // A list of required uniforms (parameters) that are needed for this shader
    private final HashMap<String, ShaderParameter> reqUniforms = new HashMap<>();
    private final HashMap<VertexAttributeType, Integer> reqVertexAttributes = new HashMap<>();

    public Shader(String name, String vertexSrc, String fragmentSrc) {
        this.name = name;
        int vertexShader = compileShader(GL_VERTEX_SHADER, vertexSrc);
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentSrc);

        findRequirements(vertexSrc, fragmentSrc);

        programId = glCreateProgram();
        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader linking failed: " + glGetProgramInfoLog(programId));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    // Compile a shader of the given type (vertex or fragment)
    private int compileShader(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader compile failed: " + glGetShaderInfoLog(shader));
        }
        return shader;
    }

    // Check if all required parameters are set
    public boolean checkAndSendParametersToGPU() {
        boolean allGood = true;
        for (ShaderParameter shaderParameter : reqUniforms.values()) {
            if (shaderParameter.isSet()) {
                shaderParameter.pushToOpenGL();
            }else{
                Log.writeln(Log.ERROR, "Uniform " + shaderParameter.name() + " not set in shader " + name);
                allGood = false;
            }
        }
        return allGood;
    }

    // Check where the attribute is located
    public Integer getAttributeLocation(VertexAttributeType type) {
        return reqVertexAttributes.getOrDefault(type, null);
    }

    // Check if uniform is required
    public boolean isUniformRequired(String name) {
        return reqUniforms.containsKey(name);
    }

    // find required uniforms
    private void findRequirements(String vertexSrc, String fragmentSrc){
        String fullSrc = vertexSrc + "\n" + fragmentSrc;
        String[] lines = fullSrc.split("\n");
        for (String line : lines) {
            line = line.trim();
            // Find Uniforms
            if (line.startsWith("uniform")) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    String typeString = parts[1];
                    String uniformName = parts[2].replace(";", "");
                    UniformType type = UniformType.valueOf(typeString.toUpperCase());
                    ShaderParameter shaderParameter = new ShaderParameter(uniformName, type, this);
                    reqUniforms.put(uniformName, shaderParameter);
                }
            }

            // Find Vertex Attributes
            if (line.startsWith("layout")) {

                String[] parts = line.split(" ");
                String attributeName = parts[parts.length-1].replace(";", "");
                VertexAttributeType vertexAttributeType = VertexAttributeType.valueOf(attributeName.toUpperCase());

                int location = Integer.parseInt(line.replaceAll(" ", "").substring(line.indexOf("="),line.indexOf(")")-2));
                reqVertexAttributes.put(vertexAttributeType, location);
            }
        }
    }

    public void bind() {
        glUseProgram(programId);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public int id() {
        return programId;
    }

    public void cleanup() {
        glDeleteProgram(programId);
    }

    public String name() {
        return name;
    }

    public ShaderParameter requiredParameter(String name){
        if(reqUniforms.containsKey(name))
            return reqUniforms.get(name);
        else{
            Log.writeln(Log.WARNING, "Not retrieving " + name + " as it's not a required parameter for shader " + this.name);
            return null;
        }
    }
    public void setParam(String name, ShaderParameter param){
        if(reqUniforms.containsKey(name))
            reqUniforms.put(name, param);
        else{
            Log.writeln(Log.WARNING, "Skipping setting " + name + " as it's not a required parameter for shader " + this.name);
        }
    }
}