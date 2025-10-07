package config;

/**
 * Central player progression and balance configuration.
 */
public class PlayerConfig {

    // ===== XP & Leveling =====
    public static final int[] LEVEL_XP_THRESHOLDS = {0, 700, 1500, 2700, 6500, 14000};
    public static final int MAX_LEVEL = LEVEL_XP_THRESHOLDS.length;

    // ===== Base Stats =====
    private static final int[][] DUELIST_STATS = {
        {35, 0}, {60, 0}, {80, 0}, {90, 0}, {100, 0}, {140, 0}
    };
    private static final int[][] WIZARD_STATS = {
        {20, 30}, {40, 50}, {50, 70}, {55, 90}, {60, 110}, {80, 140}
    };

    // ===== Attack Tables =====
    // [levelIndex][attackIndex] = {diceCount, diceSides, diceBonus, mpCost, cooldown}
    private static final int[][][] DUELIST_ATTACKS = {
        {{1, 8, 0, 0, 3000}, {1, 8, 5, 0, 6000}},
        {{2, 6, 1, 0, 2800}, {2, 6, 6, 0, 5500}},
        {{2, 6, 2, 0, 2600}, {2, 6, 7, 0, 5000}},
        {{2, 8, 3, 0, 2400}, {2, 8, 8, 0, 4500}},
        {{3, 6, 4, 0, 2200}, {3, 6, 9, 0, 4200}},
        {{3, 8, 5, 0, 2000}, {3, 8, 10, 0, 4000}}
    };

    private static final int[][][] WIZARD_ATTACKS = {
        {{1, 6, 0, 5, 3000}, {1, 6, 5, 7, 6000}},
        {{2, 6, 1, 7, 2800},  {2, 6, 6, 8, 5500}},
        {{2, 6, 2, 8, 2600},  {2, 6, 7, 10, 5000}},
        {{3, 6, 3, 9, 2400},  {3, 6, 8, 12, 4500}},
        {{4, 8, 4, 12, 2200}, {4, 8, 9, 15, 4200}},
        {{4, 6, 5, 14, 2000}, {4, 6, 10, 18, 4000}}
    };

    // ===== XP Helpers =====
    public static int getLevelForXp(int xp) {
        for (int i = LEVEL_XP_THRESHOLDS.length - 1; i >= 0; i--) {
            if (xp >= LEVEL_XP_THRESHOLDS[i]) return i + 1;
        }
        return 1;
    }

    public static int[] getXpRangeForLevel(int level) {
        int idx = Math.max(0, Math.min(level - 1, LEVEL_XP_THRESHOLDS.length - 1));
        int minXp = LEVEL_XP_THRESHOLDS[idx];
        int maxXp = (idx + 1 < LEVEL_XP_THRESHOLDS.length) ? LEVEL_XP_THRESHOLDS[idx + 1] - 1 : Integer.MAX_VALUE;
        return new int[]{minXp, maxXp};
    }

    // ===== Stats Classes =====
    public static class PlayerLevelStats {
        public final int maxHp, maxMp, expToNextLevel;
        public final AttackConfig[] attacks;

        public PlayerLevelStats(int maxHp, int maxMp, int expToNextLevel, AttackConfig[] attacks) {
            this.maxHp = maxHp;
            this.maxMp = maxMp;
            this.expToNextLevel = expToNextLevel;
            this.attacks = attacks;
        }
    }

    public static class AttackConfig {
        public final String logicalName, displayName;
        public final int diceCount, diceSides, diceBonus, mpCost, cooldown;

        public AttackConfig(String logicalName, String displayName, int diceCount, int diceSides, int diceBonus, int mpCost, int cooldown) {
            this.logicalName = logicalName;
            this.displayName = displayName;
            this.diceCount = diceCount;
            this.diceSides = diceSides;
            this.diceBonus = diceBonus;
            this.mpCost = mpCost;
            this.cooldown = cooldown;
        }
    }

    // ===== Unified Generator (fixed: accepts level) =====
    private static PlayerLevelStats buildStats(int[][] baseStats, int[][][] attackTable, int level, String atk1Name, String atk2Name) {
        int lvl = Math.max(1, Math.min(level, MAX_LEVEL));
        int idx = Math.max(0, Math.min(lvl - 1, baseStats.length - 1));

        int maxHp = baseStats[idx][0];
        int maxMp = baseStats[idx][1];

        // expToNext: for last level, define as 0 (no next level)
        int expToNext;
        if (idx + 1 < LEVEL_XP_THRESHOLDS.length) {
            expToNext = LEVEL_XP_THRESHOLDS[idx + 1] - LEVEL_XP_THRESHOLDS[idx];
        } else {
            expToNext = 0;
        }

        int[][] atkData = attackTable[idx];
        AttackConfig[] attacks = new AttackConfig[] {
            new AttackConfig("Attack01", atk1Name, atkData[0][0], atkData[0][1], atkData[0][2], atkData[0][3], atkData[0][4]),
            new AttackConfig("Attack02", atk2Name, atkData[1][0], atkData[1][1], atkData[1][2], atkData[1][3], atkData[1][4])
        };

        return new PlayerLevelStats(maxHp, maxMp, expToNext, attacks);
    }

    // ===== Public API =====
    public static PlayerLevelStats getDuelistStats(int level) {
        return buildStats(DUELIST_STATS, DUELIST_ATTACKS, level, "Quick Strike", "Power Attack");
    }

    public static PlayerLevelStats getWizardStats(int level) {
        return buildStats(WIZARD_STATS, WIZARD_ATTACKS, level, "Fire Spell", "Ice Spell");
    }

    public static double[] getRestConfiguration() {
        return new double[]{0.05, 0.05}; // 5% HP, 8% MP per rest
    }

    public static int getMaxLevel() {
        return MAX_LEVEL;
    }
}
