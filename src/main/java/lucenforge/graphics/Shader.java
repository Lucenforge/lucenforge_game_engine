package lucenforge.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final int programId;
    private final HashMap<String, Integer> uniformIDs = new HashMap<>();

    public Shader(String vertexSrc, String fragmentSrc) {
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

    public void setUniform(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(programId, name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            matrix.get(fb);
            glUniformMatrix4fv(location, false, fb);
        }
    }
    public void setUniform(String name, Vector3f v) {
        int location = glGetUniformLocation(programId, name);
        glUniform3f(location, v.x, v.y, v.z);
    }
    public void setUniform(String name, Vector4f v) {
        int location = glGetUniformLocation(programId, name);
        glUniform4f(location, v.x, v.y, v.z, v.w);
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