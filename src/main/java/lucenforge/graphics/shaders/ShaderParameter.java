package lucenforge.graphics.shaders;

import lucenforge.files.Log;
import lucenforge.graphics.Texture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform4f;

public class ShaderParameter {

    public enum UniformType {
        BOOL, FLOAT, VEC3, VEC4, MAT4, SAMPLER2D
    }

    private final String name;
    private final Shader shader;
    private Object value;
    private UniformType type;

    public ShaderParameter(ShaderParameter toCopy) {
        this.name = toCopy.name;
        this.type = toCopy.type;
        this.shader = toCopy.shader;
        this.value = null;
    }

    public ShaderParameter(String name, String typeString, Shader shader){
        UniformType type = UniformType.valueOf(typeString.toUpperCase());
        this.name = name;
        this.type = type;
        this.shader = shader;
    }

    // Setters
    public void set(boolean v){
        value = v;
        type = UniformType.BOOL;
    }

    public void set(float v) {
        value = v;
        type = UniformType.FLOAT;
    }

    public void set(Vector3f v) {
        value = v;
        type = UniformType.VEC3;
    }

    public void set(Vector4f v) {
        value = v;
        type = UniformType.VEC4;
    }

    public void set(Matrix4f v) {
        value = v;
        type = UniformType.MAT4;
    }

    public void set(ByteBuffer v) {
        value = v;
        type = UniformType.SAMPLER2D;
    }

    public void pushToShader() {
        if (!isSet()) {
            Log.writeln(Log.ERROR, "Cannot push shader parameter " + name + " to the shader " + shader.name() + " because it's not set");
            return;
        }
        shader.setParam(name, this);
    }

    public void pushToOpenGL() {
        if (value == null) {
            Log.writeln(Log.ERROR, "Parameter " + name + " not set!");
            return;
        }

        int location = glGetUniformLocation(shader.id(), name);
        if (location == -1) {
            Log.writeln(Log.ERROR, "Uniform " + name + " not found in shader " + shader.name());
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            switch (type) {
                case BOOL -> {
                    boolean b = (Boolean) value;
                    glUniform1i(location, b ? 1 : 0);
                }
                case FLOAT -> glUniform1f(location, (Float) value);
                case VEC3 -> {
                    Vector3f v = (Vector3f) value;
                    glUniform3f(location, v.x, v.y, v.z);
                }
                case VEC4 -> {
                    Vector4f v = (Vector4f) value;
                    glUniform4f(location, v.x, v.y, v.z, v.w);
                }
                case MAT4 -> {
                    FloatBuffer fb = stack.mallocFloat(16);
                    ((Matrix4f) value).get(fb);
                    glUniformMatrix4fv(location, false, fb);
                }
                case SAMPLER2D -> {
                    glUniform1i(location, 0);
                }
                default -> Log.writeln(Log.ERROR, "Unknown uniform type: " + type);
            }
        }
    }

    public boolean isSet() {
        return value != null;
    }

    public String name() {
        return name;
    }
}