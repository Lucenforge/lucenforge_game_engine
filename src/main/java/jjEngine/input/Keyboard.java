package jjEngine.input;

import jjEngine.output.Window;

import static org.lwjgl.glfw.GLFW.*;

public class Keyboard {

    public static void attach(Window attachedWindow){
        // Setup a key callback
        glfwSetKeyCallback(attachedWindow.id(), (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true);
        });
    }

}
