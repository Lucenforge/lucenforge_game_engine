import lucenforge.Engine;
import lucenforge.files.Properties;

public class Main {

    public static void main(String[] args) {
        Properties.set("window", "resolution_x", 1200);
        Properties.set("window", "resolution_y", 1000);
        Engine.run();
    }

}
