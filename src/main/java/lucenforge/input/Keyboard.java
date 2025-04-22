package lucenforge.input;

import lucenforge.output.Window;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard {

    static private final HashMap<Integer, Integer> keyMap = new HashMap<>();

    public static void attach(Window attachedWindow){
        // Setup a key callback
        // key = standardized
        // scancode = platform specific
        // action = 0: released, 1: pressed, 2: held
        glfwSetKeyCallback(attachedWindow.id(), (window, key, scancode, action, mods) -> {
            if(!keyMap.containsKey(key) || keyMap.get(key) != action) {
                keyMap.put(key, action);
                System.out.println(key + ", " + scancode + ", " + action + ", " + mods);
            }

            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true);
        });
//        glfwSetCharCallback(attachedWindow.id(), (window, codepoint) -> {
//            System.out.println(Character.codepoint);
//        }); todo
    }

    public static void getKeyName(int key){
        return glfwGetKeyName(key, 0);
    }

}
