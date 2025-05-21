package lucenforge.graphics.primitives;

import lucenforge.graphics.GraphicsManager;
import org.joml.Vector2i;
import org.joml.Vector3f;


public class Line extends Quadrilateral {

    Vector3f p1, p2;
    float width;

    public Line(){}
    public Line(int width, Vector3f p1, Vector3f p2){
        set(width, p1, p2);
    }
    public Line(float width, Vector3f p1, Vector3f p2){
        set(width, p1, p2);
    }

    public void set(int width, Vector3f p1, Vector3f p2){
        this.p1 = p1;
        this.p2 = p2;
        this.width = GraphicsManager.pxToNDC(width);
        Vector3f[] p = calculatePoints(p1, p2, this.width);
        super.setCorners(p[0], p[1], p[2], p[3]);
    }
    public void set(float width, Vector3f p1, Vector3f p2){
        this.p1 = p1;
        this.p2 = p2;
        this.width = width;
        Vector3f[] p = calculatePoints(p1, p2, width);
        super.setCorners(p[0], p[1], p[2], p[3]);
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
