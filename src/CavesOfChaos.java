import audio.MusicManager;
import config.Config;
import config.StyleConfig;
import core.*;
import graphics.AssetManager;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import player.*;
import ui.*;
import utils.FontLoader;

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
        loadResources();
        validateArgs(args);

        String playerClass = args[0].toLowerCase();
        String playerName  = args[1];
        int maxLevel       = Config.getIntSetting("caveLevelNumber");

        int startingLevel  = parseStartingLevel(args, maxLevel);
        boolean bossTest   = (startingLevel == maxLevel - 1);

        AbstractPlayer player = createPlayer(playerClass, playerName);
        if (bossTest) boostPlayerForBossTest(player);

        startGame(player, startingLevel);
    }

    // === Resource Initialization ===
    private static void loadResources() {
        try {
            FontLoader.loadCustomFonts();
            Config.loadConfigs(CONFIG_PATH, ASSETS_PATH);
            StyleConfig.loadStyling(STYLING_PATH);
        } catch (Exception e) {
            GameDebugger.logError("Failed to load configuration files", e, true);
            System.exit(1);
        }
    }

    // === Command-line Validation ===
    private static void validateArgs(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java CavesOfChaos <player-class> <player-name> [starting-level]");
            System.out.println("Example: java CavesOfChaos wizard gandalf 5");
            System.exit(0);
        }
    }

    // === Parse Starting Level ===
    private static int parseStartingLevel(String[] args, int maxLevel) {
        if (args.length < 3) return 0; // Default to level 1 (0-indexed)

        try {
            int level = Integer.parseInt(args[2]);
            if (level < 1 || level > maxLevel) {
                System.out.println("Invalid starting level: " + level + " (must be 1-" + maxLevel + ")");
                System.exit(0);
            }

            boolean bossMode = (level == maxLevel);
            System.out.printf("Starting on cave level %d (boss test mode: %s)%n", level, bossMode ? "ON" : "OFF");
            return level - 1;

        } catch (NumberFormatException e) {
            System.out.println("Invalid starting level format: " + args[2]);
            System.exit(0);
            return 0; // Unreachable, but required for compilation
        }
    }

    // === Player Creation ===
    private static AbstractPlayer createPlayer(String playerClass, String playerName) {
        // Sanitize and format player name
        playerName = sanitizePlayerName(playerName);

        AbstractPlayer player;
        switch (playerClass) {
            case "wizard" -> player = new Wizard(0, 0);
            case "duelist" -> player = new Duelist(0, 0);
            default -> {
                System.out.println("Invalid player class: " + playerClass);
                System.out.println("Valid options: wizard | duelist");
                System.exit(0);
                throw new IllegalArgumentException("Invalid player class: " + playerClass); // Defensive: never returns null
            }
        }
        player.setName(playerName);
        return player;
    }

    private static String sanitizePlayerName(String rawName) {
        if (rawName == null || rawName.isBlank()) return "Player";

        rawName = rawName.trim();
        if (rawName.length() > 12) rawName = rawName.substring(0, 12);

        return Character.toUpperCase(rawName.charAt(0)) + rawName.substring(1);
    }

    // === Boss Test Mode ===
    private static void boostPlayerForBossTest(AbstractPlayer player) {
        final int targetLevel = 6;
        System.out.println("BOSS TEST MODE: Boosting player to level " + targetLevel + "...");

        try {
            int totalXpNeeded = 15000; // More than enough to reach level 6
            int levelsGained = player.addExperience(totalXpNeeded);

            System.out.printf(
                "Player leveled up %d times%nFinal level: %d (HP: %d/%d, MP: %d/%d)%n",
                levelsGained,
                player.getLevel(),
                player.getHp(), player.getMaxHp(),
                player.getMp(), player.getMaxMp()
            );
        } catch (Exception e) {
            System.err.println("ERROR during player level boost: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    // === Game Initialization ===
    private static void startGame(AbstractPlayer player, int startingLevel) {
        GameState gameState = new GameState(player, startingLevel);
        GameController controller = new GameController(gameState);

        SwingUtilities.invokeLater(() -> createAndShowUI(gameState, controller));
    }

    // === UI Setup ===
    private static void createAndShowUI(GameState gameState, GameController controller) {
        JFrame frame = new JFrame(GAME_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(AssetManager.getAppIcon());
        frame.setLayout(new BorderLayout());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                MusicManager.getInstance().cleanup();
                System.out.println("Game cleanup completed - goodbye!");
            }
        });

        GamePanel gamePanel = new GamePanel(gameState, controller);
        StatusPanel statusPanel = new StatusPanel();
        LogPanel logPanel = new LogPanel();

        statusPanel.setGameState(gameState);

        frame.add(gamePanel, BorderLayout.CENTER);
        frame.add(statusPanel, BorderLayout.EAST);
        frame.add(logPanel, BorderLayout.SOUTH);

        GameUIManager uiManager = new GameUIManager(gamePanel, statusPanel, logPanel);
        gamePanel.setUIManager(uiManager);
        controller.setUIManager(uiManager);
        gameState.setUIManager(uiManager);

        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        gamePanel.requestFocus();
    }
}
