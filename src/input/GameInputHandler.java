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
 * - Q: Primary attack (faster cooldown)
 * - E: Secondary attack (longer cooldown)
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
    private static final long MOVEMENT_COOLDOWN_MS = 200; // Minimum time between movements
    private static final long ITEM_ACTION_COOLDOWN_MS = 300; // Prevent accidental double-use of items
    private long lastMovementTime = 0;
    private long lastItemActionTime = 0;
    
    public GameInputHandler(GameState gameState, GameController controller, GamePanel gamePanel) {
        this.gameState = gameState;
        this.controller = controller;
        this.gamePanel = gamePanel;
    }
    
    public void setUIManager(GameUIManager uiManager) {
        this.uiManager = uiManager;
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        long currentTime = System.currentTimeMillis();
        int dx = 0, dy = 0;
        boolean shouldUpdate = false;
        boolean isMovement = false;
        
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
            
            // Action controls (cooldowns handled by GameController)
            case KeyEvent.VK_R -> { 
                controller.rest(); 
                shouldUpdate = true;
            }
            case KeyEvent.VK_Q -> { 
                controller.attack(1); 
                shouldUpdate = true;
            }
            case KeyEvent.VK_E -> { 
                controller.attack(2); 
                shouldUpdate = true;
            }
            
            // Item interaction controls (throttled to prevent accidental spam)
            case KeyEvent.VK_SPACE -> { 
                if (currentTime - lastItemActionTime >= ITEM_ACTION_COOLDOWN_MS) {
                    controller.pickupItem(); 
                    shouldUpdate = true;
                    lastItemActionTime = currentTime;
                }
            }
            
            // Inventory controls (1-9) - throttled to prevent accidental double-use
            case KeyEvent.VK_1 -> { 
                if (currentTime - lastItemActionTime >= ITEM_ACTION_COOLDOWN_MS) {
                    controller.useItem(1); 
                    shouldUpdate = true;
                    lastItemActionTime = currentTime;
                }
            }
            case KeyEvent.VK_2 -> { 
                if (currentTime - lastItemActionTime >= ITEM_ACTION_COOLDOWN_MS) {
                    controller.useItem(2); 
                    shouldUpdate = true;
                    lastItemActionTime = currentTime;
                }
            }
            case KeyEvent.VK_3 -> { 
                if (currentTime - lastItemActionTime >= ITEM_ACTION_COOLDOWN_MS) {
                    controller.useItem(3); 
                    shouldUpdate = true;
                    lastItemActionTime = currentTime;
                }
            }
            case KeyEvent.VK_4 -> { 
                if (currentTime - lastItemActionTime >= ITEM_ACTION_COOLDOWN_MS) {
                    controller.useItem(4); 
                    shouldUpdate = true;
                    lastItemActionTime = currentTime;
                }
            }
            case KeyEvent.VK_5 -> { 
                if (currentTime - lastItemActionTime >= ITEM_ACTION_COOLDOWN_MS) {
                    controller.useItem(5); 
                    shouldUpdate = true;
                    lastItemActionTime = currentTime;
                }
            }
            case KeyEvent.VK_6 -> { 
                if (currentTime - lastItemActionTime >= ITEM_ACTION_COOLDOWN_MS) {
                    controller.useItem(6); 
                    shouldUpdate = true;
                    lastItemActionTime = currentTime;
                }
            }
            case KeyEvent.VK_7 -> { 
                if (currentTime - lastItemActionTime >= ITEM_ACTION_COOLDOWN_MS) {
                    controller.useItem(7); 
                    shouldUpdate = true;
                    lastItemActionTime = currentTime;
                }
            }
            case KeyEvent.VK_8 -> { 
                if (currentTime - lastItemActionTime >= ITEM_ACTION_COOLDOWN_MS) {
                    controller.useItem(8); 
                    shouldUpdate = true;
                    lastItemActionTime = currentTime;
                }
            }
            case KeyEvent.VK_9 -> { 
                if (currentTime - lastItemActionTime >= ITEM_ACTION_COOLDOWN_MS) {
                    controller.useItem(9); 
                    shouldUpdate = true;
                    lastItemActionTime = currentTime;
                }
            }
            
            // Debug controls
            // case KeyEvent.VK_C -> {
            //     // Debug key: manually recenter camera on player
            //     gamePanel.centerCameraOnPlayer();
            //     return; // Don't process further
            // }
            // case KeyEvent.VK_F -> {
            //     // Debug key: toggle fog of war
            //     gamePanel.toggleFogOfWar();
            //     return; // Don't process further
            // }
            
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
        
        // Update the game view if needed
        if (shouldUpdate) {
            updateGameView();
        }
    }
    
    private void handleMovementInput(int dx, int dy) {

        AbstractPlayer player = gameState.getPlayer();        
        int newX = player.getX() + dx;
        int newY = player.getY() + dy;
        
        // Check if the position is within the visible UI boundaries
        boolean isVisible = (uiManager == null) || uiManager.isPositionVisible(newX, newY);
        
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
    }
}
