package lucenforge.output;

import lucenforge.Engine;
import lucenforge.files.Log;
import lucenforge.files.Properties;
import org.joml.Vector2i;
import org.lwjgl.glfw.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private final Monitor monitor;
    private final long windowID;
    private int width, height;
    private boolean inFocus = false;

    public Window(Monitor monitor) {
        this.width = Properties.getInt("window", "resolution_x", (int)(monitor.width()*0.75f));
        this.height = Properties.getInt("window", "resolution_y", (int)(monitor.height()*0.75f));
        this.monitor = monitor;

        //Set window properties
        boolean isResizable = Properties.getBool("window", "resizable", false);
        boolean isBorderless = Properties.getBool("window", "borderless", false);
        boolean isMaximized = Properties.getBool("window", "maximized", true);
        boolean isFullscreen = Properties.getBool("window", "fullscreen", false);
        int antiAliasingLevel = Properties.getInt("graphics", "anti-aliasing", 4);
        glfwDefaultWindowHints(); // Configure GLFW
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); //Set initial visibility
        glfwWindowHint(GLFW_MAXIMIZED, isMaximized? GLFW_TRUE : GLFW_FALSE); //Set if the window is maximized todo figure this out
        glfwWindowHint(GLFW_RESIZABLE, isResizable? GLFW_TRUE : GLFW_FALSE); //Set if the window is resizable
        glfwWindowHint(GLFW_DECORATED, isBorderless? GLFW_FALSE : GLFW_TRUE); //Set whether it has a frame or not (borderless key here)
        glfwWindowHint(GLFW_SAMPLES, antiAliasingLevel);

        String title = Properties.getString("window", "title","LucenForge Engine");
        windowID = glfwCreateWindow(width, height, title, isFullscreen? monitor.id() : NULL, NULL);
        if ( windowID == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        setIcon("src/main/resources/icon.png");

        //Set the size callback
        glfwSetWindowSizeCallback(windowID, (window, newWidth, newHeight) -> {
            this.width = newWidth;
            this.height = newHeight;
            Log.writeln("Window resized to: " + Log.TELEMETRY + newWidth + ", " + newHeight);
        });

        //Set the focus callback
        glfwSetWindowFocusCallback(windowID, (window, newIsFocused) -> {
            boolean newValue = (window == windowID)? newIsFocused : inFocus;
            Log.writeln(Log.DEBUG, "Window focus changed: " + newValue);
            inFocus = newValue;
        });

        Log.writeln(Log.SYSTEM, "Window " + title + " started on monitor " + monitor.index() + " (" + monitor.name() + ") with resolution " + width + "x" + height);
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
    // Get the current monitor
    public Monitor monitor(){
        return monitor;
    }
    // Get the primary monitor's width and height
    public static Vector2i getDim(){
        return new Vector2i(Engine.getWindow().width(),
                Engine.getWindow().height());
    }
    public static float getAspectRatio(){
        return (float)Engine.getWindow().width() / (float)Engine.getWindow().height();
    }

    public boolean isInFocus(){
        return inFocus;
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

    //Set the window icon
    public void setIcon(String path){
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
