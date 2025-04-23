package lucenforge.output;

import org.lwjgl.glfw.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    //Window properties (make these settable later)
    private static final boolean isFullscreen = false;
    private static final boolean isBorderless = false;
    private static final boolean isResizable = true;
    private static final int[] resolution = new int[]{800, 600};

    private Monitor monitor;
    private long windowID;
    private int width, height;

    public Window(Monitor monitor) {
        this(resolution[0], resolution[1], "lucenforge", monitor);
    }
    public Window(int width, int height, String title, Monitor monitor) {
        this.width = width;
        this.height = height;
        this.monitor = monitor;

        //Set window properties
        glfwDefaultWindowHints(); // Configure GLFW
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE); //Set visibility?? (look this up)
        glfwWindowHint(GLFW_RESIZABLE, isResizable? GLFW_TRUE : GLFW_FALSE); //Set if the window is resizable
        glfwWindowHint(GLFW_DECORATED, isBorderless? GLFW_FALSE : GLFW_TRUE); //Set whether it has a frame or not (borderless key here)

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
                throw new RuntimeException("Failed to load icon: " + STBImage.stbi_failure_reason());
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
