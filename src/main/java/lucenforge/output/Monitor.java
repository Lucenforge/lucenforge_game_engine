package lucenforge.output;

import lucenforge.files.Log;
import org.lwjgl.glfw.GLFWVidMode;

import static org.lwjgl.glfw.GLFW.*;

public class Monitor {

    private static Monitor currentMonitor; // The monitor to which the window is attached

    private String name;
    private long id;
    private int index;
    private int originX, originY;
    private int width, height;
    private int refreshRate;

    public Monitor(long id, int index) {
        this.id = id;
        GLFWVidMode vidMode = glfwGetVideoMode(id);
        assert vidMode != null;
        this.width = vidMode.width();
        this.height = vidMode.height();
        this.refreshRate = vidMode.refreshRate();
        this.name = glfwGetMonitorName(id);
        this.index = index;
        // Get the monitor's origin
        int[] x = new int[1];
        int[] y = new int[1];
        glfwGetMonitorPos(id, x, y);
        this.originX = x[0];
        this.originY = y[0];
    }

    public long id(){
        return id;
    }
    public String name() { return name; }
    public int originX() {
        return originX;
    }
    public int originY() {
        return originY;
    }
    public int width() {
        return width;
    }
    public int height(){
        return height;
    }
    public int index() {return index; }
    public int refreshRate() {return refreshRate;}

    public static Monitor get(){
        return currentMonitor;
    }
    public static void set(Monitor monitor){
        currentMonitor = monitor;
    }
}
