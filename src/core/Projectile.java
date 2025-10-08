package core;

import enemies.Enemy;
import java.util.ArrayList;
import java.util.List;
import map.FogOfWar;
import map.GameMap;
import map.Tile;
import utils.GeometryHelpers;

/**
 * Represents a magical projectile that travels across the map to hit targets.
 */
public class Projectile {

    public enum ProjectileType {
        FIRE_SPELL("fire_spell", 12, 8.0, 12),
        ICE_SPELL("ice_spell", 10, 6.0, 10);

        private final String spriteId;
        private final int baseDamage;
        private final double speed;
        private final int range;

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
    private boolean active;
    private Enemy lockedTarget;
    private List<GeometryHelpers.Position> path;
    private int pathIndex;

    public Projectile(ProjectileType type, double startX, double startY, double targetX, double targetY) {
        this.type = type;
        this.x = startX; this.y = startY;
        this.startX = startX; this.startY = startY;
        this.targetX = targetX; this.targetY = targetY;
        this.active = true; this.pathIndex = 0;
    }

    public Projectile(ProjectileType type, double startX, double startY, Enemy target) {
        this(type, startX, startY, target.getX(), target.getY());
        this.lockedTarget = target;
    }

    private boolean checkCollision(List<Enemy> enemies) {
        for (Enemy e : enemies) {
            if (!e.isDead() && GeometryHelpers.isCardinallyAdjacent((int)Math.round(x),(int)Math.round(y), e.getX(), e.getY())) {
                hitTarget(e);
                active = false;
                return true;
            }
        }
        return false;
    }

    private boolean checkRangeAndTile(GameMap map) {
        if (GeometryHelpers.getEuclideanDistance(x, y, startX, startY) > type.getRange()) { 
            active = false; 
            return true; 
        }
        int tileX = (int)Math.round(x), tileY = (int)Math.round(y);
        if (tileX < 0 || tileY < 0 || tileX >= map.getWidth() || tileY >= map.getHeight()) { active = false; return true; }
        Tile tile = map.getTile(tileX, tileY);
        if (tile.getType() == Tile.WALL) { active = false; return true; }
        return false;
    }

    public void update(double deltaTime, GameMap map, List<Enemy> enemies, FogOfWar fog) {
        if (!active) return;
        if (checkCollision(enemies)) return;

        // Homing logic
        if (lockedTarget != null) {
            if (lockedTarget.isDead()) lockedTarget = getClosestVisibleEnemy(x, y, enemies, fog);
            else if (!fog.isVisible((int)lockedTarget.getX(), (int)lockedTarget.getY())) lockedTarget = null;
            else { targetX = lockedTarget.getX(); targetY = lockedTarget.getY(); }
        }

        // Movement
        double moveDistance = type.getSpeed() * deltaTime;
        if (path != null) moveAlongPath(moveDistance);
        else moveTowards(moveDistance);

        if (checkRangeAndTile(map)) return;
        checkCollision(enemies);
    }

    private void moveAlongPath(double moveDistance) {
        while (moveDistance > 0 && pathIndex < path.size()) {
            GeometryHelpers.Position next = path.get(pathIndex);
            double dist = GeometryHelpers.getEuclideanDistance(x, y, next.x, next.y);
            if (dist <= moveDistance) { x = next.x; y = next.y; pathIndex++; moveDistance -= dist; }
            else { 
                double[] pos = GeometryHelpers.moveTowards(x, y, next.x, next.y, moveDistance); 
                x = pos[0]; 
                y = pos[1]; 
                moveDistance = 0; 
            }
        }
        if (pathIndex >= path.size()) active = false;
    }

    private void moveTowards(double moveDistance) {
        double[] pos = GeometryHelpers.moveTowards(x, y, targetX, targetY, moveDistance);
        x = pos[0];
        y = pos[1];
    }

    private Enemy getClosestVisibleEnemy(double fromX, double fromY, List<Enemy> enemies, FogOfWar fog) {
        Enemy closest = null;
        double minDist = Double.MAX_VALUE;
        for (Enemy e : enemies) {
            if (e.isDead() || !fog.isVisible(e.getX(), e.getY())) continue;
            double dist = GeometryHelpers.getEuclideanDistance(fromX, fromY, e.getX(), e.getY());
            if (dist < minDist) { minDist = dist; closest = e; }
        }
        return closest;
    }

    private void hitTarget(Enemy enemy) {
        player.AbstractPlayer player = core.GameState.getInstance().getPlayer();
        int dmg = player.getTotalAttackDamage(type == ProjectileType.FIRE_SPELL ? 1 : 2);
        boolean dead = enemy.takeDamage(dmg);
        
        EventLogger logger = core.GameState.getInstance().getLogger();
        if (logger != null) {
            logger.logMeleeAttack(player.getName(), enemy.getName(), dmg);
            
            // Handle enemy defeat - grant XP and log
            if (dead) {
                int exp = enemy.getExpReward();
                int levels = player.addExperience(exp);
                logger.logEnemyDefeated(enemy.getName(), exp);
                if (levels > 0) {
                    logger.logLevels(player, levels);
                }
            }
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isActive() { return active; }
    public void deactivate() { active = false; }
    public ProjectileType getType() { return type; }
}

/**
 * Manager for projectiles
 */
class ProjectileManager {
    private final List<Projectile> activeProjectiles = new ArrayList<>();
    public void addProjectile(Projectile p) { activeProjectiles.add(p); }

    public void updateAll(double deltaTime, GameMap map, List<Enemy> enemies, FogOfWar fog) {
        List<Projectile> toRemove = new ArrayList<>();
        for (Projectile p : activeProjectiles) { p.update(deltaTime,map,enemies,fog); if (!p.isActive()) toRemove.add(p); }
        activeProjectiles.removeAll(toRemove);
    }

    public List<Projectile> getActiveProjectiles() { return new ArrayList<>(activeProjectiles); }
    public void clearAll() { activeProjectiles.clear(); }
}
