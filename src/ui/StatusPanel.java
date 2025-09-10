package ui;

import core.GameState;
import java.awt.*;
import javax.swing.*;

/**
 * Panel for displaying player status, inventory and game information.
 */
public class StatusPanel extends JPanel {
    private final Font titleFont = new Font("SansSerif", Font.BOLD, 16);
    private final Font normalFont = new Font("SansSerif", Font.PLAIN, 14);
    private GameState gameState;
    
    public StatusPanel() {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(200, 600));
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
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
        
        g.setColor(Color.WHITE);
        
        // Title
        g.setFont(titleFont);
        g.drawString("CHARACTER", 10, 25);
        
        // Status
        g.setFont(normalFont);
        if (gameState != null) {
            String levelDisplay = gameState.getLevelDisplayString();
            g.drawString(levelDisplay, 15, 50);
            
            // Add indicator if on final level
            if (!gameState.canGoToNextLevel()) {
                g.setColor(Color.YELLOW);
                g.drawString("FINAL LEVEL!", 15, 65);
                g.setColor(Color.WHITE);
            }
        } else {
            g.drawString("Cave Floor: ? of ?", 15, 50);
        }
        g.drawString("HP: 100/100", 15, 85);
        g.drawString("MP: 50/50", 15, 105);
        
        // Inventory section
        g.setFont(titleFont);
        g.drawString("INVENTORY", 10, 140);
        
        g.setFont(normalFont);
        g.drawString("1) Health Potion", 15, 165);
        g.drawString("2) Mana Potion", 15, 185);
        g.drawString("3) --", 15, 205);
        
        // Equipment section
        g.setFont(titleFont);
        g.drawString("EQUIPMENT", 10, 225);
        
        g.setFont(normalFont);
        g.drawString("Weapon: Sword", 15, 250);
        g.drawString("Armor: Leather", 15, 270);
        
        // Controls Help
        g.setFont(titleFont);
        g.drawString("CONTROLS", 10, 305);
        
        g.setFont(normalFont);
        g.drawString("WASD: Move", 15, 330);
        g.drawString("E: Attack", 15, 350);
        g.drawString("R: Rest", 15, 370);
        g.drawString("1-9: Use Item", 15, 390);
    }
}
