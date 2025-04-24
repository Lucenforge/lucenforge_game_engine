package lucenforge.output;

import lucenforge.files.Log;
import org.lwjgl.*;

import static org.lwjgl.glfw.GLFW.*;

public class Monitors {

    private static Monitor[] monitors;

    //Initialize the Monitor objects and populates the monitors array
    public static void init(){
        if(monitors == null){
            PointerBuffer monitorsBuffer = glfwGetMonitors();
            assert monitorsBuffer != null;
            int numMonitors = monitorsBuffer.remaining();
            monitors = new Monitor[numMonitors];
            for(int monitorIndex = 0; monitorIndex < numMonitors; monitorIndex++){
                monitors[monitorIndex] = new Monitor(monitorsBuffer.get(monitorIndex));
            }
        }else{
            Log.write(Log.WARNING, "Monitors already initialized");
        }
    }

    //Return the first monitor (primary)
    public static Monitor getPrimary(){
        return getMonitor(0);
    }
    //Return the monitor at the given index
    public static Monitor getMonitor(int index){
        return monitors[index];
    }

    private Monitors() {} // Prevent instantiation
}
