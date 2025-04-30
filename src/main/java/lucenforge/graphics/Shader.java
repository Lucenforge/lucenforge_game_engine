package lucenforge.graphics;

import lucenforge.files.Log;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final String name;
    private final int programId;
    private final ArrayList<String> reqUniforms = new ArrayList<>();

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

    public boolean checkUniformsSet() {
        boolean allSet = true;
        for(String uniform : reqUniforms) {
            int id = glGetUniformLocation(programId, uniform);
            if (id == -1) {
                Log.writeln(Log.ERROR, "Uniform not set: " + uniform);
                allSet = false;
            }
        }
        return allSet;
    }

    // Set uniforms
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

    // find required uniforms
    private void findReqUniforms(String vertexSrc, String fragmentSrc){
        String fullSrc = vertexSrc + "\n" + fragmentSrc;
        String[] lines = fullSrc.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("uniform")) {
                String[] parts = line.split(" ");
                if (parts.length > 2) {
                    String uniformName = parts[2].replace(";", "");
                    reqUniforms.add(uniformName);
                }
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

    public String getName() {
        return name;
    }
}