package lucenforge.graphics;

import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
    private final int programId;
    private HashMap<String, Integer> uniformIDs = new HashMap<>();

    public ShaderProgram(String vertexSrc, String fragmentSrc) {
        int vertexShader = compileShader(GL_VERTEX_SHADER, vertexSrc);
        int fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentSrc);

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

    public int getUniformID(String name) {
        Integer id = uniformIDs.get(name);
        if (id == null) {
            // If the uniform ID is not cached, retrieve it from OpenGL
            id = glGetUniformLocation(programId, name);
            if (id == -1) {
                throw new RuntimeException("Uniform not found: " + name);
            }
            uniformIDs.put(name, id);
        }
        return id;
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
}