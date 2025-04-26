package lucenforge.graphics;

import org.joml.Vector4f;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.ARBVertexArrayObject.*;

public class Mesh {
    private final int vao;
    private final int vbo;
    private final int vertexCount;

    private Vector4f color = new Vector4f(1f, 0f, 1f, 1f);

    public Mesh(float[] vertices) {
        vertexCount = vertices.length / 3;

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        // Load color
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
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}