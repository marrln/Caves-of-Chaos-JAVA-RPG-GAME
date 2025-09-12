package ui;

import config.StyleConfig;
import core.GameState;
import java.awt.*;
import javax.swing.*;

/**
 * Panel for displaying player status, inventory and game information.
 */
public class StatusPanel extends JPanel {
    private GameState gameState;
    
    public StatusPanel() {
        setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
        setPreferredSize(new Dimension(200, 600));
        setBorder(BorderFactory.createLineBorder(StyleConfig.getColor("panelBorder", Color.DARK_GRAY), 1));
    }
    
    /**
     * Sets the game state for dynamic information display.
     * 
     * @param gameState The current game state
     */
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        repaint(); // Refresh display when game state changes
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.setColor(StyleConfig.getColor("panelText", Color.WHITE));
        
        // Dynamic layout constants
        final int LINE_HEIGHT = 20;
        final int SECTION_SPACING = 10;
        final int LEFT_MARGIN = 15;
        final int TITLE_LEFT_MARGIN = 10;
        
        int currentY = 25;
        
        // Title
        g.setFont(StyleConfig.getFont("statusTitle", new Font("SansSerif", Font.BOLD, 16)));
        g.drawString("CHARACTER", TITLE_LEFT_MARGIN, currentY);
        currentY += LINE_HEIGHT + 5; // Extra space after title
        
        // Status section
        g.setFont(StyleConfig.getFont("statusNormal", new Font("SansSerif", Font.PLAIN, 14)));
        
        if (gameState != null && gameState.getPlayer() != null) {
            // Player name and class
            String playerName = gameState.getPlayer().getName();
            String playerClass = gameState.getPlayer().getClass().getSimpleName();
            g.drawString("Name: " + (playerName != null ? playerName : "Unknown"), LEFT_MARGIN, currentY);
            currentY += LINE_HEIGHT;
            g.drawString("Class: " + playerClass, LEFT_MARGIN, currentY);
            currentY += LINE_HEIGHT;
            
            // Player stats with color coding for health
            int currentHp = gameState.getPlayer().getHp();
            int maxHp = gameState.getPlayer().getMaxHp();
            double hpPercentage = (double) currentHp / maxHp;
            
            // Change color based on health percentage
            if (hpPercentage < 0.25) {
                g.setColor(Color.RED); // Critical health
            } else if (hpPercentage < 0.5) {
                g.setColor(Color.ORANGE); // Low health
            } else {
                g.setColor(StyleConfig.getColor("panelText", Color.WHITE)); // Normal
            }
            
            g.drawString("HP: " + currentHp + "/" + maxHp, LEFT_MARGIN, currentY);
            currentY += LINE_HEIGHT;
            
            // Reset color for MP
            g.setColor(StyleConfig.getColor("panelText", Color.WHITE));
            
            // Only display mana for classes that use it (max MP > 0)
            if (gameState.getPlayer().getMaxMp() > 0) {
                g.drawString("MP: " + gameState.getPlayer().getMp() + "/" + gameState.getPlayer().getMaxMp(), LEFT_MARGIN, currentY);
                currentY += LINE_HEIGHT;
            }
            
            String levelDisplay = gameState.getLevelDisplayString();
            g.drawString(levelDisplay, LEFT_MARGIN, currentY);
            currentY += LINE_HEIGHT;
            
            // Show enemy count to track combat
            int enemyCount = gameState.getCurrentEnemies().size();
            g.drawString("Enemies: " + enemyCount, LEFT_MARGIN, currentY);
            currentY += LINE_HEIGHT;
            
            // Add indicator if on final level
            if (!gameState.canGoToNextLevel()) {
                g.setColor(StyleConfig.getColor("panelHighlight", Color.YELLOW));
                g.drawString("FINAL LEVEL!", LEFT_MARGIN, currentY);
                g.setColor(StyleConfig.getColor("panelText", Color.WHITE));
                currentY += LINE_HEIGHT;
            }
        } else {
            g.drawString("Name: Unknown", LEFT_MARGIN, currentY);
            currentY += LINE_HEIGHT;
            g.drawString("Class: Unknown", LEFT_MARGIN, currentY);
            currentY += LINE_HEIGHT;
            g.drawString("HP: ?/?", LEFT_MARGIN, currentY);
            currentY += LINE_HEIGHT;
            g.drawString("MP: ?/?", LEFT_MARGIN, currentY);
            currentY += LINE_HEIGHT;
            g.drawString("Cave Floor: ? of ?", LEFT_MARGIN, currentY);
            currentY += LINE_HEIGHT;
        }
        
        currentY += SECTION_SPACING;
        
        // Inventory section
        g.setFont(StyleConfig.getFont("statusTitle", new Font("SansSerif", Font.BOLD, 16)));
        g.drawString("INVENTORY", TITLE_LEFT_MARGIN, currentY);
        currentY += LINE_HEIGHT + 5;
        
        g.setFont(StyleConfig.getFont("statusNormal", new Font("SansSerif", Font.PLAIN, 14)));
        // Display all 9 inventory slots with new format [num] ItemName: x/x
        String[] inventoryItems = {
            "[1] Health Potion: 3/3",
            "[2] Mana Potion: 2/5", 
            "[3] --",
            "[4] --",
            "[5] --",
            "[6] --",
            "[7] --",
            "[8] --",
            "[9] --"
        };
        
        for (String item : inventoryItems) {
            g.drawString(item, LEFT_MARGIN, currentY);
            currentY += LINE_HEIGHT;
        }
        
        currentY += SECTION_SPACING;
        
        // Equipment section
        g.setFont(StyleConfig.getFont("statusTitle", new Font("SansSerif", Font.BOLD, 16)));
        g.drawString("EQUIPMENT", TITLE_LEFT_MARGIN, currentY);
        currentY += LINE_HEIGHT + 5;
        
        g.setFont(StyleConfig.getFont("statusNormal", new Font("SansSerif", Font.PLAIN, 14)));
        g.drawString("Weapon: Sword", LEFT_MARGIN, currentY);
        currentY += LINE_HEIGHT;
        g.drawString("Armor: Leather", LEFT_MARGIN, currentY);
        currentY += LINE_HEIGHT;
        
        currentY += SECTION_SPACING;
        
        // Controls Help
        g.setFont(StyleConfig.getFont("statusTitle", new Font("SansSerif", Font.BOLD, 16)));
        g.drawString("CONTROLS", TITLE_LEFT_MARGIN, currentY);
        currentY += LINE_HEIGHT + 5;
        
        g.setFont(StyleConfig.getFont("statusNormal", new Font("SansSerif", Font.PLAIN, 14)));
        g.drawString("WASD: Move", LEFT_MARGIN, currentY);
        currentY += LINE_HEIGHT;
        g.drawString("Q/E: Attack", LEFT_MARGIN, currentY);
        currentY += LINE_HEIGHT;
        g.drawString("R: Rest", LEFT_MARGIN, currentY);
        currentY += LINE_HEIGHT;
        g.drawString("1-9: Use Item", LEFT_MARGIN, currentY);
    }
}
