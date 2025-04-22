package jjEngine.output;

import jjEngine.input.Keyboard;
import jjEngine.output.Window;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

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
            throw new IllegalStateException("Monitors already initialized");
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
}
