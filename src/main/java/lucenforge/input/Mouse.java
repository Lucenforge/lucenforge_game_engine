package lucenforge.input;

import lucenforge.files.Log;
import lucenforge.misc.Tools;
import lucenforge.output.Window;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class Mouse {

    public enum Button  {
        LEFT(GLFW_MOUSE_BUTTON_LEFT),
        RIGHT(GLFW_MOUSE_BUTTON_RIGHT),
        MIDDLE(GLFW_MOUSE_BUTTON_MIDDLE),
        BUTTON_4(GLFW_MOUSE_BUTTON_4),
        BUTTON_5(GLFW_MOUSE_BUTTON_5),
        BUTTON_6(GLFW_MOUSE_BUTTON_6),
        BUTTON_7(GLFW_MOUSE_BUTTON_7),
        BUTTON_8(GLFW_MOUSE_BUTTON_8);

        private final int buttonCode;

        Button(int buttonCode) {
            this.buttonCode = buttonCode;
        }

        public int getButtonCode() {
            return buttonCode;
        }
    }
    public enum CursorMode {
        NORMAL(GLFW_CURSOR_NORMAL),
        HIDDEN(GLFW_CURSOR_HIDDEN),
        DISABLED(GLFW_CURSOR_DISABLED),
        CAPTURED(GLFW_CURSOR_CAPTURED);

        private final int mode;

        CursorMode(int mode) {
            this.mode = mode;
        }

        public int getMode() {
            return mode;
        }
    }

    public static Vector2i lastPos = new Vector2i(); // Last mouse position in pixels
    public static Vector2i pos = new Vector2i(); // Mouse position in pixels
    public static Vector2d scroll = new Vector2d(0, 0); // Mouse scroll in pixels
    private static final HashMap<Integer, Boolean> buttonStatus = new HashMap<>();

    public static void attach(Window attachedWindow) {

        // Set the mouse button callback
        glfwSetMouseButtonCallback(attachedWindow.id(), (window, button, action, mods) -> {
            buttonStatus.put(button, action == 1);
        });

        // Set the scroll callback
        glfwSetScrollCallback(attachedWindow.id(), (window, xOffset, yOffset) -> {
            scroll.set(xOffset, yOffset);
        });
    }

    public static void setCursorPos(Vector3f newPos) {
        // Convert NDC to pixel coordinates
        Vector2i pixelPos = Tools.ndcToPx(newPos);
        glfwSetCursorPos(Window.current().id(), pixelPos.x, pixelPos.y);
    }
    public static void setCursorPos(Vector2i newPos) {
        glfwSetCursorPos(Window.current().id(), newPos.x, newPos.y);
    }
    public static Vector2i getPxPos() {
        return pos != null ? pos : new Vector2i(0, 0);
    }
    public static Vector2f getNDCPos() {
        return Tools.pxToNDC(new Vector2i(pos));
    }
    public static Vector2i getPxDelta(){
        return new Vector2i(pos).sub(lastPos);
    }
    public static Vector2f getNDCDelta() {
        return Tools.pxDeltaToNDC(getPxDelta());
    }

    public static boolean isButtonPressed(Button button) {
        return buttonStatus.getOrDefault(button.getButtonCode(), false);
    }
    public static boolean isButtonReleased(Button button) {
        return !buttonStatus.getOrDefault(button.getButtonCode(), false);
    }
    public static boolean isButtonHeld(Button button) {
        return buttonStatus.getOrDefault(button.getButtonCode(), false);
    }

    public static void setCursorMode(CursorMode mode) {
        glfwSetInputMode(Window.current().id(), GLFW_CURSOR, mode.getMode());
    }

    public static void update(){
        // Update the last position
        lastPos.set(pos);
        // Get the current mouse position
        double[] x = new double[1];
        double[] y = new double[1];
        glfwGetCursorPos(Window.current().id(), x, y);
        pos.set((int) x[0], (int) y[0]);
        // Reset scroll
        scroll.set(0, 0);
    }

    private Mouse() {} // Prevent instantiation
}
