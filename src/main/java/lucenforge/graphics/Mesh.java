package lucenforge.graphics;

import lucenforge.files.Log;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

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

    private Vector3f[] normals;
    private Vector3f[] vertices;
    private Vector3i[] indices;

    private final Vector4f color = new Vector4f(1f, 0f, 1f, 1f);

    FloatBuffer mappedBuffer = null;

    public Mesh init(Vector3f[] vertices, Vector3i[] indices, Usage usage) {
        init(vertices, indices, null, usage);
        return init(usage);
    }
    public Mesh init(Vector3f[] vertices, Vector3i[] indices, Vector3f[] normals, Usage usage) {
        this.vertices = vertices;
        this.indices = indices;
        this.normals = normals;
        return init(usage);
    }
    public Mesh init(Usage usage) {
        // Generate VAO, VBO, and EBO
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        // Vertex buffer
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        // Allocate buffer space
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.length * Float.BYTES, usage.glID);
        // If usage is STREAM, use mapped buffer
        ByteBuffer mapBuffer = glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
        if (mapBuffer == null) {
            Log.writeln(Log.ERROR, "Failed to map buffer!");
        } else {
            // If usage is not STREAM, Map the buffer to a FloatBuffer
            mappedBuffer = mapBuffer.asFloatBuffer();
            mappedBuffer.put(compileVBO()).flip();
            glUnmapBuffer(GL_ARRAY_BUFFER); // <--- This is key!
            mappedBuffer = null;
        }

        // Element buffer
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, compileEBO(), GL_STATIC_DRAW);

        // Vertex attribute pointer (position only)
        int stride = normals != null? 6 : 3; // 3 floats per vertex, 3 for normals
        glVertexAttribPointer(0, stride, GL_FLOAT, false, stride * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Unbind VBO (safe), but DO NOT unbind EBO while VAO is still bound
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        return this;
    }

    public void updateVerts(Vector3f[] vertices) {
        // Update the vertex data
        this.vertices = vertices;
        // Recompile the VBO
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        // If usage is STREAM, use mapped buffer
        ByteBuffer mapBuffer = glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
        if (mapBuffer == null) {
            Log.writeln(Log.ERROR, "Failed to remap buffer during update!");
        } else {
            mappedBuffer = mapBuffer.asFloatBuffer();
            mappedBuffer.put(compileVBO()).flip();
            glUnmapBuffer(GL_ARRAY_BUFFER);
            mappedBuffer = null;
        }

        // Unbind the buffer
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void render() {
        if(vertices == null){
            Log.writeln(Log.ERROR, "Mesh not initialized; Cannot render!");
            return;
        }
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    // Compile vertices and normals from Vector3f array to float array
    private float[] compileVBO() {
        int stride = normals != null? 6 : 3; // 3 floats per vertex, 3 for normals
        float[] vertexBuffer = new float[vertices.length * stride];
        // For every vertex, add the position and normal (if present) to the buffer
        for (int i = 0; i < vertices.length; i++) {
            // Add vertex position to the buffer
            vertexBuffer[i * 3    ] = vertices[i].x;
            vertexBuffer[i * 3 + 1] = vertices[i].y;
            vertexBuffer[i * 3 + 2] = vertices[i].z;
            // If normals are present, add them to the buffer
            if (normals != null) {
                vertexBuffer[i * 6 + 3] = normals[i].x;
                vertexBuffer[i * 6 + 4] = normals[i].y;
                vertexBuffer[i * 6 + 5] = normals[i].z;
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
        return indexBuffer;
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
                String[] parts = line.replace("\n","").split(" ");
                if (parts.length == 4){
                    Log.writeln(parts[3]);
                    int x = Integer.parseInt(parts[1]) - 1; // OBJ indices are 1-based
                    int y = Integer.parseInt(parts[2]) - 1;
                    int z = Integer.parseInt(parts[3]) - 1;
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

    // Setters and Getters for Color
    public void setColor(int r, int g, int b){
        color.set(r / 255f, g / 255f, b / 255f, color.w);
    }
    public void setColor(float r, float g, float b, float a) {
        color.set(r, g, b, a);
    }
    public void setColor(int r, int g, int b, int a) {
        color.set(r / 255f, g / 255f, b / 255f, a / 255f);
    }
    public Vector4f getColor() {
        return color;
    }

    // Getters for vertices and indices
    public int getNumVerts(){
        return vertices.length / 3; // Each vertex has 3 components (x, y, z)
    }
    public int getNumFaces(){
        return indices.length / 3; // Each face is a triangle, so 3 indices per face
    }
}