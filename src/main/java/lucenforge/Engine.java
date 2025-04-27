package lucenforge;

import lucenforge.files.Properties;
import lucenforge.input.Keyboard;
import lucenforge.input.Mouse;
import lucenforge.files.Log;
import lucenforge.output.Monitors;
import lucenforge.output.Window;
import lucenforge.graphics.Renderer;
import org.lwjgl.*;
import org.lwjgl.glfw.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Engine {

    // The window handle
    private static Window window;

    public static void init() {
        Log.writeln(Log.SYSTEM, "LWJGL Version " + Version.getVersion() + " started!");

        // Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Create the window, primary for now until we can select a monitor
        int monitorIndex = Properties.getInt("window", "monitor");
        window = new Window(Monitors.getIndex(monitorIndex));
        //Set the window position to the center
        window.setCenter();

        // Set the OpenGL viewport to match new window size on resize
        glfwSetFramebufferSizeCallback(window.id(), (win, width, height) -> {
            glViewport(0, 0, width, height);
        });

        // Set up the inputs
        Keyboard.attach(window);
        Mouse.attach(window);

        // Initialize the renderer
        Renderer.init(window);
    }

    public static void start(){
        //Show the window after load
        glfwShowWindow(window.id());
    }

    public static void nextFrame() {

        // Poll for window events
        glfwPollEvents();

        // use shader program
        Renderer.nextFrame();

        // swap the color buffers
        glfwSwapBuffers(window.id());

        if(isShutdownRequested())
            shutdown();
    }

    public static void shutdown(){
        // 1. Cleanup rendering stuff (while OpenGL is alive)
        Renderer.cleanup();

        // 2. Free GLFW callbacks
        glfwFreeCallbacks(window.id());

        // 3. Destroy the window
        glfwDestroyWindow(window.id());

        // 4. Terminate GLFW itself
        glfwTerminate();

        // 5. Free the error callback
        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null) {
            callback.free();
        }

        // 6. Final shutdown logging
        Log.writeln(Log.SYSTEM, "Lucenforge Engine Exit");

        // 7. Exit the program
        System.exit(0);
    }

    public static boolean isShutdownRequested() {
        return glfwWindowShouldClose(window.id());
    }

    public static Window getWindow() {
        return window;
    }

    private Engine() {} // Prevent instantiation
}
