package lucenforge.graphics;

import lucenforge.files.Log;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform4f;

public class ShaderParameter {

    private final String name;
    private final String type;
    private final Shader shader;
    private boolean set = false;

    public ShaderParameter(String name, String type, Shader shader) {
        this.name = name;
        this.type = type;
        this.shader = shader;
    }

    // Set uniforms
    public void set(Matrix4f matrix) {
        if(type.equals("mat4")) {
            int location = glGetUniformLocation(shader.id(), name);
            if(location == -1) {
                Log.writeln(Log.ERROR, "Error getting location for mat4 parameter " + name);
                return;
            }
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FloatBuffer fb = stack.mallocFloat(16);
                matrix.get(fb);
                glUniformMatrix4fv(location, false, fb);
                set = true;
            }catch(Exception e) {
                Log.writeln(Log.ERROR, "Error setting mat4 parameter " + name + ": " + e.getMessage());
            }
        }else{
            Log.writeln(Log.ERROR, "parameter type mismatch: " + name + " is a " + type + " not a mat4");
        }
    }
    public void set(Vector3f v) {
        if(type.equals("vec3")) {
            int location = glGetUniformLocation(shader.id(), name);
            if(location == -1) {
                Log.writeln(Log.ERROR, "Error getting location for vec3 parameter " + name);
                return;
            }
            glUniform3f(location, v.x, v.y, v.z);
            set = true;
        }else{
            Log.writeln(Log.ERROR, "parameter type mismatch: " + name + " is a " + type + " not a vec3");
        }
    }
    public void set(Vector4f v) {
        if(type.equals("vec4")) {
            int location = glGetUniformLocation(shader.id(), name);
            if(location == -1) {
                Log.writeln(Log.ERROR, "Error getting location for vec4 parameter " + name);
                return;
            }
            glUniform4f(location, v.x, v.y, v.z, v.w);
            set = true;
        }else{
            Log.writeln(Log.ERROR, "parameter type mismatch: " + name + " is a " + type + " not a vec4");
        }
    }

    public boolean isSet(){
        return set;
    }

    public String name() {
        return name;
    }

}
