package lucenforge.misc;

import lucenforge.output.Window;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class Tools {

    public static Vector3f toVector3f(Vector2f v) {
        return new Vector3f(v.x, v.y, 0.0f);
    }

    // Convert pixel coordinates to normalized device coordinates
    public static Vector2f pxToNDC(Vector2i p) {
        float x = ((float) p.x / Window.current().getDim().x) * 2.0f - 1.0f;
        x *= Window.current().getAspectRatio();
        float y = 1.0f - ((float) p.y / Window.current().getDim().y) * 2.0f;
        return new Vector2f(x, y);
    }
    // Convert pixel delta to normalized device coordinate delta
    public static Vector2f pxDeltaToNDC(Vector2i p) {
        float x = ((float) p.x / Window.current().getDim().x) * 2.0f;
        x *= Window.current().getAspectRatio();
        float y = - ((float) p.y / Window.current().getDim().y) * 2.0f;
        return new Vector2f(x, y);
    }
    // Convert 1 dimensional pixel size to normalized device coordinate size
    public static float pxToNDC(int p){
        return ((float) p / Window.current().getDim().y) * 2;
    }
    // Convert normalized device coordinates to pixel coordinates
    public static Vector2i ndcToPx(Vector3f p){
        int x = (int) ((p.x + 1) / 2 * Window.current().getDim().x);
        int y = (int) ((1 - p.y) / 2 * Window.current().getDim().y);
        return new Vector2i(x, y);
    }

}
