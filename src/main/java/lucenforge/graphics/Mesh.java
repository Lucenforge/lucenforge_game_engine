package lucenforge.graphics;

import lucenforge.files.Log;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

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

    private float[] vertices;
    private int[] indices;

    private final Vector4f color = new Vector4f(1f, 0f, 1f, 1f);

    FloatBuffer mappedBuffer = null;

    public void init(float[] vertices, int[] indices, Usage usage) {
        this.vertices = vertices;
        this.indices = indices;

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        // Vertex buffer
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.length * Float.BYTES, usage.glID);
        ByteBuffer mapBuffer = glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
        if (mapBuffer == null) {
            Log.writeln(Log.ERROR, "Failed to map buffer!");
        } else {
            mappedBuffer = mapBuffer.asFloatBuffer();
            mappedBuffer.put(vertices).flip();
            glUnmapBuffer(GL_ARRAY_BUFFER); // <--- This is key!
            mappedBuffer = null;
        }


        // Element buffer
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Vertex attribute pointer (position only)
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Unbind VBO (safe), but DO NOT unbind EBO while VAO is still bound
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void update(float[] vertices) {
        // Replace the vertices in the VBO
        this.vertices = vertices;
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        // If usage is STREAM, use mapped buffer
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        ByteBuffer mapBuffer = glMapBuffer(GL_ARRAY_BUFFER, GL_WRITE_ONLY);
        if (mapBuffer == null) {
            Log.writeln(Log.ERROR, "Failed to remap buffer during update!");
        } else {
            mappedBuffer = mapBuffer.asFloatBuffer();
            mappedBuffer.put(vertices).flip();
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
}