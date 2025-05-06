package lucenforge.input;

import lucenforge.Engine;
import lucenforge.graphics.GraphicsManager;
import lucenforge.output.Window;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class Mouse {

    private static Window window; // The window to which the mouse is attached

    public static Vector2i lastPos = new Vector2i(); // Last mouse position in pixels
    public static Vector2i pos = new Vector2i(); // Mouse position in pixels
    public static Vector2d scroll = new Vector2d(0, 0); // Mouse scroll in pixels
    private static final HashMap<Integer, Boolean> buttonStatus = new HashMap<>();

    public static void attach(Window attachedWindow) {
        window = attachedWindow;

        // Set the mouse position callback
        glfwSetCursorPosCallback(attachedWindow.id(), (window, x_pos, y_pos) -> {
            pos = new Vector2i((int) x_pos, (int) y_pos);
        });

        // Set the mouse button callback
        glfwSetMouseButtonCallback(attachedWindow.id(), (window, button, action, mods) -> {
            buttonStatus.put(button, action == 1);
        });

        // Set the scroll callback
        glfwSetScrollCallback(attachedWindow.id(), (window, xOffset, yOffset) -> {
            scroll = new Vector2d(xOffset, yOffset);
        });
    }

    public static void setCursorPos(Vector2i newPos) {
        lastPos = pos;
        glfwSetCursorPos(Engine.getWindow().id(), newPos.x, newPos.y);
    }
    public static Vector2i getPxPos() {
        return pos != null ? pos : new Vector2i(0, 0);
    }
    public static Vector3f getNDCPos() {
        return GraphicsManager.pxToNDC(pos);
    }
    public static Vector2i getDelta(){
        Vector2i delta = new Vector2i(pos);
        delta.sub(lastPos);
        lastPos = pos;
        return delta;
    }

    public static boolean isButtonPressed(int button) {
        return buttonStatus.getOrDefault(button, false);
    }
    public static boolean isButtonReleased(int button) {
        return !buttonStatus.getOrDefault(button, false);
    }
    public static boolean isButtonHeld(int button) {
        return buttonStatus.getOrDefault(button, false);
    }

    public static void setVisible(boolean visible) {
        glfwSetInputMode(Engine.getWindow().id(), GLFW_CURSOR, visible ? GLFW_CURSOR_NORMAL : GLFW_CURSOR_HIDDEN);
    }

    private Mouse() {} // Prevent instantiation
}
