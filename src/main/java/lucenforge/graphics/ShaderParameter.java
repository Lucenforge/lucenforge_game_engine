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
    private Matrix4f valueMatrix4f;
    private Vector3f valueVector3f;
    private Vector4f valueVector4f;

    public ShaderParameter(ShaderParameter toCopy){
        name = toCopy.name;
        type = toCopy.type;
        shader = toCopy.shader;
        valueVector3f = null;
        valueVector4f = null;
        valueMatrix4f = null;
    }

    public ShaderParameter(String name, String type, Shader shader) {
        this.name = name;
        this.type = type;
        this.shader = shader;
    }

    // Set uniforms
    public void set(Matrix4f v) {
        valueMatrix4f = v;
    }
    public void set(Vector3f v) {
        valueVector3f = v;
    }
    public void set(Vector4f v) {
        valueVector4f = v;
    }

    public void pushToShader(){
        if(!isSet()) {
            Log.writeln(Log.ERROR, "Cannot push shader parameter " + name + " to the shader " + shader.name() + " because it's not set");
            return;
        }
        ShaderParameter shaderParam = shader.getParam(name);
        if(valueVector3f != null)
            shaderParam.set(valueVector3f);
        else if(valueVector4f != null)
            shaderParam.set(valueVector4f);
        else if(valueMatrix4f != null)
            shaderParam.set(valueMatrix4f);
    }

    public void pushToOpenGL(){
        if(valueVector3f != null){
            Vector3f v = valueVector3f;
            if(type.equals("vec3")) {
                int location = glGetUniformLocation(shader.id(), name);
                if(location == -1) {
                    Log.writeln(Log.ERROR, "Error getting location for vec3 parameter " + name);
                    return;
                }
                glUniform3f(location, v.x, v.y, v.z);
            }else{
                Log.writeln(Log.ERROR, "parameter type mismatch: " + name + " is a " + type + " not a vec3");
            }
        }else if(valueVector4f != null){
            Vector4f v = valueVector4f;
            if(type.equals("vec4")) {
                int location = glGetUniformLocation(shader.id(), name);
                if(location == -1) {
                    Log.writeln(Log.ERROR, "Error getting location for vec4 parameter " + name);
                    return;
                }
                glUniform4f(location, v.x, v.y, v.z, v.w);
            }else{
                Log.writeln(Log.ERROR, "parameter type mismatch: " + name + " is a " + type + " not a vec4");
            }
        }else if(valueMatrix4f != null){
            Matrix4f v = valueMatrix4f;
            if(type.equals("mat4")) {
                int location = glGetUniformLocation(shader.id(), name);
                if(location == -1) {
                    Log.writeln(Log.ERROR, "Error getting location for mat4 parameter " + name);
                    return;
                }
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    FloatBuffer fb = stack.mallocFloat(16);
                    v.get(fb);
                    glUniformMatrix4fv(location, false, fb);
                }catch(Exception e) {
                    Log.writeln(Log.ERROR, "Error setting mat4 parameter " + name + ": " + e.getMessage());
                }
            }else{
                Log.writeln(Log.ERROR, "parameter type mismatch: " + name + " is a " + type + " not a mat4");
            }
        }else{
            Log.writeln(Log.ERROR, "Parameter "+name+" not set!");
        }
    }

    public boolean isSet(){
        return valueVector3f != null ||
                valueVector4f != null ||
                valueMatrix4f != null;
    }

    public String name() {
        return name;
    }

}
