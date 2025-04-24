package lucenforge;

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
    //Window properties
    public static final boolean isFullscreen = false;
    public static final boolean isBorderless = false;
    public static final boolean isResizable = true;
    public static final int[] resolution = new int[]{800, 600};

    public static void run() {
        // Initialize Log
        Log.init();

        Log.writeln(Log.SYSTEM, "LWJGL Version " + Version.getVersion() + " started!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window.id());

        // Terminate GLFW and free the error callback
        glfwTerminate();
        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        assert callback != null;
        callback.free();

        //Shut down everything
        Log.writeln(Log.SYSTEM, "Lucenforge Engine Exit");
    }

    private static void init() {
        // Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Initialize the monitors
        Monitors.init();
        // Create the window, primary for now until we can select a monitor
        window = new Window(Monitors.getPrimary());
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

    private static void loop() {
        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key
        while ( !glfwWindowShouldClose(window.id())) {
            // Poll for window events
            glfwPollEvents();

            // use shader program
            Renderer.loop();

            // swap the color buffers
            glfwSwapBuffers(window.id());
        }
    }

    // Set Options
    public static void setRes(int x, int y){

    }


    private Engine() {} // Prevent instantiation
}
