package lucenforge.output;

public class Log {

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
        System.out.print(logType);
        System.out.print(message);
    }

    // Write into log (with new-line)
    public static void writeln(Object message) {
        writeln(EVENT, message);
    }
    public static void writeln(String logType, Object message) {
        write(logType, message);
        write("\n");
    }

    private void sendToLogFile(){
        //todo (make sure to strip any ANSI escape codes)
    }


    private Log() {} // Prevent instantiation
}
