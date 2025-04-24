package lucenforge.files;

import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

public class Properties {

    private static final String PROPERTIES_FILE = "src/main/resources/properties.ini";
    private static Wini ini;

    public static void checkInit(){
        // Check if the properties file is already initialized
        if (ini != null) {
            return;
        }
        // Initialize the properties file
        try {
            ini = new Wini(new File(PROPERTIES_FILE));
        } catch(IOException e) {
            Log.writeln(Log.ERROR, "Error loading properties file: " + e.getMessage());
        }
    }

    public static boolean getBoolean(String section, String key) {
        return get(section, key, Boolean.class);
    }
    public static int getInt(String section, String key) {
        return get(section, key, Integer.class);
    }

    public static void set(String section, String key, Object value) {
        checkInit();
        ini.put(section, key, value);
        Log.writeln(Log.SYSTEM, "Set property: " + section + "." + key + " = " + value);
        try {
            ini.store();
        } catch (IOException e) {
            Log.writeln(Log.ERROR, "Error saving properties file: " + e.getMessage());
        }
    }

    public static <T> T get(String section, String key, Class<T> type) {
        checkInit();
        return ini.get(section, key, type);
    }

    // Preventing instantiation
    public Properties() {}
}
