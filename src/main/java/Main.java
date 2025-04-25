import lucenforge.Engine;
import lucenforge.files.Properties;

public class Main {

    public static void main(String[] args) {
        Properties.set("window", "monitor", 0);
        Engine.run();
    }

}
