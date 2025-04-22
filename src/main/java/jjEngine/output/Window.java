package jjEngine.output;

import org.lwjgl.glfw.GLFWMonitorCallback;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    //Window properties (make these settable later)
    private static final boolean isFullscreen = false;
    private static final int isBorderless = GLFW_FALSE;
    private static final int isResizable = GLFW_TRUE;
    private static final int[] resolution = new int[]{800, 600};

    private Monitor monitor;
    private long windowID;
    private int width, height;

    public Window(Monitor monitor) {
        this(resolution[0], resolution[1], "jjEngine", monitor);
    }
    public Window(int width, int height, String title, Monitor monitor) {
        this.width = width;
        this.height = height;
        this.monitor = monitor;

        //Set window properties
        glfwDefaultWindowHints(); // Configure GLFW
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE); //Set visibility?? (look this up)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); //Set if the window is resizable
        glfwWindowHint(GLFW_DECORATED, GLFW_TRUE); //Set whether it has a frame or not (borderless key here)

        windowID = glfwCreateWindow(width, height, title, isFullscreen? monitor.id() : NULL, NULL);
        if ( windowID == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        //Set the size callback
        glfwSetWindowSizeCallback(windowID, (window, newWidth, newHeight) -> {
            this.width = newWidth;
            this.height = newHeight;
            System.out.println(newWidth + ", " + newHeight);
        });
    }

    //Get the monitor's id (used within GLFW)
    public long id() {
        return windowID;
    }

    //Set the window's position to the center of the monitor
    public void setCenter(){
        setPosition(monitor.width()/2 - width/2,
                monitor.height()/2 - height/2);
    }
    //Set the window's position
    public void setPosition(int x, int y) {
        glfwSetWindowPos(windowID, x, y);
    }

}
