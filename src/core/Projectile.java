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
    private List<LineUtils.Point> path; 
    private int pathIndex;

    public Projectile(ProjectileType type, double startX, double startY, double targetX, double targetY) {
        this.type = type;
        this.x = this.startX = startX;
        this.y = this.startY = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.active = true;
        this.pathIndex = 0;
        precomputePath(null);
        updateDirection();
    }

    public Projectile(ProjectileType type, double startX, double startY, Enemy target) {
        this(type, startX, startY, target.getX(), target.getY());
        this.lockedTarget = target;
        this.path = null; // homing
    }

    private void updateDirection() {
        double dx = targetX - x, dy = targetY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist > 0) { directionX = dx / dist; directionY = dy / dist; } 
        else { directionX = directionY = 0; }
    }

    private void precomputePath(GameMap map) {
        if (map == null) return;
        path = LineUtils.getProjectilePath(
            (int)Math.round(startX), (int)Math.round(startY),
            (int)Math.round(targetX), (int)Math.round(targetY),
            (tileX, tileY) -> tileX < 0 || tileY < 0 || tileX >= map.getWidth() || tileY >= map.getHeight() || map.getTile(tileX, tileY).getType() == Tile.WALL
        );
        pathIndex = 0;
    }

    public void update(double dt, GameMap map, List<Enemy> enemies, map.FogOfWar fogOfWar) {
        if (!active) return;

        // collision before movement
        for (Enemy e : enemies) if (!e.isDead() && isHitting(e)) { hitTarget(e); active = false; return; }

        // homing
        if (lockedTarget != null) {
            if (!lockedTarget.isDead() && isEnemyVisible(lockedTarget, fogOfWar)) {
                targetX = lockedTarget.getX(); targetY = lockedTarget.getY();
                updateDirection();
            } else lockedTarget = null;
        }

        // movement
        double moveDist = type.getSpeed() * dt;
        if (path != null) {
            while (moveDist > 0 && pathIndex < path.size()) {
                LineUtils.Point next = path.get(pathIndex);
                double dx = next.x - x, dy = next.y - y, dist = Math.sqrt(dx*dx + dy*dy);
                if (dist <= moveDist) { x = next.x; y = next.y; pathIndex++; moveDist -= dist; }
                else { x += dx / dist * moveDist; y += dy / dist * moveDist; moveDist = 0; }
            }
            if (pathIndex >= path.size()) { active = false; return; }
        } else { x += directionX * moveDist; y += directionY * moveDist; }

        // range
        double dx = x - startX, dy = y - startY;
        if (dx*dx + dy*dy > type.getRange()*type.getRange()) { active = false; return; }

        // tile collision
        int tileX = (int)Math.round(x), tileY = (int)Math.round(y);
        if (tileX < 0 || tileY < 0 || tileX >= map.getWidth() || tileY >= map.getHeight() || map.getTile(tileX, tileY).getType() == Tile.WALL) {
            active = false; return;
        }

        // collision after movement
        for (Enemy e : enemies) if (!e.isDead() && isHitting(e)) { hitTarget(e); active = false; return; }
    }

    private boolean isEnemyVisible(Enemy e, map.FogOfWar fogOfWar) { return fogOfWar == null || fogOfWar.isVisible(e.getX(), e.getY()); }
    private boolean isHitting(Enemy e) { return LineUtils.isCardinallyAdjacent((int)Math.round(x), (int)Math.round(y), e.getX(), e.getY()); }

    private void hitTarget(Enemy e) {
        player.AbstractPlayer player = core.GameState.getInstance().getPlayer();
        int dmg = player.getTotalAttackDamage(type == ProjectileType.FIRE_SPELL ? 1 : 2);
        boolean dead = e.takeDamage(dmg);

        log(player.getName() + " attacks " + e.getName() + " for " + dmg + " damage!");

        if (dead) {
            int exp = e.getExpReward();
            int levelsGained = player.addExperience(exp);
            log(e.getName() + " has been defeated! You gained " + exp + " exp!", config.StyleConfig.getColor("accent"));

            if (levelsGained > 0) {
                if (levelsGained == 1)
                    log(player.getName() + " reached level " + player.getLevel() + "! (HP: " + player.getMaxHp() + ", MP: " + player.getMaxMp() + ")", config.StyleConfig.getColor("victoryGold"));
                else
                    log(player.getName() + " gained " + levelsGained + " levels! Now level " + player.getLevel() + "!", config.StyleConfig.getColor("victoryGold"));
            }
        }
    }

    // ====== LOG HELPERS ======
    private void log(String msg) { core.GameState.getInstance().logMessage(msg); }
    private void log(String msg, java.awt.Color color) { core.GameState.getInstance().logMessage(msg, color); }

    // ====== GETTERS ======
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

    public void updateAll(double dt, GameMap map, List<Enemy> enemies, map.FogOfWar fogOfWar) {
        List<Projectile> toRemove = new ArrayList<>();
        for (Projectile p : activeProjectiles) {
            p.update(dt, map, enemies, fogOfWar);
            if (!p.isActive()) toRemove.add(p);
        }
        activeProjectiles.removeAll(toRemove);
    }

    public List<Projectile> getActiveProjectiles() { return new ArrayList<>(activeProjectiles); }
    public void clearAll() { activeProjectiles.clear(); }
}
