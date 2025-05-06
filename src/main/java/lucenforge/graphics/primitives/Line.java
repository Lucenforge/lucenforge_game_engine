package lucenforge.graphics.primitives;

import lucenforge.files.Log;
import lucenforge.graphics.GraphicsManager;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;


public class Line extends Quadrilateral {

    Vector3f p1, p2;
    float width;

    public Line(){}

    public Line(Vector3i p1, Vector3i p2, int width, Usage usage){
        Vector3f a = GraphicsManager.pxToNDC(p1);
        Vector3f b = GraphicsManager.pxToNDC(p2);
        float widthNDC = GraphicsManager.pxToNDC(width);
        Log.writeln(Log.DEBUG, "FlatLine: p1 = " + a + ", p2 = " + b + ", width = " + this.width);
        init(a, b, widthNDC, usage);
    }

    public Line(float width, Usage usage){
        init(new Vector3f(), new Vector3f(), width, usage);
    }

    public Line(Vector3f p1, Vector3f p2, float width, Usage usage){
        init(p1, p2, width, usage);
    }

    public void init(Vector3f p1, Vector3f p2, float width, Usage usage){
        this.p1 = p1;
        this.p2 = p2;
        this.width = width;
        Vector3f[] p = calculatePoints(p1, p2, width);
        super.init(p[0], p[1], p[2], p[3], usage);
    }

    public void update(Vector3f p1, Vector3f p2) {
        this.p1 = p1;
        this.p2 = p2;
        Vector3f[] points = calculatePoints(p1, p2, width);
        super.update(points[0], points[1], points[2], points[3]);
    }

    public void update(Vector2i p1, Vector2i p2) {
        Vector3f a = GraphicsManager.pxToNDC(p1);
        Vector3f b = GraphicsManager.pxToNDC(p2);
        Vector3f[] p = calculatePoints(a, b, width); // Assuming width of 1 pixel
        super.update(p[0], p[1], p[2], p[3]);
    }

    private Vector3f[] calculatePoints(Vector3f a, Vector3f b, float width) {
        // Find slope and perpendicular slope
        Vector3f direction = new Vector3f(b).sub(a);
        Vector3f perpendicular = new Vector3f(direction.y, -direction.x, 0).normalize();
        // Calculate the offset
        Vector3f offset = new Vector3f(perpendicular).mul(width / 2.0f);
        // Calculate the four points of the line
        Vector3f p1 = new Vector3f(a).add(offset);
        Vector3f p2 = new Vector3f(a).sub(offset);
        Vector3f p3 = new Vector3f(b).sub(offset);
        Vector3f p4 = new Vector3f(b).add(offset);
        return new Vector3f[]{p1, p2, p3, p4};
    }
}
