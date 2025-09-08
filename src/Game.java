
import core.Config;
import core.GameController;
import core.GameState;
import java.awt.*;
import javax.swing.*;
import player.AbstractPlayer;
import player.Duelist;
import player.Wizard;
import ui.GamePanel;
import ui.LogPanel;
import ui.StatusPanel;
import ui.GameUIManager;

public class Game {
    public static void main(String[] args) {
        Config.loadConfigs(
            "src/config/settings.xml",
            "src/config/assets.xml"
        );

        if (args.length < 2) {
            System.out.println("Missing arguments");
            System.out.println("Please run as: java core.Game <player-class> <player-name>");
            System.out.println("  where <player-class> is either 'wizard' or 'duelist'");
            System.out.println("  and <player-name> is the character name");
            System.exit(0);
        }

        String playerClass = args[0].toLowerCase();
        String playerName = args[1];
        AbstractPlayer player;
        switch (playerClass) {
            case "wizard":
                player = new Wizard(0, 0); // TODO: Set start position
                break;
            case "duelist":
                player = new Duelist(0, 0); // TODO: Set start position
                break;
            default:
                System.out.println("Invalid player class: " + playerClass);
                System.out.println("Valid options: 'wizard' or 'duelist'");
                System.exit(0);
                return;
        }

        // Set player name
        player.setName(playerName);

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
                }
            }
        } catch (Exception e) {}
        double fillPercentage = 0.45;
        GameState gameState = new GameState(player, mapWidth, mapHeight, fillPercentage);
        GameController controller = new GameController(gameState);

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Caves of Chaos");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            // Main game/map panel (center)
            GamePanel gamePanel = new GamePanel(gameState, controller);
            frame.add(gamePanel, BorderLayout.CENTER);

            // Status panel (right)
            StatusPanel statusPanel = new StatusPanel();
            frame.add(statusPanel, BorderLayout.EAST);

            // Log panel (bottom)
            LogPanel logPanel = new LogPanel();
            frame.add(logPanel, BorderLayout.SOUTH);
            
            // Create and set up the GameUIManager to manage panel boundaries
            GameUIManager uiManager = new GameUIManager(gamePanel, statusPanel, logPanel);
            gamePanel.setUIManager(uiManager);
            controller.setUIManager(uiManager);

            frame.setSize(1024, 768);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            // Request focus on the game panel to capture keyboard input
            gamePanel.requestFocus();
        });
    }
}
