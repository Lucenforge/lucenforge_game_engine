import lucenforge.Engine;
import lucenforge.files.Properties;
import lucenforge.graphics.Mesh;
import lucenforge.graphics.Renderer;

public class Main {

    public static void main(String[] args) {

        float[] vertices = {
                0.0f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f
        };

        Engine.init();
        Renderer.addToRenderBatch("flat", new Mesh(vertices));
        Engine.start();
    }

}
