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
    private ArrayList<Vertex> vertices;
    // Indices
    private ArrayList<Vector3i> faces;

    private Usage usage;
    private Shader shader;
    private final HashMap<String, ShaderParameter> params = new HashMap<>();

    FloatBuffer mappedBuffer = null;

    public Mesh init(ArrayList<Vertex> vertices, ArrayList<Vector3i> indices, Usage usage) {
        this.vertices = vertices;
        this.faces = indices;
        return init(usage);
    }
    public Mesh init(Usage usage) {

        // Fail gracefully if no vertices are provided
        if (vertices.isEmpty()) {
            Log.writeln(Log.ERROR, "Cannot initialize mesh with no vertices.");
            return this;
        }

        this.usage = usage;

        // Generate VAO, VBO, and EBO
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        // Vertex buffer
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        // Allocate buffer space
        Vertex firstVertex = vertices.get(0);
        int floatStride = firstVertex.getFloatStride(); // 3 floats per vertex, 3 for normals
        int byteStride = floatStride * Float.BYTES;
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.size() * byteStride, usage.glID);
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

        bindVertexAttributes(byteStride);

        // Unbind VBO (safe), but DO NOT unbind EBO while VAO is still bound
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return this;
    }

    private void bindVertexAttributes(int byteStride) {
        int offsetBytes = 0;
        int attribIndex = 0;

        // Position (always present)
        glVertexAttribPointer(attribIndex, 3, GL_FLOAT, false, byteStride, offsetBytes);
        glEnableVertexAttribArray(attribIndex++);
        offsetBytes += 3 * Float.BYTES;

        if (vertices.get(0).texture != null) {
            glVertexAttribPointer(attribIndex, 2, GL_FLOAT, false, byteStride, offsetBytes);
            glEnableVertexAttribArray(attribIndex++);
            offsetBytes += 2 * Float.BYTES;
        }

        if (vertices.get(0).normal != null) {
            glVertexAttribPointer(attribIndex, 3, GL_FLOAT, false, byteStride, offsetBytes);
            glEnableVertexAttribArray(attribIndex++);
            offsetBytes += 3 * Float.BYTES;
        }

        if(byteStride != offsetBytes){
            Log.writeln(Log.WARNING, "Byte offset (" + offsetBytes + ") "
                    + "doesn't equal byte stride (" + byteStride + ") in bindVertexAttributes in the Mesh class");
        }
    }

    public void updateVerts(ArrayList<Vertex> vertices) {
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
        int stride = vertices.get(0).getFloatStride();
        float[] vertexBuffer = new float[vertices.size() * stride];
        // For every vertex, add the position and normal (if present) to the buffer
        for (int i = 0; i < vertices.size(); i++) {
            int base = i * stride;
            int offset = 0;
            vertexBuffer[base + offset++] = vertices.get(i).position.x;
            vertexBuffer[base + offset++] = vertices.get(i).position.y;
            vertexBuffer[base + offset++] = vertices.get(i).position.z;
            if (vertices.get(i).texture != null) {
                vertexBuffer[base + offset++] = vertices.get(i).texture.x;
                vertexBuffer[base + offset++] = vertices.get(i).texture.y;
            }
            if (vertices.get(i).normal != null) {
                vertexBuffer[base + offset++] = vertices.get(i).normal.x;
                vertexBuffer[base + offset++] = vertices.get(i).normal.y;
                vertexBuffer[base + offset  ] = vertices.get(i).normal.z;
            }
        }
        return vertexBuffer;
    }

    // Compile indices from Vector3i array to int array
    private int[] compileEBO(){
        int[] indexBuffer = new int[faces.size() * 3];
        // For every index, add the vertex indices to the buffer
        for (int i = 0; i < faces.size(); i++) {
            Vector3i face = faces.get(i);
            indexBuffer[i * 3    ] = face.x;
            indexBuffer[i * 3 + 1] = face.y;
            indexBuffer[i * 3 + 2] = face.z;
        }
        eboLength = indexBuffer.length;
        return indexBuffer;
    }

    // Compute normals for the mesh
    public void computeNormals(boolean smooth) {
        ArrayList<Vector3f> normals = new ArrayList<>();
        for(Vertex ignored : vertices){
            normals.add(new Vector3f());
        }

        for (Vector3i face : faces) {
            Vector3f v1 = vertices.get(face.x).position;
            Vector3f v2 = vertices.get(face.y).position;
            Vector3f v3 = vertices.get(face.z).position;

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

        for(int i = 0; i < vertices.size(); i++){
            vertices.get(i).normal = new Vector3f(normals.get(i).normalize());
        }
    }

    // Get Model Matrix for rendering
    public Matrix4f getModelMatrix() {
        return new Matrix4f()
                .identity()
                .translate(position())
                .rotateY((float)Math.toRadians(rotation().y))
                .rotateZ((float)Math.toRadians(rotation().z))
                .rotateX((float)Math.toRadians(rotation().x))
                .scale(scale());
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
        if(shader == null) {
            Log.writeln(Log.ERROR, "Shader has not been set yet, skipping setParam for: " + paramName);
            return;
        }
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
    public ArrayList<Vertex> vertices(){
        return vertices;
    }
    public ArrayList<Vector3i> faces(){
        return faces;
    }
    public void setVertices(ArrayList<Vertex> vertices){
        this.vertices = vertices;
    }
    public void setFaces(ArrayList<Vector3i> faces){
        this.faces = faces;
    }
}