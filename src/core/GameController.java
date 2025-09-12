package core;

import enemies.Enemy;
import map.GameMap;
import map.Tile;
import player.AbstractPlayer;
import ui.GameUIManager;

/**
 * Controller for game logic and player actions.
 */
public class GameController {
    private GameState gameState;
    private GameUIManager uiManager;

    /**
     * Creates a new game controller with the given game state.
     * 
     * @param gameState The game state to control
     */
    public GameController(GameState gameState) {
        this.gameState = gameState;
    }
    
    /**
     * Sets the UI manager to use for boundary checking.
     * This is called from the Game class after all UI components are initialized.
     * 
     * @param uiManager The UI manager to use
     */
    public void setUIManager(GameUIManager uiManager) {
        this.uiManager = uiManager;
    }

    /**
     * Attempts to move the player in the specified direction.
     * Validates the move against map boundaries, wall tiles, and UI boundaries.
     * 
     * @param dx The change in x position (-1, 0, or 1)
     * @param dy The change in y position (-1, 0, or 1)
     * @return true if the player moved, false otherwise
     */
    public boolean movePlayer(int dx, int dy) {
        AbstractPlayer player = gameState.getPlayer();
        int newX = player.getX() + dx;
        int newY = player.getY() + dy;
        GameMap map = gameState.getCurrentMap();
        
        // Check map boundaries
        if (newX < 0 || newY < 0 || newX >= map.getWidth() || newY >= map.getHeight()) {
            return false;
        }
        
        // Check UI boundaries if a UI manager is set
        if (uiManager != null && !uiManager.isPositionVisible(newX, newY)) {
            return false;
        }
        
        // Check tile type
        Tile tile = map.getTile(newX, newY);
        if (tile == null) {
            return false;
        }
        
        switch (tile.getType()) {
            case Tile.FLOOR -> {
                // Use collision-aware movement from AbstractPlayer
                if (player.tryMoveTo(newX, newY)) {
                    gameState.updateFogOfWar();
                    return true;
                } else {
                    return false; // Movement blocked by collision system
                }
            }
            case Tile.STAIRS_DOWN -> {
                // Move to next level if possible (using config-defined max levels)
                if (gameState.canGoToNextLevel()) {
                    gameState.goToNextLevel();
                    return true;
                }
                return false;
            }
            case Tile.STAIRS_UP -> {
                // Move to previous level if possible
                if (gameState.getCurrentLevel() > 0) {
                    gameState.goToPreviousLevel();
                    return true;
                }
                return false;
            }
            default -> {
                return false;
            }
        }
    }
    
    /**
     * Resting: restore health/mana for the player.
     * In a roguelike, this typically advances time and may trigger random events.
     */
    public void rest() {
        AbstractPlayer player = gameState.getPlayer();
        player.rest();
        // TODO: Add logic for possible enemy spawn on rest
    }

    /**
     * Perform an attack of the specified type.
     * 
     * @param attackType The type of attack (1 or 2)
     */
    public void attack(int attackType) {
        AbstractPlayer player = gameState.getPlayer();
        
        // Start the player's attack animation/state
        player.attack(attackType);
        
        // Find enemies in cardinal directions (N, S, E, W) and attack them
        int playerX = player.getX();
        int playerY = player.getY();
        
        // Check all four cardinal directions for enemies
        int[][] directions = {{0, -1}, {1, 0}, {0, 1}, {-1, 0}}; // N, E, S, W
        
        for (int[] dir : directions) {
            int targetX = playerX + dir[0];
            int targetY = playerY + dir[1];
            
            // Find enemy at this position
            Enemy targetEnemy = findEnemyAt(targetX, targetY);
            if (targetEnemy != null) {
                // Calculate player's attack damage
                int damage = player.getAttackDamage();
                
                gameState.logCombatMessage(player.getName() + " attacks " + targetEnemy.getName() + 
                                         " for " + damage + " damage!");
                
                // Deal damage to the enemy
                boolean enemyDied = targetEnemy.takeDamage(damage);
                
                if (enemyDied) {
                    gameState.logCombatMessage(targetEnemy.getName() + " has been defeated!");
                    // Player gains experience
                    int exp = targetEnemy.getExpReward();
                    player.addExperience(exp);
                    gameState.logCombatMessage(player.getName() + " gains " + exp + " experience!");
                }
                
                // Only attack one enemy per attack action
                return;
            }
        }
        
        gameState.logCombatMessage(player.getName() + " swings at empty air!");
    }
    
    /**
     * Finds an enemy at the specified position.
     */
    private Enemy findEnemyAt(int x, int y) {
        for (Enemy enemy : gameState.getCurrentEnemies()) {
            if (!enemy.isDead() && enemy.getX() == x && enemy.getY() == y) {
                return enemy;
            }
        }
        return null;
    }

    /**
     * Use an item from the specified inventory slot.
     * 
     * @param slot The inventory slot (1-9)
     */
    public void useItem(int slot) {
        AbstractPlayer player = gameState.getPlayer();
        player.useItem(slot);
        // TODO: Remove item if consumed, update inventory
    }
}
