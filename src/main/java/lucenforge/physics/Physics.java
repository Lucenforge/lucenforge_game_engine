package lucenforge.physics;

public class Physics {

    private static long gameTimeStart ;
    private static long frameStartTime;
    private static long lastFrameTime ;

    public static void init(){
        long millis = System.currentTimeMillis();
        gameTimeStart = millis;
        frameStartTime = millis;
        lastFrameTime = 0;
    }

    // Returns the time since startup in milliseconds
    public static long getRuntime(){
        return System.currentTimeMillis() - gameTimeStart;
    }

    // Returns the last frame's time in milliseconds
    public static long lastFrameMillis() {
        return lastFrameTime;
    }
    public static float lastFrameSeconds() {
        return lastFrameMillis()/1000f;
    }

    // Returns the current frame's time in milliseconds
    public static long currentFrameMillis() {
        return System.currentTimeMillis() - frameStartTime;
    }
    public static float currentFrameSeconds() {
        return currentFrameMillis()/1000f;
    }

    // Updates the frame times (and more later)
    public static void update() {
        long currentTime = System.currentTimeMillis();
        lastFrameTime = currentTime - frameStartTime;
        frameStartTime = currentTime;
    }
}
