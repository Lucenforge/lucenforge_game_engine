package lucenforge.input;

import lucenforge.output.Window;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class Mouse {

    public static Vector2i pos = new Vector2i(); // Mouse position in pixels
    public static Vector2d scroll = new Vector2d(0, 0); // Mouse scroll in pixels
    private static final HashMap<Integer, Boolean> buttonStatus = new HashMap<>();

    public static void attach(Window attachedWindow) {

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

    public static Vector2i getPos() {
        return pos != null ? pos : new Vector2i(0, 0);
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

    private Mouse() {} // Prevent instantiation
}
