package lucenforge.output;

import org.lwjgl.*;

import static org.lwjgl.glfw.GLFW.*;

public class Monitors {

    private static Monitor[] monitors;

    //Initialize the Monitor objects and populates the monitors array
    public static void checkInit(){
        if(monitors != null)
            return;
        PointerBuffer monitorsBuffer = glfwGetMonitors();
        assert monitorsBuffer != null;
        int numMonitors = monitorsBuffer.remaining();
        monitors = new Monitor[numMonitors];
        for(int monitorIndex = 0; monitorIndex < numMonitors; monitorIndex++){
            monitors[monitorIndex] = new Monitor(monitorsBuffer.get(monitorIndex), monitorIndex);
        }
    }

    //Return the first monitor (primary)
    public static Monitor getPrimary(){
        checkInit();
        return getIndex(0);
    }
    //Return the monitor at the given index
    public static Monitor getIndex(int index){
        checkInit();
//        index = Math.max(0, Math.min(index, monitors.length - 1));
        index = index % monitors.length;
        return monitors[index];
    }

    private Monitors() {} // Prevent instantiation
}
