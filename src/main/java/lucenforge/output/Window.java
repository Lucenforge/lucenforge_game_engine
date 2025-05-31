package lucenforge.output;

import lucenforge.Engine;
import lucenforge.files.Log;
import lucenforge.files.Properties;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private static Window currentWindow;

    private final Monitor monitor;
    private final long windowID;
    private int width, height;
    private boolean inFocus = false;
    private boolean isResizable  = false;
    private boolean isBorderless = false;
    private boolean isMaximized  = false;
    private boolean isFullscreen = false;

    public Window(Monitor monitor) {
        this.monitor = monitor;

        // Read properties for window settings & add comment about which modes are available
        String windowMode = Properties.getString("window", "mode", "windowed");
        Properties.addComment("window", "mode", "Mode types: \"windowed\", \"fullscreen\", \"borderless\"");

        if(!windowMode.equals("fullscreen") && !windowMode.equals("borderless") && !windowMode.equals("windowed")) {
            Log.writeln(Log.WARNING, "Invalid window mode: \"" + windowMode + "\", defaulting to windowed mode.");
        }
        switch (windowMode) {
            case "fullscreen" -> {
                isFullscreen = true;
                // width and height should match monitor for fullscreen
                this.width = monitor.width();
                this.height = monitor.height();
            }
            case "borderless" -> {
                isBorderless = true;
                // width and height should match monitor for borderless
                this.width = monitor.width();
                this.height = monitor.height();
            }
            case "windowed" -> {
                isResizable = true;
                isMaximized = Properties.getBool("window", "maximized", false);
                this.width = Properties.getInt("window", "resolution_x", (int) (monitor.width() * 0.75f));
                this.height = Properties.getInt("window", "resolution_y", (int) (monitor.height() * 0.75f));
            }
            default -> {
                Log.writeln(Log.WARNING, "Unknown window mode: " + windowMode + ", defaulting to windowed mode.");
            }
        }

        int antiAliasingLevel = Properties.getInt("graphics", "anti-aliasing", 4);
        glfwDefaultWindowHints(); // Configure GLFW
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); //Set initial visibility
//        glfwWindowHint(GLFW_MAXIMIZED, isMaximized? GLFW_TRUE : GLFW_FALSE); //Set if the window is maximized todo figure this out
        glfwWindowHint(GLFW_RESIZABLE, isResizable? GLFW_TRUE : GLFW_FALSE); //Set if the window is resizable
        glfwWindowHint(GLFW_DECORATED, isBorderless? GLFW_FALSE : GLFW_TRUE); //Set whether it has a frame or not (borderless key here)
        glfwWindowHint(GLFW_FOCUSED, GLFW_TRUE); // ensures it starts focused if possible
        glfwWindowHint(GLFW_SAMPLES, antiAliasingLevel);

        String title = Properties.getString("window", "title","LucenForge Engine");

        // Create the window
        windowID = glfwCreateWindow(width, height, title, isFullscreen? monitor.id() : NULL, NULL);
        if ( windowID == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        setIcon("src/main/resources/icon.png");

        //Set the size callback
        glfwSetWindowSizeCallback(windowID, (window, newWidth, newHeight) -> {
            this.width = newWidth;
            this.height = newHeight;
            Log.writeln(Log.SYSTEM, "Window resized to: " + Log.TELEMETRY + newWidth + ", " + newHeight);
        });

        //Set the focus callback
        glfwSetWindowFocusCallback(windowID, (window, newIsFocused) -> {
            inFocus = (window == windowID)? newIsFocused : inFocus;
        });

        // Center if windowed but not maximized
        if(windowMode.equals("windowed") && !isMaximized){
            setCenter();
        }else{
            setPosition(0,0); //Normally not necessary, but compensates for which monitor the window is on
        }

        // Maximize window after creation
        if(isMaximized){
            glfwMaximizeWindow(windowID);
        }

        inFocus = true; // Assume the window is in focus after creation

        Log.writeln(Log.SYSTEM, "Window " + title + " started on monitor " + monitor.index() + " (" + monitor.name() + ") with resolution " + width + "x" + height + " offset at " + monitor.originX() + ", " + monitor.originY() + ".");
    }

    //Get the monitor's id (used within GLFW)
    public long id() {
        return windowID;
    }

    // Get the window's width
    public int width() {
        return width;
    }
    // Get the window's height
    public int height() {
        return height;
    }

    public static void set(Window window){
        currentWindow = window;
    }
    public static Window current(){
        return currentWindow;
    }

    // Get the current monitor
    public Monitor monitor(){
        return monitor;
    }

    // Get the primary monitor's width and height
    public Vector2i getDim(){
        return new Vector2i(width, height);
    }
    public float getAspectRatio(){
        return (float)Window.current().width() / (float)Window.current().height();
    }

    public boolean isInFocus(){
        return inFocus;
    }

    public boolean isFullscreen(){
        return isFullscreen;
    }

    //Set the window's position to the center of the monitor
    public void setCenter(){
        setPosition(monitor.width()/2 - width/2,
                monitor.height()/2 - height/2);
    }
    //Set the window's position
    public void setPosition(int x, int y) {
        glfwSetWindowPos(windowID, x + monitor.originX(), y + monitor.originY());
    }

    //Set the window icon
    public void setIcon(String path){ //todo make this load from TextureFile
        // Load image from resources
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            // Load image (flip vertically if needed)
            STBImage.stbi_set_flip_vertically_on_load(false);

            // Load PNG file as byte buffer (update path as needed)
            ByteBuffer imageData = STBImage.stbi_load(path, width, height, channels, 4); // force RGBA

            if (imageData == null) {
                Log.writeln(Log.WARNING, "Failed to load icon: " + STBImage.stbi_failure_reason());
                return;
            }

            // Create GLFWImage and set fields
            GLFWImage icon = GLFWImage.malloc(stack);
            icon.set(width.get(0), height.get(0), imageData);

            GLFWImage.Buffer icons = GLFWImage.malloc(1, stack);
            icons.put(0, icon);

            // Apply to window
            glfwSetWindowIcon(windowID, icons);

            // Free image buffer manually
            STBImage.stbi_image_free(imageData);
        }
    }

}
