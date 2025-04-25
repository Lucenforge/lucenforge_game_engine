package lucenforge.input;

import lucenforge.files.Log;
import lucenforge.output.Window;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class Mouse {

    public static double xPos, yPos, scrollX, scrollY;
    private static final HashMap<Integer, Boolean> buttonStatus = new HashMap<>();

    public static void attach(Window attachedWindow) {

        // Set the mouse position callback
        glfwSetCursorPosCallback(attachedWindow.id(), (window, xpos, ypos) -> {
            xPos = xpos;
            yPos = ypos;
        });

        // Set the mouse button callback
        glfwSetMouseButtonCallback(attachedWindow.id(), (window, button, action, mods) -> {
            buttonStatus.put(button, action == 1);
        });

        // Set the scroll callback
        glfwSetScrollCallback(attachedWindow.id(), (window, xoffset, yoffset) -> {
            scrollX = xoffset;
            scrollY = yoffset;
        });

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
