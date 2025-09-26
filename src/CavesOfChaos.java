import audio.MusicManager;
import config.Config;
import config.StyleConfig;
import core.GameController;
import core.GameDebugger;
import core.GameState;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
 * Main entry point for Caves of Chaos.
 */
public class CavesOfChaos {

    private static final String GAME_TITLE   = "Caves of Chaos";
    private static final int WINDOW_WIDTH    = 1024;
    private static final int WINDOW_HEIGHT   = 768;
    private static final String CONFIG_PATH  = "bin/config/settings.xml";
    private static final String ASSETS_PATH  = "bin/config/assets.xml";
    private static final String STYLING_PATH = "bin/config/styling.xml";

    public static void main(String[] args) {

        // === Load configuration (must exist, no fallbacks) ===
        try {
            Config.loadConfigs(CONFIG_PATH, ASSETS_PATH);
            StyleConfig.loadStyling(STYLING_PATH);
        } catch (Exception e) {
            GameDebugger.logError("Failed to load configuration files", e, true);
            System.exit(1);
        }

        // === Command-line arguments ===
        if (args.length < 2) {
            System.out.println("Usage: java CavesOfChaos <player-class> <player-name> [starting-level]");
            System.out.println("Example: java CavesOfChaos wizard gandalf 5");
            System.exit(0);
        }

        String playerClass = args[0].toLowerCase();
        String playerName  = args[1];
        int startingLevel = 0; // Default to level 1 (0-indexed)
        int max_level = Config.getIntSetting("caveLevelNumber");
        // Parse optional starting level
        if (args.length >= 3) {
            try {
            int level = Integer.parseInt(args[2]);
            if (level >= 1 && level <= max_level) {
                startingLevel = level - 1; // Convert to 0-indexed
                System.out.println("Starting on level " + level + " (boss mode: " + (level == max_level ? "ON" : "OFF") + ")");
            } else {
                System.out.println("Invalid starting level: " + level + " (must be 1-" + max_level + ")");
                System.exit(0);
            }
            } catch (NumberFormatException e) {
            System.out.println("Invalid starting level format: " + args[2]);
            System.exit(0);
            }
        }
        AbstractPlayer player;
        switch (playerClass) {
            case "wizard" -> player = new Wizard(0, 0);
            case "duelist" -> player = new Duelist(0, 0);
            default -> {
                System.out.println("Invalid player class: " + playerClass);
                System.out.println("Valid options: wizard | duelist");
                System.exit(0);
                return;
            }
        }
        player.setName(playerName);

        // === Initialize game ===
        GameState gameState = new GameState(player, startingLevel);
        GameController controller = new GameController(gameState);

        // === Build UI on Event Dispatch Thread ===
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(GAME_TITLE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    MusicManager.getInstance().cleanup();
                    System.out.println("Game cleanup completed - goodbye!");
                }
            });

            GamePanel gamePanel = new GamePanel(gameState, controller);
            StatusPanel status  = new StatusPanel();
            LogPanel logPanel   = new LogPanel();

            status.setGameState(gameState);

            frame.add(gamePanel, BorderLayout.CENTER);
            frame.add(status, BorderLayout.EAST);
            frame.add(logPanel, BorderLayout.SOUTH);

            GameUIManager uiManager = new GameUIManager(gamePanel, status, logPanel);
            gamePanel.setUIManager(uiManager);
            controller.setUIManager(uiManager);
            gameState.setUIManager(uiManager);

            frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            gamePanel.requestFocus();
        });
    }
}
