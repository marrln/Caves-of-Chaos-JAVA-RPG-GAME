package player;

import config.PlayerConfig;
import core.Projectile;
import enemies.Enemy;
import java.util.List;

public class Wizard extends AbstractPlayer {
    public Wizard(int x, int y) {
        super(x, y);
        this.name = "Wizard";
    }

    @Override
    protected PlayerConfig.PlayerLevelStats getLevelStats(int level) {
        return PlayerConfig.getWizardStats(level);
    }

    public Projectile createProjectile(int attackType, List<Enemy> enemies, map.FogOfWar fogOfWar) {
        Enemy closest = findClosestVisibleEnemy(enemies, fogOfWar);
        if (closest == null) return null;

        Projectile.ProjectileType type = (attackType == 1)
            ? Projectile.ProjectileType.FIRE_SPELL
            : Projectile.ProjectileType.ICE_SPELL;

        return new Projectile(type, x, y, closest);
    }

    private Enemy findClosestVisibleEnemy(List<Enemy> enemies, map.FogOfWar fogOfWar) {
        Enemy closest = null;
        double minDist = Double.MAX_VALUE;

        for (Enemy e : enemies) {
            if (!e.isDead() && isEnemyVisible(e, fogOfWar)) {
                double d = Math.hypot(e.getX() - x, e.getY() - y);
                if (d < minDist) { minDist = d; closest = e; }
            }
        }
        return closest;
    }

    private boolean isEnemyVisible(Enemy e, map.FogOfWar fogOfWar) {
        return fogOfWar == null || fogOfWar.isVisible(e.getX(), e.getY());
    }
}
