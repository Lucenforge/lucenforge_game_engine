package lucenforge.input;

import lucenforge.files.Log;
import lucenforge.output.Window;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard {

    static private final HashMap<Integer, Integer> keyMap = new HashMap<>();
    static private String charBuffer = "";

    public static final int KEY_NOT_PRESSED = GLFW_RELEASE; //0
    public static final int KEY_PRESSED = GLFW_PRESS; //1
    public static final int KEY_HELD = GLFW_REPEAT; //2

    //Attach the keyboard input to a window
    public static void attach(Window attachedWindow){
        // Setup a key callback
        // key = standardized
        // scancode = platform specific
        // action = 0: released, 1: pressed, 2: held
        glfwSetKeyCallback(attachedWindow.id(), (window, key, scancode, action, mods) -> {
            //Update keymap
            if(!keyMap.containsKey(key) || keyMap.get(key) != action)
                keyMap.put(key, action);
        });

        //Set up the charBuffer callback
        glfwSetCharCallback(attachedWindow.id(), (window, codepoint) -> {
            charBuffer += Character.toChars(codepoint)[0];
        });
    }

    //Get the char buffer and clear it
    public static String popCharBuffer() {
        String buffer = charBuffer;
        charBuffer = "";
        return buffer;
    }

    //Get key status
    public static int keyStatus(String keyString){
        if(keyString.length() > 1)
            switch(keyString){
                case "ESCAPE" -> {
                    return keyStatus(GLFW_KEY_ESCAPE);
                }
                case "SPACE" -> {
                    return keyStatus(GLFW_KEY_SPACE);
                }
                case "ENTER" -> {
                    return keyStatus(GLFW_KEY_ENTER);
                }
                case "BACKSPACE" -> {
                    return keyStatus(GLFW_KEY_BACKSPACE);
                }
                case "L_SHIFT" -> {
                    return keyStatus(GLFW_KEY_LEFT_SHIFT);
                }
                case "L_CTRL" -> {
                    return keyStatus(GLFW_KEY_LEFT_CONTROL);
                }
                default -> {
                    Log.writeln(Log.ERROR, "Key not found: " + keyString);
                    return KEY_NOT_PRESSED;
                }
            }
        else
            return keyStatus((int)keyString.charAt(0));
    }
    public static int keyStatus(char keyChar){
        return keyStatus((int)keyChar);
    }
    public static int keyStatus(int key) {
        return keyMap.getOrDefault(key, KEY_NOT_PRESSED);
    }
    public static boolean isKeyPressed(int key) {
        return keyMap.containsKey(key) && keyMap.get(key) != KEY_NOT_PRESSED;
    }
    public static boolean isKeyReleased(int key) {
        return keyMap.containsKey(key) && keyMap.get(key) == KEY_NOT_PRESSED;
    }
    public static boolean isKeyHeld(int key) {
        return keyMap.containsKey(key) && keyMap.get(key) == KEY_HELD;
    }

    private Keyboard() {} // Prevent instantiation
}
