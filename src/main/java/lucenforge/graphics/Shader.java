package lucenforge.graphics;

import lucenforge.files.Log;

import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final String name;
    private final int programId;
    private final HashMap<String, ShaderParameter> reqUniforms = new HashMap<>();

    public Shader(String name, String vertexSrc, String fragmentSrc) {
        this.name = name;
        int vertexShader = compileShader(GL_VERTEX_SHADER, vertexSrc);
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentSrc);

        findReqUniforms(vertexSrc, fragmentSrc);

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

    private int compileShader(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader compile failed: " + glGetShaderInfoLog(shader));
        }
        return shader;
    }

    public boolean checkThatUniformsAreSet() {
        boolean allGood = true;
        for (ShaderParameter shaderParameter : reqUniforms.values()) {
            if (!shaderParameter.isSet()) {
                Log.writeln(Log.ERROR, "Uniform " + shaderParameter.name() + " not set in shader " + name);
                allGood = false;
            }
        }
        return allGood;
    }

    // find required uniforms
    private void findReqUniforms(String vertexSrc, String fragmentSrc){
        String fullSrc = vertexSrc + "\n" + fragmentSrc;
        String[] lines = fullSrc.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("uniform")) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    String type = parts[1];
                    String uniformName = parts[2].replace(";", "");
                    ShaderParameter shaderParameter = new ShaderParameter(uniformName, type, this);
                    reqUniforms.put(uniformName, shaderParameter);
                }
            }
        }
    }

    public ShaderParameter parameter(String name) {
        return reqUniforms.get(name);
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
}