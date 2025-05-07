package lucenforge.output;

import org.lwjgl.glfw.GLFWVidMode;

import static org.lwjgl.glfw.GLFW.*;

public class Monitor {

    private String name;
    private long id;
    private int index;
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
    }

    public long id(){
        return id;
    }
    public String name() { return name; }
    public int width() {
        return width;
    }
    public int height(){
        return height;
    }
    public int index() {return index; }
    public int refreshRate() {return refreshRate;}

}
