package input;

import core.GameController;
import core.GameState;
import map.GameMap;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import player.AbstractPlayer;
import ui.GamePanel;
import ui.GameUIManager;

/**
 * Handles all keyboard input for the game.
 * This class centralizes input processing and provides a clean separation
 * between UI components and input handling logic.
 * 
 * Supported controls:
 * - WASD/Arrow Keys: Player movement
 * - R: Rest/Wait
 * - E: Primary attack
 * - Q: Secondary attack
 * - 1-9: Use inventory items
 * - C: Debug - Center camera on player
 * - F: Debug - Toggle fog of war
 */
public class GameInputHandler extends KeyAdapter {
    
    private final GameState gameState;
    private final GameController controller;
    private final GamePanel gamePanel;
    private GameUIManager uiManager;
    
    /**
     * Creates a new GameInputHandler.
     * 
     * @param gameState The current game state
     * @param controller The game controller for processing actions
     * @param gamePanel The game panel for camera and rendering operations
     */
    public GameInputHandler(GameState gameState, GameController controller, GamePanel gamePanel) {
        this.gameState = gameState;
        this.controller = controller;
        this.gamePanel = gamePanel;
    }
    
    /**
     * Sets the UI manager for boundary checking.
     * This should be called after the UI manager is initialized.
     * 
     * @param uiManager The UI manager instance
     */
    public void setUIManager(GameUIManager uiManager) {
        this.uiManager = uiManager;
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int dx = 0, dy = 0;
        boolean shouldUpdate = false;
        
        switch (e.getKeyCode()) {
            // Movement controls
            case KeyEvent.VK_W, KeyEvent.VK_UP -> { 
                dy = -1; 
            }
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> { 
                dy = 1; 
            }
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> { 
                dx = -1; 
            }
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> { 
                dx = 1; 
            }
            
            // Action controls
            case KeyEvent.VK_R -> { 
                controller.rest(); 
                shouldUpdate = true; 
            }
            case KeyEvent.VK_E -> { 
                controller.attack(1); 
                shouldUpdate = true; 
            }
            case KeyEvent.VK_Q -> { 
                controller.attack(2); 
                shouldUpdate = true; 
            }
            
            // Inventory controls (1-9)
            case KeyEvent.VK_1 -> { 
                controller.useItem(1); 
                shouldUpdate = true; 
            }
            case KeyEvent.VK_2 -> { 
                controller.useItem(2); 
                shouldUpdate = true; 
            }
            case KeyEvent.VK_3 -> { 
                controller.useItem(3); 
                shouldUpdate = true; 
            }
            case KeyEvent.VK_4 -> { 
                controller.useItem(4); 
                shouldUpdate = true; 
            }
            case KeyEvent.VK_5 -> { 
                controller.useItem(5); 
                shouldUpdate = true; 
            }
            case KeyEvent.VK_6 -> { 
                controller.useItem(6); 
                shouldUpdate = true; 
            }
            case KeyEvent.VK_7 -> { 
                controller.useItem(7); 
                shouldUpdate = true; 
            }
            case KeyEvent.VK_8 -> { 
                controller.useItem(8); 
                shouldUpdate = true; 
            }
            case KeyEvent.VK_9 -> { 
                controller.useItem(9); 
                shouldUpdate = true; 
            }
            
            // Debug controls
            case KeyEvent.VK_C -> {
                // Debug key: manually recenter camera on player
                gamePanel.centerCameraOnPlayer();
                return; // Don't process further
            }
            case KeyEvent.VK_F -> {
                // Debug key: toggle fog of war
                gamePanel.toggleFogOfWar();
                return; // Don't process further
            }
            
            default -> { 
                return; // Unknown key, do nothing
            }
        }
        
        // Handle movement input
        if (dx != 0 || dy != 0) {
            handleMovementInput(dx, dy);
            shouldUpdate = true;
        }
        
        // Update the game view if needed
        if (shouldUpdate) {
            updateGameView();
        }
    }
    
    /**
     * Processes movement input and validates the move before executing it.
     * 
     * @param dx The horizontal movement delta (-1, 0, or 1)
     * @param dy The vertical movement delta (-1, 0, or 1)
     */
    private void handleMovementInput(int dx, int dy) {
        AbstractPlayer player = gameState.getPlayer();
        int newX = player.getX() + dx;
        int newY = player.getY() + dy;
        
        // Check if the position is within the visible UI boundaries
        boolean isVisible = (uiManager == null) || 
                            uiManager.isPositionVisible(newX, newY);
        
        // Only try to move if within visible area
        if (isVisible) {
            controller.movePlayer(dx, dy);
        }
    }
    
    /**
     * Updates the camera and refreshes the game view after input processing.
     */
    private void updateGameView() {
        AbstractPlayer player = gameState.getPlayer();
        GameMap map = gameState.getCurrentMap();
        
        // Update camera to follow player
        gamePanel.getCamera().setMapSize(map.getWidth(), map.getHeight());
        gamePanel.getCamera().centerOn(player.getX(), player.getY());
        
        // Refresh the view
        gamePanel.repaint();
    }
}
