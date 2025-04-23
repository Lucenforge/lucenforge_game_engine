package lucenforge.input;

import lucenforge.output.Window;

import java.sql.Struct;
import java.util.Arrays;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard {

    static private final HashMap<Integer, Integer> keyMap = new HashMap<>();
    static private String charBuffer = "";

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
    public static boolean isKeyPressed(int key) {
        return keyMap.containsKey(key) && keyMap.get(key) != GLFW_RELEASE;
    }
    public static boolean isKeyReleased(int key) {
        return keyMap.containsKey(key) && keyMap.get(key) == GLFW_RELEASE;
    }
    public static boolean isKeyHeld(int key) {
        return keyMap.containsKey(key) && keyMap.get(key) == GLFW_REPEAT;
    }
}
