package lucenforge.files;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Log {

    static BufferedWriter writer;

    public static void checkInit(){
        if(writer != null) return;
        try{
            String logDirPath = "_logs";
            // Ensure the log directory exists
            FileTools.createDirectory(logDirPath);
            // Get the number of logs allowed from properties
            int maxLogs = Properties.get("logging", "max_logs", 3);
            // If the number of files is greater than the max logs, delete the oldest log file
            FileTools.limitNumFilesInDir(logDirPath, maxLogs);
            // Create a new log file with the current timestamp
            String currentTimeSec = String.valueOf(System.currentTimeMillis()/1000);
            writer = new BufferedWriter(new FileWriter("_logs/log_"+currentTimeSec+".txt", true));
        }
        catch (IOException e) {
            System.err.println("Error initializing log file: " + e.getMessage());
        }
    }

    // Log Types (ANSI Escape Codes)
    public static final String SYSTEM     = "\033[0;36m"; // CYAN
    public static final String FAILURE    = "\033[0;31m"; // Red Text
    public static final String SUCCESS    = "\033[0;32m"; // Green Text
    public static final String DEBUG      = "\033[0;35m"; // Purple text
    public static final String TELEMETRY  = "\033[0;34m"; // Blue Text
    public static final String ERROR      = "\033[1;31m"; // Bold Red Text
    public static final String WARNING    = "\033[0;33m"; // yellow text
    public static final String EVENT      = "\033[0m";    // Text Reset (white/gray)

    // Write into log (no new-line)
    public static void write(Object message) {
        write(EVENT, message);
    }
    public static void write(String logType, Object message) {
        checkInit();
        System.out.print(logType);
        if(logType.equals(ERROR))
            message = "ERROR: " + message;
        else if(logType.equals(WARNING))
            message = "WARNING: " + message;
        System.out.print(message);
        sendToLogFile(message.toString());
    }

    // Write into log (with new-line)
    public static void writeln(Object message) {
        writeln(EVENT, message);
    }
    public static void writeln(String logType, Object message) {
        write(logType, message);
        write("\n");
    }

    private static void sendToLogFile(String line){
        //Strip ANSI Escape Codes
        line = line.replace(SYSTEM   , "");
        line = line.replace(FAILURE  , "");
        line = line.replace(SUCCESS  , "");
        line = line.replace(DEBUG    , "");
        line = line.replace(TELEMETRY, "");
        line = line.replace(ERROR    , "");
        line = line.replace(WARNING  , "");
        line = line.replace(EVENT    , "");

        try{
            checkInit();
            writer.write(line);
            writer.flush();
        }catch (IOException ex) {
            Log.writeln(Log.ERROR, "Error writing to log file: " + ex.getMessage());
        }
    }


    private Log() {} // Prevent instantiation
}
