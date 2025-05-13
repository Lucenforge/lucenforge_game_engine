package lucenforge.graphics.primitives.mesh;

import lucenforge.entity.WorldEntity;
import lucenforge.files.Log;
import lucenforge.graphics.GraphicsManager;
import lucenforge.graphics.Renderable;
import lucenforge.graphics.Shader;
import lucenforge.graphics.ShaderParameter;
import org.joml.*;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.*;

public class Mesh extends WorldEntity implements Renderable {

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

    // Vertexes
    private ArrayList<Vector3f> vertices;
    private ArrayList<Vector2f> vertexTextures;
    private ArrayList<Vector3f> vertexNormals;
    // Indices
    private ArrayList<Vector3i> vertexIndices;
    private ArrayList<Vector3i> textureIndices;
    private ArrayList<Vector3i> normalIndices;

    private Usage usage;
    private Shader shader;
    private HashMap<String, ShaderParameter> params = new HashMap<>();

    FloatBuffer mappedBuffer = null;

    public Mesh init(ArrayList<Vector3f> vertices, ArrayList<Vector3i> indices, Usage usage) {
        init(vertices, indices, vertexNormals, usage);
        return init(usage);
    }
    public Mesh init(ArrayList<Vector3f> vertices, ArrayList<Vector3i> indices, ArrayList<Vector3f> normals, Usage usage) {
        this.vertices = vertices;
        this.vertexIndices = indices;
        this.vertexNormals = normals;
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
        int stride = vertexNormals != null? 6 : 3; // 3 floats per vertex, 3 for normals
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.size() * stride * Float.BYTES, usage.glID);
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
        if (vertexNormals != null) {
            glVertexAttribPointer(1, 3, GL_FLOAT, false, stride * Float.BYTES, 3 * Float.BYTES);
            glEnableVertexAttribArray(1);
        }

        // Unbind VBO (safe), but DO NOT unbind EBO while VAO is still bound
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return this;
    }

    public void updateVerts(ArrayList<Vector3f> vertices) {
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

    private void pushParamsToShader(){
        if(shader.isUniformRequired("model"))
            setParam("model", getModelMatrix());
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

        pushParamsToShader();
        if(!shader.areParametersSet()){
            Log.writeln(Log.ERROR, "Shader " + shader.name() + " has not set all required uniforms!");
            return;
        }

        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, eboLength, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    // Compile vertices and normals from Vector3f array to float array
    private float[] compileVBO() {
        int stride = vertexNormals != null? 6 : 3; // 3 floats per vertex, 3 for normals
        float[] vertexBuffer = new float[vertices.size() * stride];
        // For every vertex, add the position and normal (if present) to the buffer
        for (int i = 0; i < vertices.size(); i++) {
            int base = i * stride;
            vertexBuffer[base] = vertices.get(i).x;
            vertexBuffer[base + 1] = vertices.get(i).y;
            vertexBuffer[base + 2] = vertices.get(i).z;
            if (vertexNormals != null) {
                vertexBuffer[base + 3] = vertexNormals.get(i).x;
                vertexBuffer[base + 4] = vertexNormals.get(i).y;
                vertexBuffer[base + 5] = vertexNormals.get(i).z;
            }
        }
        return vertexBuffer;
    }

    // Compile indices from Vector3i array to int array
    private int[] compileEBO(){
        int[] indexBuffer = new int[vertexIndices.size() * 3];
        // For every index, add the vertex indices to the buffer
        for (int i = 0; i < vertexIndices.size(); i++) {
            indexBuffer[i * 3    ] = vertexIndices.get(i).x;
            indexBuffer[i * 3 + 1] = vertexIndices.get(i).y;
            indexBuffer[i * 3 + 2] = vertexIndices.get(i).z;
        }
        eboLength = indexBuffer.length;
        return indexBuffer;
    }

    // Compute normals for the mesh
    public static ArrayList<Vector3f> computeNormals(boolean smooth, ArrayList<Vector3f> vertices, ArrayList<Vector3i> indices) {
        ArrayList<Vector3f> normals = new ArrayList<>();
        for(Vector3f vertex : vertices){
            normals.add(new Vector3f());
        }

        for (Vector3i face : indices) {
            Vector3f v1 = vertices.get(face.x);
            Vector3f v2 = vertices.get(face.y);
            Vector3f v3 = vertices.get(face.z);

            Vector3f edge1 = v2.sub(v1, new Vector3f());
            Vector3f edge2 = v3.sub(v1, new Vector3f());

            Vector3f normal = edge1.cross(edge2).normalize();

            if (smooth) {
                normals.get(face.x).add(normal);
                normals.get(face.y).add(normal);
                normals.get(face.z).add(normal);
            } else {
                normals.set(face.x, new Vector3f(normal));
                normals.set(face.y, new Vector3f(normal));
                normals.set(face.z, new Vector3f(normal));
            }
        }

        if (smooth) {
            for (Vector3f n : normals) {
                if (n.lengthSquared() > 0f) {
                    n.normalize();
                }
            }
        }

        return normals;
    }

    // Get Model Matrix for rendering
    public Matrix4f getModelMatrix() {
        return new Matrix4f()
                .identity()
                .translate(position)
                .rotateY((float)Math.toRadians(rotation.y))
                .rotateZ((float)Math.toRadians(rotation.z))
                .rotateX((float)Math.toRadians(rotation.x))
                .scale(scale);
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
        if(value instanceof Float)
            parameter.set((Float) value);
        else if(value instanceof Vector3f)
            parameter.set((Vector3f) value);
        else if(value instanceof Vector4f)
            parameter.set((Vector4f) value);
        else if(value instanceof Matrix4f)
            parameter.set((Matrix4f) value);
        else{
            Log.writeln(Log.ERROR, "Type needs to be added to Mesh class: "+value);
        }
    }
    public void setShader(String shaderName){
        if (GraphicsManager.masterShaders.containsKey(shaderName)) {
            setShader(GraphicsManager.masterShaders.get(shaderName));
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
        return vertices.size(); // Each vertex has 3 components (x, y, z)
    }
    public int getNumFaces(){
        return vertexIndices.size(); // Each face is a triangle, so 3 indices per face
    }
    public ArrayList<Vector3f> vertices(){
        return vertices;
    }
    public ArrayList<Vector3i> indices(){
        return vertexIndices;
    }
    public int getNumVertexTextures(){
        return vertexTextures.size();
    }
    public int getNumVertexNormals(){
        return vertexNormals.size();
    }
}