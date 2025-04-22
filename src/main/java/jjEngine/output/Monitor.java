package jjEngine.output;

import static org.lwjgl.glfw.GLFW.*;

public class Monitor {

    private long id;
    private int width, height;
    private String name;

    public Monitor(long id) {
        this.id = id;
        org.lwjgl.glfw.GLFWVidMode vidMode = glfwGetVideoMode(id);
        assert vidMode != null;
        this.width = vidMode.width();
        this.height = vidMode.height();
        this.name = glfwGetMonitorName(id);
    }

    public long id(){
        return id;
    }
    public int width() {
        return width;
    }
    public int height(){
        return height;
    }

}
