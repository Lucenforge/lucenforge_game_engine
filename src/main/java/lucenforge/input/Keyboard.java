package lucenforge.input;

import lucenforge.files.Log;
import lucenforge.output.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard {

    private static final HashMap<Integer, Boolean> keysCurrent = new HashMap<>();
    private static final HashMap<Integer, Boolean> keysLast = new HashMap<>();

    private static long windowID;
    private static String charBuffer = "";

    // Attach keyboard input to a window
    public static void attach(Window attachedWindow) {
        windowID = attachedWindow.id();

        // Set up the character buffer callback
        glfwSetKeyCallback(windowID, (window, key, scancode, action, mods) -> {
            if(!keysCurrent.containsKey(key)) {
                keysCurrent.put(key, action != GLFW_RELEASE);
            }
        });
        glfwSetCharCallback(windowID, (window, codepoint) -> {
            charBuffer += Character.toChars(codepoint)[0];
        });
    }

    // Must be called once per frame before checking input
    public static void update() {
        keysLast.putAll(keysCurrent);
        keysCurrent.replaceAll((key, value) -> glfwGetKey(windowID, key) == GLFW_PRESS);
    }

    // Get the char buffer and clear it
    public static String popCharBuffer() {
        String buffer = charBuffer;
        charBuffer = "";
        return buffer;
    }

    // Query key state by int keycode
    public static boolean isPressed(int key) {
        return keysCurrent.getOrDefault(key, false) && !keysLast.getOrDefault(key, false);
    }

    public static boolean isHeld(int key) {
        return keysCurrent.getOrDefault(key, false);
    }

    public static boolean isReleased(int key) {
        return !keysCurrent.getOrDefault(key, false) && keysLast.getOrDefault(key, false);
    }

    // Query by key name (e.g., "A", "SPACE", "ENTER")
    public static boolean isPressed(String keyName) {
        return isPressed(getKeyCode(keyName));
    }

    public static boolean isHeld(String keyName) {
        return isHeld(getKeyCode(keyName));
    }

    public static boolean isReleased(String keyName) {
        return isReleased(getKeyCode(keyName));
    }

    private static int getKeyCode(String keyString) {
        keyString = keyString.toUpperCase();
        if (keyString.length() == 1) {
            return keyString.charAt(0); // e.g. "A" -> 65
        }
        return switch (keyString) {
            case "ESCAPE" -> GLFW_KEY_ESCAPE;
            case "SPACE" -> GLFW_KEY_SPACE;
            case "ENTER" -> GLFW_KEY_ENTER;
            case "BACKSPACE" -> GLFW_KEY_BACKSPACE;
            case "L_SHIFT" -> GLFW_KEY_LEFT_SHIFT;
            case "R_SHIFT" -> GLFW_KEY_RIGHT_SHIFT;
            case "L_CTRL" -> GLFW_KEY_LEFT_CONTROL;
            case "R_CTRL" -> GLFW_KEY_RIGHT_CONTROL;
            case "F1" -> GLFW_KEY_F1;
            case "F2" -> GLFW_KEY_F2;
            case "F3" -> GLFW_KEY_F3;
            // Add more as needed
            default -> {
                Log.writeln(Log.ERROR, "Key not recognized: " + keyString);
                yield -1;
            }
        };
    }

    private Keyboard() {} // Prevent instantiation
}
