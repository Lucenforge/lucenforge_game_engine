package lucenforge.output;

import static org.lwjgl.glfw.GLFW.*;

public class Monitor {

    private long id;
    private int index;
    private int width, height;
    private String name;

    public Monitor(long id, int index) {
        this.id = id;
        org.lwjgl.glfw.GLFWVidMode vidMode = glfwGetVideoMode(id);
        assert vidMode != null;
        this.width = vidMode.width();
        this.height = vidMode.height();
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

}
