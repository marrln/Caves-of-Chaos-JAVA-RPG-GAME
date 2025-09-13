package core;

/**
 * A debugging utility class to help diagnose initialization issues.
 */
public class GameDebugger {

    public static void logError(String message, Exception e, boolean printStackTrace) {
        System.err.println("[ERROR] " + message);
        if (e != null) {
            System.err.println("         " + e.getMessage());
            if (printStackTrace) {
                e.printStackTrace();
            }
        }
    }
}
