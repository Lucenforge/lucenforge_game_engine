package lucenforge.graphics;

import lucenforge.files.Log;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.*;

public class Mesh {

    public enum Usage {
        STATIC (GL_STATIC_DRAW ),
        DYNAMIC(GL_DYNAMIC_DRAW),
        STREAM (GL_STREAM_DRAW );

        public final int glID;
        Usage(int glID) { this.glID = glID; }
    }

    private int vao; // Vertex Array Object
    private int vbo; // Vertex Buffer Object
    private int ebo; // Element Buffer Object
    private int eboLength;

    public Vector3f[] normals; //todo make private!
    private Vector3f[] vertices;
    private Vector3i[] indices;
    private Usage usage;
    private Shader shader;
    private HashMap<String, ShaderParameter> params = new HashMap<>();

    FloatBuffer mappedBuffer = null;

    public Mesh init(Vector3f[] vertices, Vector3i[] indices, Usage usage) {
        init(vertices, indices, normals, usage);
        return init(usage);
    }
    public Mesh init(Vector3f[] vertices, Vector3i[] indices, Vector3f[] normals, Usage usage) {
        this.vertices = vertices;
        this.indices = indices;
        this.normals = normals;
        return init(usage);
    }
    public Mesh init(Usage usage) {
        this.usage = usage;

        // Generate VAO, VBO, and EBO
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        // Vertex buffer
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        // Allocate buffer space
        int stride = normals != null? 6 : 3; // 3 floats per vertex, 3 for normals
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.length * stride * Float.BYTES, usage.glID);
        // If usage is STREAM, use mapped buffer
        if (usage == Usage.STREAM) {
            ByteBuffer mapBuffer = glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
            if (mapBuffer == null) {
                Log.writeln(Log.ERROR, "Failed to map buffer!");
            } else {
                mappedBuffer = mapBuffer.asFloatBuffer();
                mappedBuffer.put(compileVBO()).flip();
                glUnmapBuffer(GL_ARRAY_BUFFER);
                mappedBuffer = null;
            }
        } else {
            glBufferSubData(GL_ARRAY_BUFFER, 0, compileVBO());
        }


        // Element buffer
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, compileEBO(), GL_STATIC_DRAW);

        // Vertex attribute pointer (position only)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        // Vertex attribute pointer (normals)
        if (normals != null) {
            glVertexAttribPointer(1, 3, GL_FLOAT, false, stride * Float.BYTES, 3 * Float.BYTES);
            glEnableVertexAttribArray(1);
        }

        // Unbind VBO (safe), but DO NOT unbind EBO while VAO is still bound
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return this;
    }

    public void updateVerts(Vector3f[] vertices) {
        if (vbo == 0 || vertices == null) {
            Log.writeln(Log.ERROR, "updateVerts called before init!");
            return;
        }
        // Update the vertex data
        this.vertices = vertices;
        // Recompile the VBO
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        // If usage is STREAM, use mapped buffer
        if (usage == Usage.STREAM) {
            ByteBuffer mapBuffer = glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
            if (mapBuffer == null) {
                Log.writeln(Log.ERROR, "Failed to map buffer!");
            } else {
                mappedBuffer = mapBuffer.asFloatBuffer();
                mappedBuffer.put(compileVBO()).flip();
                glUnmapBuffer(GL_ARRAY_BUFFER);
                mappedBuffer = null;
            }
        } else {
            glBufferSubData(GL_ARRAY_BUFFER, 0, compileVBO());
        }

        // Unbind the buffer
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void pushParamsToShader(){
        //Push uniforms (parameters)
        for(ShaderParameter param : params.values()){
            param.pushToShader();
        }
    }

    public void render() {
        if(vertices == null){
            Log.writeln(Log.ERROR, "Mesh not initialized; Cannot render!");
            return;
        }

        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, eboLength, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    // Compile vertices and normals from Vector3f array to float array
    private float[] compileVBO() {
        int stride = normals != null? 6 : 3; // 3 floats per vertex, 3 for normals
        float[] vertexBuffer = new float[vertices.length * stride];
        // For every vertex, add the position and normal (if present) to the buffer
        for (int i = 0; i < vertices.length; i++) {
            int base = i * stride;
            vertexBuffer[base]     = vertices[i].x;
            vertexBuffer[base + 1] = vertices[i].y;
            vertexBuffer[base + 2] = vertices[i].z;
            if (normals != null) {
                vertexBuffer[base + 3] = normals[i].x;
                vertexBuffer[base + 4] = normals[i].y;
                vertexBuffer[base + 5] = normals[i].z;
            }
        }
        return vertexBuffer;
    }

    // Compile indices from Vector3i array to int array
    private int[] compileEBO(){
        int[] indexBuffer = new int[indices.length * 3];
        // For every index, add the vertex indices to the buffer
        for (int i = 0; i < indices.length; i++) {
            indexBuffer[i * 3    ] = indices[i].x;
            indexBuffer[i * 3 + 1] = indices[i].y;
            indexBuffer[i * 3 + 2] = indices[i].z;
        }
        eboLength = indexBuffer.length;
        return indexBuffer;
    }

    // Compute normals for the mesh todo check as this might not fully be correct, though that could be the shader
    public void computeNormals(boolean smooth) {
        if (normals != null) {
            Log.writeln(Log.WARNING, "Normals already computed, overwriting");
        }

        normals = new Vector3f[vertices.length];
        for (int i = 0; i < normals.length; i++) {
            normals[i] = new Vector3f();
        }

        for (Vector3i face : indices) {
            Vector3f v1 = vertices[face.x];
            Vector3f v2 = vertices[face.y];
            Vector3f v3 = vertices[face.z];

            Vector3f edge1 = v2.sub(v1, new Vector3f());
            Vector3f edge2 = v3.sub(v1, new Vector3f());

            Vector3f normal = edge1.cross(edge2).normalize();

            if (smooth) {
                normals[face.x].add(normal);
                normals[face.y].add(normal);
                normals[face.z].add(normal);
            } else {
                normals[face.x] = new Vector3f(normal);
                normals[face.y] = new Vector3f(normal);
                normals[face.z] = new Vector3f(normal);
            }
        }

        if (smooth) {
            for (Vector3f n : normals) {
                n.normalize();
            }
        }
    }


    // Parse mesh data from an obj string
    // Only supports simple v and f right now, no normals or textures
    public void parseOBJ(String fileContents) {
        ArrayList<Vector3f> vertexList = new ArrayList<>();
        ArrayList<Vector3i> indexList = new ArrayList<>();
        String[] lines = fileContents.split("\n");
        for (String line : lines) {
            if (line.startsWith("v ")) {
                String[] parts = line.split(" ");
                if (parts.length == 4) {
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float z = Float.parseFloat(parts[3]);
                    vertexList.add(new Vector3f(x, y, z));
                }else{
                    Log.writeln(Log.WARNING, "Invalid vertex line: " + line + "; Expected format: v x y z");
                }
            } else if (line.startsWith("f ")) {
                String[] parts = line.replace("\r","").split(" ");
                if (parts.length == 4){
                    //todo include texture and normal data later; This will change
                    int x = Integer.parseInt(parts[1].split("/")[0]) - 1;
                    int y = Integer.parseInt(parts[2].split("/")[0]) - 1;
                    int z = Integer.parseInt(parts[3].split("/")[0]) - 1;
                    indexList.add(new Vector3i(x, y, z));
                }else{
                    Log.writeln(Log.WARNING, "Invalid face line: " + line + "; Expected format: f v1 v2 v3");
                }
            }
        }
        vertices = new Vector3f[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            vertices[i] = vertexList.get(i);
        }
        indices = new Vector3i[indexList.size()];
        for (int i = 0; i < indexList.size(); i++) {
            indices[i] = indexList.get(i);
        }
        computeNormals(false);
    }

    // Cleanup method
    public void cleanup() {
        if (mappedBuffer != null) {
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glUnmapBuffer(GL_ARRAY_BUFFER);
        }
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
    }

    // Shader setters and getters
    public <T> void setParam(String paramName, T value){
        if (!params.containsKey(paramName)) {
            ShaderParameter paramFromShader = shader.getParam(paramName);
            if(paramFromShader == null) { //Warning shows up in the function above
                return;
            }
            params.put(paramName, new ShaderParameter(paramFromShader));
        }
        ShaderParameter parameter = params.get(paramName);
        if(value instanceof Matrix4f)
            parameter.set((Matrix4f) value);
        else if(value instanceof Vector3f)
            parameter.set((Vector3f) value);
        else if(value instanceof Vector4f)
            parameter.set((Vector4f) value);
        else{
            Log.writeln(Log.ERROR, "Type needs to be added to ShaderParameter class: "+value);
        }
    }
    public void setShader(String shaderName){
        if (GraphicsManager.masterShaders.containsKey(shaderName)) {
            shader = GraphicsManager.masterShaders.get(shaderName);
        } else {
            Log.writeln(Log.ERROR, "Shader not found in master lookup: " + shaderName);
        }
    }
    public void setShader(Shader shader){
        this.shader = shader;
    }
    public Shader shader(){
        return shader;
    }

    // Getters for vertices and indices
    public int getNumVerts(){
        return vertices.length; // Each vertex has 3 components (x, y, z)
    }
    public int getNumFaces(){
        return indices.length; // Each face is a triangle, so 3 indices per face
    }
}