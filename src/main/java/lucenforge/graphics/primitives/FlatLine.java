package lucenforge.graphics.primitives;

import lucenforge.graphics.GraphicsManager;
import lucenforge.graphics.RenderLayer;
import org.joml.Vector2f;
import org.joml.Vector2i;

public class FlatLine extends FlatQuadrilateral{

    public FlatLine(Vector2i p1, Vector2i p2, float width){
        Vector2f a = GraphicsManager.pxToRaw(p1);
        Vector2f b = GraphicsManager.pxToRaw(p2);
        width = GraphicsManager.pxToRaw(width);
        init(a, b, width);
    }

    public FlatLine(Vector2f p1, Vector2f p2, float width){
        init(p1, p2, width);
    }

    public void init(Vector2f a, Vector2f b, float width){
        // Find slope and perpendicular slope
        Vector2f direction = new Vector2f(b).sub(a);
        Vector2f perpendicular = new Vector2f(direction.y, -direction.x).normalize();
        // Calculate the offset
        Vector2f offset = new Vector2f(perpendicular).mul(width / 2.0f);
        // Calculate the four points of the line
        Vector2f p1 = new Vector2f(a).add(offset);
        Vector2f p2 = new Vector2f(a).sub(offset);
        Vector2f p3 = new Vector2f(b).sub(offset);
        Vector2f p4 = new Vector2f(b).add(offset);
        super.init(p1, p2, p3, p4);
    }
}
