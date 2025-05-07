package lucenforge.files;

import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Properties {

    private static final HashMap<String, String> properties = new HashMap<>();
    private static final String PROPERTIES_FILE_PATH = "src/main/resources/properties.ini";
    private static Wini ini;

    public static void checkInit(){
        // Check if the properties file is already initialized
        if (ini != null) {
            return;
        }
        // Initialize the properties file
        try {
            File propertiesFile = new File(PROPERTIES_FILE_PATH);
            propertiesFile.createNewFile();
            ini = new Wini(propertiesFile);
        } catch(IOException e) {
            Log.writeln(Log.ERROR, "Error loading properties file: " + e.getMessage());
        }
    }

    // get boolean
    public static boolean get(String section, String key, Boolean defaultValue) {
        if(properties.containsKey(key)){
            return Boolean.parseBoolean(properties.get(key));
        }
        return get(section, key, Boolean.class, defaultValue);
    }
    // get integer
    public static int get(String section, String key, Integer defaultValue) {
        if(properties.containsKey(key)){
            return Integer.parseInt(properties.get(key));
        }
        return get(section, key, Integer.class, defaultValue);
    }
    // get float
    public static float get(String section, String key, Float defaultValue) {
        if(properties.containsKey(key)){
            return Float.parseFloat(properties.get(key));
        }
        return get(section, key, Float.class, defaultValue);
    }
    // get string
    public static String get(String section, String key, String defaultValue) {
        if(properties.containsKey(key)){
            return properties.get(key);
        }
        return get(section, key, String.class, defaultValue);
    }
    // get anything else (from file)
    public static <T> T get(String section, String key, Class<T> type, T defaultValue) {
        checkInit();
        T value;
        try {
            value = ini.get(section, key, type);
        }catch (Exception e){
            Log.writeln(Log.ERROR, "Error getting property: " + section + "." + key + ", setting to default");
            value = null;
        }
        if (value == null) {
            set(section, key, defaultValue);
            return defaultValue;
        }
        return value;
    }

    public static void set(String section, String key, Object value) {
        checkInit();
        // Check if the section exists
        Object current = null;
        try {
            current = ini.get(section, key, value.getClass());
        } catch (Exception e) {
            Log.writeln(Log.ERROR, "Error setting property: " + section + "." + key + " to " + value + ", setting to default");
        }
        if (current != null && current.equals(value))
            return;

        // If not, create it
        ini.put(section, key, value);
        properties.put(key, value.toString());
        Log.writeln(Log.SYSTEM, "Set property: " + section + "." + key + " = " + value);
        try {
            ini.store();
        } catch (IOException e) {
            Log.writeln(Log.ERROR, "Error saving properties file: " + e.getMessage());
        }
    }

    // Preventing instantiation
    public Properties() {}
}
