package core;

import enemies.Enemy;
import java.util.ArrayList;
import java.util.List;
import map.GameMap;
import map.Tile;
import utils.LineUtils;

/**
 * Represents a magical projectile that travels across the map to hit targets.
 * Used by wizards for ranged spell attacks.
 */
public class Projectile {

    public enum ProjectileType {
        // TODO: remove hardcoded values and make the spells configurable
        FIRE_SPELL("fire_spell", 12, 8.0, 12),
        ICE_SPELL("ice_spell", 10, 6.0, 10);

        private final String spriteId;
        private final int baseDamage;
        private final double speed; // tiles per second
        private final int range;    // max travel distance in tiles

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
    private double x, y;
    private final double startX, startY;
    private double targetX, targetY;
    private double directionX, directionY;
    private boolean active;
    private Enemy lockedTarget;
    private List<LineUtils.Point> path; // Precomputed path for non-homing projectiles
    private int pathIndex;

    public Projectile(ProjectileType type, double startX, double startY, double targetX, double targetY) {
        this.type = type;
        this.x = startX;
        this.y = startY;
        this.startX = startX;
        this.startY = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.active = true;
        this.pathIndex = 0;
        precomputePath(null); // Non-homing projectile
        updateDirection();
    }

    public Projectile(ProjectileType type, double startX, double startY, Enemy target) {
        this(type, startX, startY, target.getX(), target.getY());
        this.lockedTarget = target;
        this.path = null; // Homing projectile doesn’t use precomputed path
    }

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

    private void precomputePath(GameMap gameMap) {
        if (gameMap == null) return;
        path = LineUtils.getProjectilePath(
                (int)Math.round(startX), (int)Math.round(startY),
                (int)Math.round(targetX), (int)Math.round(targetY),
                (x, y) -> {
                    if (x < 0 || y < 0 || x >= gameMap.getWidth() || y >= gameMap.getHeight()) return true;
                    Tile tile = gameMap.getTile(x, y);
                    return tile.getType() == Tile.WALL;
                });
        pathIndex = 0;
    }

    public void update(double deltaTime, GameMap gameMap, List<Enemy> enemies, map.FogOfWar fogOfWar) {
        if (!active) return;

        // --- Check for collision with enemies BEFORE movement ---
        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && isHitting(enemy)) {
                hitTarget(enemy);
                active = false;
                return;
            }
        }

        // Update homing target
        if (lockedTarget != null && !lockedTarget.isDead()) {
            if (isEnemyVisible(lockedTarget, fogOfWar)) {
                targetX = lockedTarget.getX();
                targetY = lockedTarget.getY();
                updateDirection();
            } else {
                lockedTarget = null;
            }
        } else if (lockedTarget != null && lockedTarget.isDead()) {
            findNewTarget(enemies, fogOfWar);
        }

        // Move along precomputed path if available
        if (path != null) {
            double moveDistance = type.getSpeed() * deltaTime;
            while (moveDistance > 0 && pathIndex < path.size()) {
                LineUtils.Point next = path.get(pathIndex);
                double dx = next.x - x;
                double dy = next.y - y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist <= moveDistance) {
                    x = next.x;
                    y = next.y;
                    pathIndex++;
                    moveDistance -= dist;
                } else {
                    x += dx / dist * moveDistance;
                    y += dy / dist * moveDistance;
                    moveDistance = 0;
                }
            }
            if (pathIndex >= path.size()) active = false;
        } else {
            // Normal movement
            double moveDistance = type.getSpeed() * deltaTime;
            x += directionX * moveDistance;
            y += directionY * moveDistance;
        }

        // Check range
        double dx = x - startX;
        double dy = y - startY;
        if (dx*dx + dy*dy > type.getRange() * type.getRange()) {
            active = false;
            return;
        }

        // Tile collision
        int tileX = (int)Math.round(x);
        int tileY = (int)Math.round(y);
        if (tileX < 0 || tileY < 0 || tileX >= gameMap.getWidth() || tileY >= gameMap.getHeight()) {
            active = false;
            return;
        }
        Tile tile = gameMap.getTile(tileX, tileY);
        if (tile.getType() == Tile.WALL) {
            active = false;
            return;
        }

        // --- Check for collision with enemies AFTER movement ---
        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && isHitting(enemy)) {
                hitTarget(enemy);
                active = false;
                return;
            }
        }
    }

    private void findNewTarget(List<Enemy> enemies, map.FogOfWar fogOfWar) {
        Enemy closestEnemy = null;
        double closestDist = Double.MAX_VALUE;
        for (Enemy e : enemies) {
            if (!e.isDead() && isEnemyVisible(e, fogOfWar)) {
                double dist = (e.getX()-x)*(e.getX()-x) + (e.getY()-y)*(e.getY()-y);
                if (dist < closestDist) {
                    closestDist = dist;
                    closestEnemy = e;
                }
            }
        }
        if (closestEnemy != null) {
            lockedTarget = closestEnemy;
            targetX = closestEnemy.getX();
            targetY = closestEnemy.getY();
            updateDirection();
        } else {
            lockedTarget = null;
        }
    }

    private boolean isEnemyVisible(Enemy enemy, map.FogOfWar fogOfWar) {
        return fogOfWar == null || fogOfWar.isVisible(enemy.getX(), enemy.getY());
    }

    private boolean isHitting(Enemy enemy) {
        return LineUtils.isCardinallyAdjacent((int)Math.round(x), (int)Math.round(y), enemy.getX(), enemy.getY());
    }

    private void hitTarget(Enemy enemy) {
        player.AbstractPlayer player = core.GameState.getInstance().getPlayer();
        int dmg = player.getTotalAttackDamage(type == ProjectileType.FIRE_SPELL ? 1 : 2);
        boolean dead = enemy.takeDamage(dmg);
        core.GameState.getInstance().logMessage(player.getName() + " attacks " + enemy.getName() + " for " + dmg + " damage!");
        // Award XP if enemy is killed
        if (dead) {
            int exp = enemy.getExpReward();
            int levelsGained = player.addExperience(exp);
            core.GameState.getInstance().logMessage(enemy.getName() + " has been defeated! You gained " + exp + " exp!", 
                config.StyleConfig.getColor("accent")); // Gold for XP gains
            
            // Log level-up message
            if (levelsGained > 0) {
                if (levelsGained == 1) {
                    core.GameState.getInstance().logMessage(player.getName() + " reached level " + player.getLevel() + "! (HP: " + player.getMaxHp() + ", MP: " + player.getMaxMp() + ")", 
                        config.StyleConfig.getColor("victoryGold")); // Bright gold for level up!
                } else {
                    core.GameState.getInstance().logMessage(player.getName() + " gained " + levelsGained + " levels! Now level " + player.getLevel() + "!", 
                        config.StyleConfig.getColor("victoryGold")); // Bright gold for multiple levels!
                }
            }
        }
        // Optional: add effects for FIRE_SPELL or ICE_SPELL
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isActive() { return active; }
    public ProjectileType getType() { return type; }
    public void deactivate() { active = false; }
}

/**
 * Manager for all projectiles in the game.
 */
class ProjectileManager {
    private final List<Projectile> activeProjectiles = new ArrayList<>();

    public void addProjectile(Projectile p) { activeProjectiles.add(p); }

    public void updateAll(double deltaTime, GameMap gameMap, List<Enemy> enemies, map.FogOfWar fogOfWar) {
        List<Projectile> toRemove = new ArrayList<>();
        for (Projectile p : activeProjectiles) {
            p.update(deltaTime, gameMap, enemies, fogOfWar);
            if (!p.isActive()) toRemove.add(p);
        }
        activeProjectiles.removeAll(toRemove);
    }

    public List<Projectile> getActiveProjectiles() { return new ArrayList<>(activeProjectiles); }
    public void clearAll() { activeProjectiles.clear(); }
}
