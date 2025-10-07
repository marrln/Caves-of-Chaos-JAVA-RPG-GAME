package core;

import enemies.Enemy;
import java.util.ArrayList;
import java.util.List;
import map.GameMap;
import map.Tile;
import map.FogOfWar;
import utils.LineUtils;

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
    private double directionX, directionY;
    private boolean active;
    private Enemy lockedTarget;
    private List<LineUtils.Point> path;
    private int pathIndex;

    public Projectile(ProjectileType type, double startX, double startY, double targetX, double targetY) {
        this.type = type;
        this.x = startX; this.y = startY;
        this.startX = startX; this.startY = startY;
        this.targetX = targetX; this.targetY = targetY;
        this.active = true; this.pathIndex = 0;
        updateDirection();
    }

    public Projectile(ProjectileType type, double startX, double startY, Enemy target) {
        this(type, startX, startY, target.getX(), target.getY());
        this.lockedTarget = target;
    }

    private void updateDirection() {
        double dx = targetX - x, dy = targetY - y;
        double dist = Math.sqrt(dx*dx + dy*dy);
        if (dist > 0) { directionX = dx / dist; directionY = dy / dist; }
        else { directionX = 0; directionY = 0; }
    }

    private boolean checkCollision(List<Enemy> enemies) {
        for (Enemy e : enemies) {
            if (!e.isDead() && LineUtils.isCardinallyAdjacent((int)Math.round(x),(int)Math.round(y), e.getX(), e.getY())) {
                hitTarget(e);
                active = false;
                return true;
            }
        }
        return false;
    }

    private boolean checkRangeAndTile(GameMap map) {
        if (!LineUtils.withinRange(x, y, startX, startY, type.getRange())) { active = false; return true; }
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
            if (lockedTarget.isDead()) lockedTarget = LineUtils.getClosestVisibleEnemy(x, y, enemies, fog);
            else if (!LineUtils.isVisible(fog, lockedTarget)) lockedTarget = null;
            else { targetX = lockedTarget.getX(); targetY = lockedTarget.getY(); updateDirection(); }
        }

        // Movement
        double moveDistance = type.getSpeed() * deltaTime;
        if (path != null) moveAlongPath(moveDistance);
        else { double[] pos = LineUtils.moveTowards(x, y, targetX, targetY, moveDistance); x = pos[0]; y = pos[1]; }

        if (checkRangeAndTile(map)) return;
        checkCollision(enemies);
    }

    private void moveAlongPath(double moveDistance) {
        while (moveDistance > 0 && pathIndex < path.size()) {
            LineUtils.Point next = path.get(pathIndex);
            double dx = next.x - x, dy = next.y - y, dist = Math.sqrt(dx*dx + dy*dy);
            if (dist <= moveDistance) { x = next.x; y = next.y; pathIndex++; moveDistance -= dist; }
            else { x += dx / dist * moveDistance; y += dy / dist * moveDistance; moveDistance = 0; }
        }
        if (pathIndex >= path.size()) active = false;
    }

    private void hitTarget(Enemy enemy) {
        player.AbstractPlayer player = core.GameState.getInstance().getPlayer();
        int dmg = player.getTotalAttackDamage(type == ProjectileType.FIRE_SPELL ? 1 : 2);
        enemy.takeDamage(dmg);
        log(player.getName() + " attacks " + enemy.getName() + " for " + dmg + " damage!");
    }

    private void log(String msg) { core.GameState.getInstance().logMessage(msg); }

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
