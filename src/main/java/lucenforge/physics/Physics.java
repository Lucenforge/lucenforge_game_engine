package lucenforge.physics;

import lucenforge.files.Log;

public class Physics {

    private static long gameStartTimeMillis;
    private static long framesSinceStart;

    private static long updateStartTime;
    private static long deltaTimeMillis = 0;

    public static void init(){
        gameStartTimeMillis = System.currentTimeMillis();
        updateStartTime = gameStartTimeMillis;
    }

    // Returns the time since startup in milliseconds
    public static long getRuntimeMillis(){
        return System.currentTimeMillis() - gameStartTimeMillis;
    }
    public static float getRuntimeSeconds(){
        return getRuntimeMillis()/1000f;
    }

    // Updates the time (and more later)
    public static void update() {
        long millisNow = System.currentTimeMillis();
        deltaTimeMillis = millisNow - updateStartTime;
        updateStartTime = millisNow;
        framesSinceStart++;
    }

    public static long deltaTimeMillis(){
        return deltaTimeMillis;
    }
    public static float deltaTimeSeconds(){
        return deltaTimeMillis()/1000f;
    }
    public static long framesSinceStart(){
        return framesSinceStart;
    }
}
