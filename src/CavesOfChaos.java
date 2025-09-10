import core.Config;
import core.GameController;
import core.GameDebugger;
import core.GameState;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import player.AbstractPlayer;
import player.Duelist;
import player.Wizard;
import ui.GamePanel;
import ui.GameUIManager;
import ui.LogPanel;
import ui.StatusPanel;

/**
 * Main class for the Caves of Chaos game.
 * This is the entry point for the application.
 */
public class CavesOfChaos {
    
    // Constants
    private static final String GAME_TITLE = "Caves of Chaos";
    private static final int WINDOW_WIDTH = 1024;
    private static final int WINDOW_HEIGHT = 768;
    private static final String CONFIG_PATH = "bin/config/settings.xml";
    private static final String ASSETS_PATH = "bin/config/assets.xml";
    
    /**
     * Main method, entry point for the game.
     * 
     * @param args Command line arguments for player class and name
     */
    public static void main(String[] args) {
        // Print system information
        GameDebugger.printSystemInfo();
        
        // Load configuration files
        try {
            GameDebugger.log("CONFIG", "Attempting to load config files from: " + CONFIG_PATH + " and " + ASSETS_PATH);
            
            // Check if files exist first
            boolean settingsExist = GameDebugger.checkFileExists(CONFIG_PATH);
            boolean assetsExist = GameDebugger.checkFileExists(ASSETS_PATH);
            
            if (settingsExist && assetsExist) {
                Config.loadConfigs(CONFIG_PATH, ASSETS_PATH);
                GameDebugger.log("CONFIG", "Configuration files loaded successfully");
            } else {
                // Try alternate paths
                String altSettingsPath = "src/config/settings.xml";
                String altAssetsPath = "src/config/assets.xml";
                
                GameDebugger.log("CONFIG", "Trying alternate paths: " + altSettingsPath + " and " + altAssetsPath);
                
                settingsExist = GameDebugger.checkFileExists(altSettingsPath);
                assetsExist = GameDebugger.checkFileExists(altAssetsPath);
                
                if (settingsExist && assetsExist) {
                    Config.loadConfigs(altSettingsPath, altAssetsPath);
                    GameDebugger.log("CONFIG", "Configuration files loaded from alternate paths");
                } else {
                    GameDebugger.logError("Could not find configuration files", null, false);
                    System.exit(1);
                }
            }
        } catch (Exception e) {
            GameDebugger.logError("Failed to load configuration files", e, true);
            System.exit(1);
        }
        
        // Process command line arguments
        if (args.length < 2) {
            GameDebugger.log("ARGS", "Missing command line arguments");
            System.out.println("Missing arguments");
            System.out.println("Please run as: java CavesOfChaos <player-class> <player-name>");
            System.out.println("  where <player-class> is either 'wizard' or 'duelist'");
            System.out.println("  and <player-name> is the character name");
            System.exit(0);
        }

        String playerClass = args[0].toLowerCase();
        String playerName = args[1];
        GameDebugger.log("PLAYER", "Creating player of class: " + playerClass + " with name: " + playerName);
        
        AbstractPlayer player;
        switch (playerClass) {
            case "wizard" -> player = new Wizard(0, 0); // Starting position
            case "duelist" -> player = new Duelist(0, 0); // Starting position
            default -> {
                GameDebugger.logError("Invalid player class: " + playerClass, null, false);
                System.out.println("Invalid player class: " + playerClass);
                System.out.println("Valid options: 'wizard' or 'duelist'");
                System.exit(0);
                return;
            }
        }

        // Set player name
        player.setName(playerName);
        GameDebugger.log("PLAYER", "Player created successfully");

        // Read map size from settings.xml
        int mapWidth = 40;
        int mapHeight = 40;
        try {
            String mapSizeStr = Config.getSetting("mapSize");
            if (mapSizeStr != null && mapSizeStr.contains("width")) {
                int wIdx = mapSizeStr.indexOf("width=\"");
                int hIdx = mapSizeStr.indexOf("height=\"");
                if (wIdx != -1 && hIdx != -1) {
                    int wEnd = mapSizeStr.indexOf('"', wIdx + 7 + 1);
                    int hEnd = mapSizeStr.indexOf('"', hIdx + 8 + 1);
                    mapWidth = Integer.parseInt(mapSizeStr.substring(wIdx + 7, wEnd));
                    mapHeight = Integer.parseInt(mapSizeStr.substring(hIdx + 8, hEnd));
                    GameDebugger.log("MAP", "Map dimensions from config: " + mapWidth + "x" + mapHeight);
                }
            }
        } catch (Exception e) {
            GameDebugger.logError("Error parsing map dimensions", e, true);
            GameDebugger.log("MAP", "Using default map dimensions: " + mapWidth + "x" + mapHeight);
        }
        
        double fillPercentage = 0.45;
        
        // Create game state with player instance
        GameDebugger.log("INIT", "Initializing game state...");
        GameState gameState = new GameState(player, mapWidth, mapHeight, fillPercentage);
        GameController controller = new GameController(gameState);
        GameDebugger.log("INIT", "Game state initialized successfully");
        
        // Use SwingUtilities to ensure UI is created on the Event Dispatch Thread
        GameDebugger.log("UI", "Creating game UI...");
        SwingUtilities.invokeLater(() -> {
            // Create the main frame
            JFrame frame = new JFrame(GAME_TITLE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            
            // Main game/map panel (center)
            GamePanel gamePanel = new GamePanel(gameState, controller);
            frame.add(gamePanel, BorderLayout.CENTER);
            
            // Status panel (right)
            StatusPanel statusPanel = new StatusPanel();
            statusPanel.setGameState(gameState); // Connect status panel to game state
            frame.add(statusPanel, BorderLayout.EAST);
            
            // Log panel (bottom)
            LogPanel logPanel = new LogPanel();
            frame.add(logPanel, BorderLayout.SOUTH);
            
            // Create and set up the GameUIManager to manage panel boundaries
            GameUIManager uiManager = new GameUIManager(gamePanel, statusPanel, logPanel);
            gamePanel.setUIManager(uiManager);
            controller.setUIManager(uiManager);
            
            frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            // Request focus on the game panel to capture keyboard input
            gamePanel.requestFocus();
            
            GameDebugger.log("UI", "Game UI created and displayed");
        });
    }
}
