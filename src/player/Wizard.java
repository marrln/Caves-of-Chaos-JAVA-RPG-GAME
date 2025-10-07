package player;

import core.Projectile;
import enemies.Enemy;
import java.util.List;

public class Wizard extends AbstractPlayer {
    
    // ===== HP/MP Progression =====
    private static final int[][] BASE_STATS = {
        {20, 30},   // Level 1
        {40, 50},   // Level 2
        {50, 70},   // Level 3
        {55, 90},   // Level 4
        {60, 110},  // Level 5
        {80, 140}   // Level 6
    };

    // ===== Attack Progression =====
    // [levelIndex][attackIndex] = {diceCount, diceSides, diceBonus, mpCost, cooldown}
    private static final int[][][] ATTACK_TABLE = {
        {{1, 6, 0, 5, 3000}, {1, 6, 5, 7, 6000}},    // Level 1
        {{2, 6, 1, 7, 2800}, {2, 6, 6, 8, 5500}},    // Level 2
        {{2, 6, 2, 8, 2600}, {2, 6, 7, 10, 5000}},   // Level 3
        {{3, 6, 3, 9, 2400}, {3, 6, 8, 12, 4500}},   // Level 4
        {{4, 8, 4, 12, 2200}, {4, 8, 9, 15, 4200}},  // Level 5
        {{4, 6, 5, 14, 2000}, {4, 6, 10, 18, 4000}}  // Level 6
    };

    private static final String[] ATTACK_NAMES = {"Fire Spell", "Ice Spell"};

    public Wizard(int x, int y) {
        super(x, y);
        this.name = "Wizard";
    }

    @Override
    protected PlayerLevelStats getLevelStats(int level) {
        int idx = Math.max(0, Math.min(level - 1, BASE_STATS.length - 1));

        int maxHp = BASE_STATS[idx][0];
        int maxMp = BASE_STATS[idx][1];
        int expToNext = getExpToNextLevel(level);

        int[][] atkData = ATTACK_TABLE[idx];
        AttackConfig[] attacks = new AttackConfig[] {
            new AttackConfig("Attack01", ATTACK_NAMES[0], atkData[0][0], atkData[0][1], atkData[0][2], atkData[0][3], atkData[0][4]),
            new AttackConfig("Attack02", ATTACK_NAMES[1], atkData[1][0], atkData[1][1], atkData[1][2], atkData[1][3], atkData[1][4])
        };

        return new PlayerLevelStats(maxHp, maxMp, expToNext, attacks);
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
