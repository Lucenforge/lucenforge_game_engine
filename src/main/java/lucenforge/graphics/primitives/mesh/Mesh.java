package lucenforge.graphics.primitives.mesh;

import lucenforge.entity.WorldEntity;
import lucenforge.files.Log;
import lucenforge.graphics.GraphicsManager;
import lucenforge.graphics.Renderable;
import lucenforge.graphics.Texture;
import lucenforge.graphics.shaders.Shader;
import lucenforge.graphics.shaders.ShaderParameter;
import lucenforge.graphics.shaders.VertexAttributeType;
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
    // Texture
    private Texture texture;

    private Usage usage;
    private Shader shader;
    private final HashMap<String, ShaderParameter> params = new HashMap<>();

    FloatBuffer mappedBuffer = null;

    public void setTopology(ArrayList<Vertex> vertices, ArrayList<Vector3i> faces) {
        this.vertices = vertices;
        this.faces = faces;
    }

    public void init(Usage usage, Shader shader) {
        this.shader = shader;

        // Fail gracefully if no vertices are provided
        if (vertices.isEmpty()) {
            Log.writeln(Log.ERROR, "Cannot initialize mesh with no vertices.");
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
            if (hasTexCoords()) { //todo change when doing textures
                vertexBuffer[base + offset++] = vertices.get(i).texture.x; //todo change when doing textures
                vertexBuffer[base + offset++] = vertices.get(i).texture.y; //todo same ^^
            }
            if (hasNormals()) {
                vertexBuffer[base + offset++] = vertices.get(i).normal.x;
                vertexBuffer[base + offset++] = vertices.get(i).normal.y;
                vertexBuffer[base + offset++] = vertices.get(i).normal.z;
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

    private void bindVertexAttributes(int byteStride) {
        int offsetBytes = 0;

        // Position (always present)
        int posLoc = shader.getAttributeLocation(VertexAttributeType.POSITION_IN);
        glVertexAttribPointer(posLoc, 3, GL_FLOAT, false, byteStride, offsetBytes);
        glEnableVertexAttribArray(posLoc);
        offsetBytes += 3 * Float.BYTES;
        // Texture (if present)
        if (hasTexCoords()) {
            Integer texLoc = shader.getAttributeLocation(VertexAttributeType.TEXTURE_IN);
            if(texLoc != null) {
                glVertexAttribPointer(texLoc, 2, GL_FLOAT, false, byteStride, offsetBytes);
                glEnableVertexAttribArray(texLoc);
            }
            offsetBytes += 2 * Float.BYTES;
        }
        // Normal (if present)
        if (hasNormals()) {
            Integer normLoc = shader.getAttributeLocation(VertexAttributeType.NORMAL_IN);
            if(normLoc != null) {
                glVertexAttribPointer(normLoc, 3, GL_FLOAT, false, byteStride, offsetBytes);
                glEnableVertexAttribArray(normLoc);
            }
        }
    }

    private boolean hasTexCoords() {
        return vertices.get(0).texture != null;
    }

    private boolean hasNormals() {
        return vertices.get(0).normal != null;
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
        if(texture != null)
            setParam("texture0", 0);
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

        // Bind texture
        if (texture != null) {
            texture.bind();
        }

        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, eboLength, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    // Compute normals for the mesh
    public void shadeSmooth(boolean smooth){
        if (vertices == null || faces == null) {
            Log.writeln(Log.ERROR, "Mesh not initialized; Cannot compute normals!");
            return;
        }
        computeNormals(smooth, vertices, faces);
    }
    public static void computeNormals(boolean smooth, ArrayList<Vertex> vertices, ArrayList<Vector3i> faces) {
        if (!smooth) {
            // Flat shading: use existing logic
            for (Vector3i face : faces) {
                Vector3f v1 = vertices.get(face.x).position;
                Vector3f v2 = vertices.get(face.y).position;
                Vector3f v3 = vertices.get(face.z).position;

                Vector3f edge1 = new Vector3f(v2).sub(v1);
                Vector3f edge2 = new Vector3f(v3).sub(v1);
                Vector3f normal = edge1.cross(edge2).normalize();

                vertices.get(face.x).normal = new Vector3f(normal);
                vertices.get(face.y).normal = new Vector3f(normal);
                vertices.get(face.z).normal = new Vector3f(normal);
            }
            return;
        }

        // Smooth shading
        // Map from position to accumulated normal
        HashMap<Vector3f, Vector3f> normalMap = new HashMap<>(vertices.size());

        // First pass: accumulate face normals
        for (Vector3i face : faces) {
            Vertex v0 = vertices.get(face.x);
            Vertex v1 = vertices.get(face.y);
            Vertex v2 = vertices.get(face.z);

            Vector3f edge1 = new Vector3f(v1.position).sub(v0.position);
            Vector3f edge2 = new Vector3f(v2.position).sub(v0.position);
            Vector3f faceNormal = edge1.cross(edge2).normalize();

            for (Vertex v : new Vertex[] { v0, v1, v2 }) {
                Vector3f key = v.position; // assumes no two Vector3f instances represent the same point unless shared
                Vector3f acc = normalMap.computeIfAbsent(key, k -> new Vector3f());
                acc.add(faceNormal);
            }
        }

        // Second pass: normalize and assign
        for (Vertex v : vertices) {
            Vector3f key = v.position;
            v.normal = new Vector3f(normalMap.get(key)).normalize();
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

    // Shader setters and getters
    public <T> void setParam(String paramName, T value){
        if(shader == null) {
            Log.writeln(Log.ERROR, "Shader has not been set yet, skipping setParam for: " + paramName);
            return;
        }
        if (!params.containsKey(paramName)) {
            ShaderParameter paramFromShader = shader.param(paramName);
            if(paramFromShader == null) { //Warning shows up in the function above
                return;
            }
            if(!value.getClass().equals(paramFromShader.getType())){
                Log.writeln(Log.ERROR, "Type mismatch for shader parameter " + paramName + ": expected " + paramFromShader.getType() + ", got " + value.getClass());
            }else {
                params.put(paramName, new ShaderParameter(paramFromShader));
            }
        }
        ShaderParameter parameter = params.get(paramName);
        if(value instanceof Float)
            parameter.set((Float) value);
        else if(value instanceof Integer)
            parameter.set((Integer) value);
        else if(value instanceof Vector3f)
            parameter.set((Vector3f) value);
        else if(value instanceof Vector4f)
            parameter.set((Vector4f) value);
        else if(value instanceof Matrix4f)
            parameter.set((Matrix4f) value);
        else if(value instanceof ByteBuffer)
            parameter.set((ByteBuffer) value);
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
    public void setTexture(Texture texture){
        this.texture = texture;
        texture.setShader(shader);
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


    // Cleanup method
    public void cleanup() {
        if(texture != null)
            texture.cleanup();
        if (mappedBuffer != null) {
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glUnmapBuffer(GL_ARRAY_BUFFER);
        }
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
    }
}