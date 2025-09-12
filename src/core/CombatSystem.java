package core;

import enemies.Enemy;
import java.util.List;
import player.AbstractPlayer;
import utils.Dice;

/**
 * Handles all combat interactions between players and enemies.
 * This includes damage calculation, range checking, and combat state management.
 */
public class CombatSystem {
    
    // Configuration constants
    private static final int DAMAGE_TIMING_PERCENT = 60; // Damage dealt at 60% through attack animation
    private static final int ADJACENCY_RANGE = 1;        // Melee range in tiles
    
    /**
     * Represents the result of a combat action.
     */
    public static class CombatResult {
        public final boolean hit;
        public final int damage;
        public final boolean critical;
        public final boolean targetDied;
        public final String attackName;
        
        public CombatResult(boolean hit, int damage, boolean critical, boolean targetDied, String attackName) {
            this.hit = hit;
            this.damage = damage;
            this.critical = critical;
            this.targetDied = targetDied;
            this.attackName = attackName;
        }
        
        public static CombatResult miss(String attackName) {
            return new CombatResult(false, 0, false, false, attackName);
        }
        
        public static CombatResult hit(int damage, boolean critical, boolean targetDied, String attackName) {
            return new CombatResult(true, damage, critical, targetDied, attackName);
        }
    }
    
    /**
     * Checks if two positions are adjacent (within melee range).
     * Only allows N-E-S-W directions, no diagonal attacks.
     * 
     * @param x1 First position x
     * @param y1 First position y
     * @param x2 Second position x
     * @param y2 Second position y
     * @return true if positions are adjacent in cardinal directions
     */
    public static boolean isAdjacent(int x1, int y1, int x2, int y2) {
        // Use centralized line utilities for consistent behavior
        return utils.LineUtils.isCardinallyAdjacent(x1, y1, x2, y2);
    }
    
    /**
     * Calculates the distance between two positions.
     * 
     * @param x1 First position x
     * @param y1 First position y
     * @param x2 Second position x
     * @param y2 Second position y
     * @return The distance between the positions
     */
    public static double calculateDistance(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Attempts a player attack against enemies in range.
     * 
     * @param player The attacking player
     * @param attackType The type of attack (1 or 2)
     * @param enemies List of all enemies to check for targets
     * @return The result of the combat action, or null if no valid target
     */
    public static CombatResult playerAttack(AbstractPlayer player, int attackType, List<Enemy> enemies) {
        // Find adjacent enemies
        Enemy target = null;
        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && isAdjacent(player.getX(), player.getY(), enemy.getX(), enemy.getY())) {
                target = enemy;
                break; // Attack the first adjacent enemy found
            }
        }
        
        if (target == null) {
            return null; // No valid target
        }
        
        // Get attack configuration based on player type
        String attackName = getPlayerAttackName(player, attackType);
        
        // Calculate damage and check for critical hit
        int baseDamage = getPlayerAttackDamage(player, attackType);
        boolean isCritical = checkPlayerCriticalHit(player, attackType);
        
        if (isCritical) {
            baseDamage = (baseDamage * getPlayerCriticalMultiplier(player)) / 100;
        }
        
        // Apply damage to target
        boolean targetDied = target.takeDamage(baseDamage);
        
        return CombatResult.hit(baseDamage, isCritical, targetDied, attackName);
    }
    
    /**
     * Attempts an enemy attack against the player.
     * 
     * @param enemy The attacking enemy
     * @param player The target player
     * @return The result of the combat action, or null if not in range
     */
    public static CombatResult enemyAttack(Enemy enemy, AbstractPlayer player) {
        // Check if player is adjacent
        if (!isAdjacent(enemy.getX(), enemy.getY(), player.getX(), player.getY())) {
            return null; // Not in range
        }
        
        // Enemy selects attack type and calculates damage
        int damage = enemy.getAttackDamage();
        String attackName = "Attack"; // Default name, could be enhanced later
        
        // Apply damage to player
        boolean playerDied = applyDamageToPlayer(player, damage);
        
        return CombatResult.hit(damage, false, playerDied, attackName);
    }
    
    /**
     * Checks if an enemy should notice the player based on distance.
     * 
     * @param enemy The enemy to check
     * @param playerX The player's x position
     * @param playerY The player's y position
     * @return true if the enemy should notice the player
     */
    public static boolean shouldEnemyNoticePlayer(Enemy enemy, int playerX, int playerY) {
        double distance = calculateDistance(enemy.getX(), enemy.getY(), playerX, playerY);
        // This would need to be enhanced to get notice radius from enemy config
        return distance <= 5.0; // Default notice radius
    }
    
    // Helper methods for player combat calculations
    
    private static String getPlayerAttackName(AbstractPlayer player, int attackType) {
        if (player instanceof player.Duelist) {
            return attackType == 1 ? "Quick Strike" : "Power Attack";
        } else if (player instanceof player.Wizard) {
            return attackType == 1 ? "Fire Spell" : "Ice Spell";
        }
        return "Attack";
    }
    
    private static int getPlayerAttackDamage(AbstractPlayer player, int attackType) {
        // This would integrate with the player config system
        // For now, return a basic calculation
        if (player instanceof player.Duelist) {
            return attackType == 1 ? 10 : 15; // Quick vs Power attack
        } else if (player instanceof player.Wizard) {
            return attackType == 1 ? 12 : 10; // Fire vs Ice spell
        }
        return 8;
    }
    
    private static boolean checkPlayerCriticalHit(AbstractPlayer player, int attackType) {
        // Basic critical hit chance - would be enhanced with player config
        int critChance = 10; // 10% base chance
        if (attackType == 2) {
            critChance += 5; // Power attacks have higher crit chance
        }
        return Dice.checkChance(critChance);
    }
    
    private static int getPlayerCriticalMultiplier(AbstractPlayer player) {
        // Basic critical multiplier - would be enhanced with player config
        return player instanceof player.Duelist ? 200 : 180; // 2x for Duelist, 1.8x for Wizard
    }
    
    private static boolean applyDamageToPlayer(AbstractPlayer player, int damage) {
        // This would need to be implemented in the AbstractPlayer class
        // For now, just a placeholder
        return false;
    }
}
