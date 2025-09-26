package input;

import core.GameController;
import core.GameState;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import map.GameMap;
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
 * - SPACE: Pick up item
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
    
    // Input throttling to prevent spam and game slowdown
    private static final long MOVEMENT_COOLDOWN_MS = 150; // Minimum time between movements
    private static final long ACTION_COOLDOWN_MS = 200;   // Minimum time between actions
    private long lastMovementTime = 0;
    private long lastActionTime = 0;
    
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
        long currentTime = System.currentTimeMillis();
        int dx = 0, dy = 0;
        boolean shouldUpdate = false;
        boolean isMovement = false;
        boolean isAction = false;
        
        switch (e.getKeyCode()) {
            // Movement controls
            case KeyEvent.VK_W, KeyEvent.VK_UP -> { 
                dy = -1; 
                isMovement = true;
            }
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> { 
                dy = 1; 
                isMovement = true;
            }
            case KeyEvent.VK_A, KeyEvent.VK_LEFT -> { 
                dx = -1; 
                isMovement = true;
            }
            case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> { 
                dx = 1; 
                isMovement = true;
            }
            
            // Action controls
            case KeyEvent.VK_R -> { 
                if (currentTime - lastActionTime >= ACTION_COOLDOWN_MS) {
                    controller.rest(); 
                    shouldUpdate = true;
                    isAction = true;
                }
            }
            case KeyEvent.VK_E -> { 
                if (currentTime - lastActionTime >= ACTION_COOLDOWN_MS) {
                    controller.attack(1); 
                    shouldUpdate = true;
                    isAction = true;
                }
            }
            case KeyEvent.VK_Q -> { 
                if (currentTime - lastActionTime >= ACTION_COOLDOWN_MS) {
                    controller.attack(2); 
                    shouldUpdate = true;
                    isAction = true;
                }
            }
            
            // Item interaction controls
            case KeyEvent.VK_SPACE -> { 
                if (currentTime - lastActionTime >= ACTION_COOLDOWN_MS) {
                    controller.pickupItem(); 
                    shouldUpdate = true;
                    isAction = true;
                }
            }
            
            // Inventory controls (1-9)
            case KeyEvent.VK_1 -> { 
                if (currentTime - lastActionTime >= ACTION_COOLDOWN_MS) {
                    controller.useItem(1); 
                    shouldUpdate = true;
                    isAction = true;
                }
            }
            case KeyEvent.VK_2 -> { 
                if (currentTime - lastActionTime >= ACTION_COOLDOWN_MS) {
                    controller.useItem(2); 
                    shouldUpdate = true;
                    isAction = true;
                }
            }
            case KeyEvent.VK_3 -> { 
                if (currentTime - lastActionTime >= ACTION_COOLDOWN_MS) {
                    controller.useItem(3); 
                    shouldUpdate = true;
                    isAction = true;
                }
            }
            case KeyEvent.VK_4 -> { 
                if (currentTime - lastActionTime >= ACTION_COOLDOWN_MS) {
                    controller.useItem(4); 
                    shouldUpdate = true;
                    isAction = true;
                }
            }
            case KeyEvent.VK_5 -> { 
                if (currentTime - lastActionTime >= ACTION_COOLDOWN_MS) {
                    controller.useItem(5); 
                    shouldUpdate = true;
                    isAction = true;
                }
            }
            case KeyEvent.VK_6 -> { 
                if (currentTime - lastActionTime >= ACTION_COOLDOWN_MS) {
                    controller.useItem(6); 
                    shouldUpdate = true;
                    isAction = true;
                }
            }
            case KeyEvent.VK_7 -> { 
                if (currentTime - lastActionTime >= ACTION_COOLDOWN_MS) {
                    controller.useItem(7); 
                    shouldUpdate = true;
                    isAction = true;
                }
            }
            case KeyEvent.VK_8 -> { 
                if (currentTime - lastActionTime >= ACTION_COOLDOWN_MS) {
                    controller.useItem(8); 
                    shouldUpdate = true;
                    isAction = true;
                }
            }
            case KeyEvent.VK_9 -> { 
                if (currentTime - lastActionTime >= ACTION_COOLDOWN_MS) {
                    controller.useItem(9); 
                    shouldUpdate = true;
                    isAction = true;
                }
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
        
        // Handle movement input with throttling
        if ((dx != 0 || dy != 0) && isMovement) {
            if (currentTime - lastMovementTime >= MOVEMENT_COOLDOWN_MS) {
                handleMovementInput(dx, dy);
                shouldUpdate = true;
                lastMovementTime = currentTime;
            }
        }
        
        // Update action timestamp if an action was performed
        if (isAction) {
            lastActionTime = currentTime;
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
        // Safety checks to prevent crashes
        if (gameState == null || controller == null) return;
        
        AbstractPlayer player = gameState.getPlayer();
        if (player == null) return;
        
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
     * Updates the camera after input processing.
     * Note: Rendering is handled by the main game loop to avoid excessive repaints.
     */
    private void updateGameView() {
        // Safety checks to prevent crashes
        if (gameState == null || gamePanel == null) return;
        
        AbstractPlayer player = gameState.getPlayer();
        GameMap map = gameState.getCurrentMap();
        
        if (player == null || map == null) return;
        
        // Update camera to follow player
        gamePanel.getCamera().setMapSize(map.getWidth(), map.getHeight());
        gamePanel.getCamera().centerOn(player.getX(), player.getY());
        
        // Let the main game loop handle repainting to avoid excessive render calls
        // gamePanel.repaint(); // Removed - handled by game timer
    }
}
