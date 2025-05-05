package lucenforge.physics;

import lucenforge.files.Log;

import java.util.HashMap;

public class Physics {

    private static HashMap<String, Long> startTimes = new HashMap<>();
    private static HashMap<String, Long> lastTimes = new HashMap<>();

    public static void startTimer(String name) {
        startTimes.put(name, System.currentTimeMillis());
    }

    public static Long getTimerTime(String name) {
        if (startTimes.containsKey(name)) {
            return System.currentTimeMillis() - startTimes.get(name);
        } else {
            Log.writeln(Log.WARNING, "Timer not found: " + name);
            return null;
        }
    }

    public static float getFPS() {
        long frameTime = getTimerTime("frame");
        return 1000f / frameTime;
    }
}
