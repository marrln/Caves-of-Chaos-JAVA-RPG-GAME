package core;

import enemies.Enemy;
import java.util.ArrayList;
import java.util.List;
import map.GameMap;
import map.Tile;

/**
 * Represents a magical projectile that travels across the map to hit targets.
 * Used by wizards for ranged spell attacks.
 */
public class Projectile {
    
    /**
     * Different types of projectiles with their own properties.
     */
    public enum ProjectileType {
        FIRE_SPELL("fire_spell", 12, 1.5, 10),
        ICE_SPELL("ice_spell", 10, 1.2, 8);
        
        private final String spriteId;
        private final int baseDamage;
        private final double speed;        // Tiles per second
        private final int range;           // Maximum travel distance in tiles
        
        ProjectileType(String spriteId, int baseDamage, double speed, int range) {
            this.spriteId = spriteId;
            this.baseDamage = baseDamage;
            this.speed = speed;
            this.range = range;
        }
        
        public String getSpriteId() { return spriteId; }
        public int getBaseDamage() { return baseDamage; }
        public double getSpeed() { return speed; }
        public int getRange() { return range; }
    }
    
    private final ProjectileType type;
    private double x, y;                   // Current position (can be fractional)
    private final double startX, startY;   // Starting position
    private double targetX, targetY;       // Target position
    private double directionX, directionY; // Normalized direction vector
    private boolean active;
    private Enemy lockedTarget;            // The enemy this projectile is tracking
    
    /**
     * Creates a new projectile.
     * 
     * @param type The type of projectile
     * @param startX Starting x position
     * @param startY Starting y position
     * @param targetX Initial target x position
     * @param targetY Initial target y position
     */
    public Projectile(ProjectileType type, double startX, double startY, double targetX, double targetY) {
        this.type = type;
        this.x = startX;
        this.y = startY;
        this.startX = startX;
        this.startY = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.active = true;
        
        updateDirection();
    }
    
    /**
     * Creates a projectile that targets a specific enemy.
     * 
     * @param type The type of projectile
     * @param startX Starting x position
     * @param startY Starting y position
     * @param target The enemy to target
     */
    public Projectile(ProjectileType type, double startX, double startY, Enemy target) {
        this(type, startX, startY, target.getX(), target.getY());
        this.lockedTarget = target;
    }
    
    /**
     * Updates the direction vector based on current position and target.
     */
    private void updateDirection() {
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            directionX = dx / distance;
            directionY = dy / distance;
        } else {
            directionX = 0;
            directionY = 0;
        }
    }
    
    /**
     * Updates the projectile's position and tracking.
     * 
     * @param deltaTime Time elapsed since last update in seconds
     * @param gameMap The current game map for collision checking
     * @param enemies List of enemies for target tracking
     */
    public void update(double deltaTime, GameMap gameMap, List<Enemy> enemies) {
        if (!active) return;
        
        // Update target position if tracking a specific enemy
        if (lockedTarget != null && !lockedTarget.isDead()) {
            targetX = lockedTarget.getX();
            targetY = lockedTarget.getY();
            updateDirection();
        } else if (lockedTarget != null && lockedTarget.isDead()) {
            // Target died, find new closest enemy
            findNewTarget(enemies);
        }
        
        // Move the projectile
        double moveDistance = type.getSpeed() * deltaTime;
        x += directionX * moveDistance;
        y += directionY * moveDistance;
        
        // Check if we've traveled too far
        double totalDistance = Math.sqrt(
            (x - startX) * (x - startX) + (y - startY) * (y - startY)
        );
        if (totalDistance > type.getRange()) {
            active = false;
            return;
        }
        
        // Check wall collision
        int tileX = (int) Math.round(x);
        int tileY = (int) Math.round(y);
        if (isWallAt(gameMap, tileX, tileY)) {
            active = false;
            return;
        }
        
        // Check enemy collision
        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && isHitting(enemy)) {
                hitTarget(enemy);
                active = false;
                return;
            }
        }
        
        // Check if we've reached the target
        double distanceToTarget = Math.sqrt(
            (x - targetX) * (x - targetX) + (y - targetY) * (y - targetY)
        );
        if (distanceToTarget < 0.3) { // Close enough to target
            active = false;
        }
    }
    
    /**
     * Finds a new target when the current target dies.
     * 
     * @param enemies List of available enemies
     */
    private void findNewTarget(List<Enemy> enemies) {
        Enemy closestEnemy = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (Enemy enemy : enemies) {
            if (!enemy.isDead()) {
                double distance = Math.sqrt(
                    (enemy.getX() - x) * (enemy.getX() - x) + 
                    (enemy.getY() - y) * (enemy.getY() - y)
                );
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEnemy = enemy;
                }
            }
        }
        
        if (closestEnemy != null) {
            lockedTarget = closestEnemy;
            targetX = closestEnemy.getX();
            targetY = closestEnemy.getY();
            updateDirection();
        } else {
            // No enemies left, projectile continues in current direction
            lockedTarget = null;
        }
    }
    
    /**
     * Checks if there's a wall at the specified tile position.
     * 
     * @param gameMap The game map
     * @param tileX The x coordinate of the tile
     * @param tileY The y coordinate of the tile
     * @return true if there's a wall at this position
     */
    private boolean isWallAt(GameMap gameMap, int tileX, int tileY) {
        if (tileX < 0 || tileY < 0 || tileX >= gameMap.getWidth() || tileY >= gameMap.getHeight()) {
            return true; // Out of bounds counts as wall
        }
        
        Tile tile = gameMap.getTile(tileX, tileY);
        return tile == null || tile.getType() == Tile.WALL;
    }
    
    /**
     * Checks if the projectile is hitting the specified enemy.
     * 
     * @param enemy The enemy to check
     * @return true if the projectile is close enough to hit
     */
    private boolean isHitting(Enemy enemy) {
        double distance = Math.sqrt(
            (enemy.getX() - x) * (enemy.getX() - x) + 
            (enemy.getY() - y) * (enemy.getY() - y)
        );
        return distance < 0.5; // Hit radius
    }
    
    /**
     * Applies damage to the target enemy.
     * 
     * @param enemy The enemy to damage
     */
    private void hitTarget(Enemy enemy) {
        int damage = type.getBaseDamage();
        enemy.takeDamage(damage);
        
        // TODO: Add special effects based on projectile type
        // Fire spell: might cause burning
        // Ice spell: might slow the enemy
    }
    
    // Getters
    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isActive() { return active; }
    public ProjectileType getType() { return type; }
    
    /**
     * Manually deactivates the projectile.
     */
    public void deactivate() {
        this.active = false;
    }
}

/**
 * Manager class for handling all active projectiles in the game.
 */
class ProjectileManager {
    private final List<Projectile> activeProjectiles;
    
    public ProjectileManager() {
        this.activeProjectiles = new ArrayList<>();
    }
    
    /**
     * Adds a new projectile to the manager.
     * 
     * @param projectile The projectile to add
     */
    public void addProjectile(Projectile projectile) {
        activeProjectiles.add(projectile);
    }
    
    /**
     * Updates all active projectiles.
     * 
     * @param deltaTime Time elapsed since last update
     * @param gameMap The current game map
     * @param enemies List of enemies
     */
    public void updateAll(double deltaTime, GameMap gameMap, List<Enemy> enemies) {
        List<Projectile> toRemove = new ArrayList<>();
        
        for (Projectile projectile : activeProjectiles) {
            projectile.update(deltaTime, gameMap, enemies);
            
            if (!projectile.isActive()) {
                toRemove.add(projectile);
            }
        }
        
        activeProjectiles.removeAll(toRemove);
    }
    
    /**
     * Gets all currently active projectiles.
     * 
     * @return List of active projectiles
     */
    public List<Projectile> getActiveProjectiles() {
        return new ArrayList<>(activeProjectiles);
    }
    
    /**
     * Clears all projectiles (useful when changing levels).
     */
    public void clearAll() {
        activeProjectiles.clear();
    }
}
