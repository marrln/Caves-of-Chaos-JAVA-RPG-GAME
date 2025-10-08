package player;

import core.Projectile;
import enemies.Enemy;
import java.util.List;
import player.AbstractPlayer.AttackConfig;
import player.AbstractPlayer.PlayerLevelStats;

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
        {{2, 6, 2, 5, 3000}, {3, 6, 5, 10, 6000}},   // Level 1
        {{2, 6, 4, 6, 2800}, {3, 8, 7, 12, 5800}},   // Level 2
        {{3, 6, 5, 7, 2600}, {3, 8, 10, 14, 5500}},  // Level 3
        {{3, 8, 6, 8, 2400}, {4, 8, 12, 16, 5200}},  // Level 4
        {{4, 8, 7, 9, 2200}, {4, 10, 14, 18, 4900}}, // Level 5
        {{4, 10, 8, 10, 2000}, {5, 10, 17, 20, 4500}}// Level 6
    };


    private static final String[] ATTACK_NAMES = {"Fire Spell", "Ice Spell"};

    public Wizard(int x, int y) {
        super(x, y);
        this.name = "Wizard";
    }

    @Override
    protected PlayerLevelStats getLevelStats(int level) {
        int idx = Math.max(0, Math.min(level - 1, BASE_STATS.length - 1));

        int baseMaxHp = BASE_STATS[idx][0];
        int baseMaxMp = BASE_STATS[idx][1];
        int expToNextLevel = getExpToNextLevel(level);

        int[][] atkData = ATTACK_TABLE[idx];
        AttackConfig[] attacks = new AttackConfig[] {
            new AttackConfig("Attack01", ATTACK_NAMES[0], atkData[0][0], atkData[0][1], atkData[0][2], atkData[0][3], atkData[0][4]),
            new AttackConfig("Attack02", ATTACK_NAMES[1], atkData[1][0], atkData[1][1], atkData[1][2], atkData[1][3], atkData[1][4])
        };

        return new PlayerLevelStats(baseMaxHp, baseMaxMp, expToNextLevel, attacks);
    }

    public static String getAttackName(int attackType) {
        return switch (attackType) {
            case 1 -> ATTACK_NAMES[0];
            case 2 -> ATTACK_NAMES[1];
            default -> "Unknown Spell";
        };
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
