package lucenforge.graphics;

import lucenforge.files.Log;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.*;

public class Mesh {

    private int vao; // Vertex Array Object
    private int vbo; // Vertex Buffer Object
    private int ebo; // Element Buffer Object

    float[] vertices;
    int[] indices;

    private Vector4f color = new Vector4f(1f, 0f, 1f, 1f);

    public void init(float[] vertices, int[] indices) {
        this.vertices = vertices;
        this.indices = indices;

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();

        glBindVertexArray(vao);

        // Vertex buffer
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

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
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
    }
}