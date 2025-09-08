package core;

/**
 * A debugging utility class to help diagnose initialization issues.
 */
public class GameDebugger {
    /**
     * Log a debug message with a specific tag.
     * 
     * @param tag The tag for categorizing the message (e.g., "CONFIG", "INIT", "ERROR")
     * @param message The message to log
     */
    public static void log(String tag, String message) {
        System.out.println("[" + tag + "] " + message);
    }
    
    /**
     * Log an error message and optionally print a stack trace.
     * 
     * @param message The error message
     * @param e The exception that occurred (can be null)
     * @param printStackTrace Whether to print the stack trace
     */
    public static void logError(String message, Exception e, boolean printStackTrace) {
        System.err.println("[ERROR] " + message);
        if (e != null) {
            System.err.println("         " + e.getMessage());
            if (printStackTrace) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Check if a file exists and log the result.
     * 
     * @param filePath The path to the file to check
     * @return true if the file exists, false otherwise
     */
    public static boolean checkFileExists(String filePath) {
        boolean exists = new java.io.File(filePath).exists();
        if (exists) {
            log("FILE", "File exists: " + filePath);
        } else {
            logError("File does not exist: " + filePath, null, false);
        }
        return exists;
    }
    
    /**
     * Print system information to help diagnose environment issues.
     */
    public static void printSystemInfo() {
        log("SYSTEM", "Java Version: " + System.getProperty("java.version"));
        log("SYSTEM", "Java Home: " + System.getProperty("java.home"));
        log("SYSTEM", "OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
        log("SYSTEM", "Working Directory: " + System.getProperty("user.dir"));
    }
}
