package lucenforge;

import lucenforge.files.Properties;
import lucenforge.graphics.GraphicsManager;
import lucenforge.input.Keyboard;
import lucenforge.input.Mouse;
import lucenforge.files.Log;
import lucenforge.output.Monitors;
import lucenforge.output.Window;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

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
        int monitorIndex = Properties.get("window", "monitor", 0);
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


        // Initialize OpenGL context and renderer
        glfwMakeContextCurrent(window.id()); // Make the window's context current
        GL.createCapabilities(); // Initialize OpenGL capabilities
        GraphicsManager.init(window);
    }

    public static void start(){
        //Show the window after load
        glfwShowWindow(window.id());
    }

    // Frame Loop Iteration: Begins a new frame, polls for events
    public static void frameBegin() {
        // Poll for window events
        glfwPollEvents();
    }
    // Frame Loop Iteration: Clears the screen and depth buffer
    public static void clearScreen(){ clearScreen(
            GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT,
            new Vector4f(0.1f, 0.1f, 0.1f, 1.0f));
    }
    public static void clearScreen(int clearFlags, Vector4f color) {
        // Clear the screen and depth buffer (or not)
        glClearColor(color.x, color.y, color.z, color.w);
        glClear(clearFlags);
    }
    // Frame Loop Iteration: Ends the current frame, swaps buffers
    public static void frameEnd(){
        // swap the color buffers
        glfwSwapBuffers(window.id());

        if(isShutdownRequested())
            shutdown();
    }

    public static void shutdown(){
        // 1. Cleanup rendering stuff (while OpenGL is alive)
        GraphicsManager.cleanup();

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
