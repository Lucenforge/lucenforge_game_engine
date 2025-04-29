package lucenforge.graphics.primitives;

import lucenforge.files.Log;
import lucenforge.graphics.GraphicsManager;
import org.joml.Vector2f;
import org.joml.Vector2i;


public class FlatLine extends FlatQuadrilateral{

    Vector2f p1, p2;
    float width;

    public FlatLine(Vector2i p1, Vector2i p2, int width, Usage usage){
        Vector2f a = GraphicsManager.pxToNDC(p1);
        Vector2f b = GraphicsManager.pxToNDC(p2);
        float widthNDC = GraphicsManager.pxToNDC(width);
        Log.writeln(Log.DEBUG, "FlatLine: p1 = " + a + ", p2 = " + b + ", width = " + this.width);
        init(a, b, widthNDC, usage);
    }

    public FlatLine(float width, Usage usage){
        this(new Vector2f(0, 0), new Vector2f(0, 0), width, usage);
    }

    public FlatLine(Vector2f p1, Vector2f p2, float width, Usage usage){
        init(p1, p2, width, usage);
    }

    public void init(Vector2f p1, Vector2f p2, float width, Usage usage){
        this.p1 = p1;
        this.p2 = p2;
        this.width = width;
        Vector2f[] p = calculatePoints(p1, p2, width);
        super.init(p[0], p[1], p[2], p[3], usage);
    }

    public void update(Vector2f p1, Vector2f p2) {
        this.p1 = p1;
        this.p2 = p2;
        Vector2f[] points = calculatePoints(p1, p2, width);
        super.update(points[0], points[1], points[2], points[3]);
    }

    public void update(Vector2i p1, Vector2i p2) {
        Vector2f a = GraphicsManager.pxToNDC(p1);
        Vector2f b = GraphicsManager.pxToNDC(p2);
        Vector2f[] p = calculatePoints(a, b, width); // Assuming width of 1 pixel
        super.update(p[0], p[1], p[2], p[3]);
    }

    private Vector2f[] calculatePoints(Vector2f a, Vector2f b, float width) {
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
        return new Vector2f[]{p1, p2, p3, p4};
    }
}
