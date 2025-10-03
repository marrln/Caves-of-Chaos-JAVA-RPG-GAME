package config;

/**
 * Central player progression and balance configuration.
 */
public class PlayerConfig {

    // ===== XP & Leveling =====
    public static final int[] LEVEL_XP_THRESHOLDS = {0, 300, 900, 2700, 6500, 14000};
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
        {{1, 8, 0, 0, 800}, {1, 8, 0, 0, 1200}},
        {{2, 6, 0, 0, 700}, {2, 6, 0, 0, 1100}},
        {{2, 6, 2, 0, 700}, {2, 6, 2, 0, 1100}},
        {{2, 8, 0, 0, 650}, {2, 8, 0, 0, 1000}},
        {{3, 6, 2, 0, 600}, {3, 6, 2, 0, 900}},
        {{3, 8, 0, 0, 600}, {3, 8, 0, 0, 900}}
    };

    private static final int[][][] WIZARD_ATTACKS = {
        {{1, 6, 0, 5, 1000}, {1, 6, 0, 5, 1400}},
        {{2, 6, 0, 8, 900},  {2, 6, 0, 8, 1300}},
        {{2, 6, 2, 10, 900}, {2, 6, 2, 10, 1300}},
        {{3, 6, 0, 12, 850}, {3, 6, 0, 12, 1200}},
        {{3, 8, 0, 15, 800}, {3, 8, 0, 15, 1100}},
        {{4, 6, 4, 18, 800}, {4, 6, 4, 18, 1100}}
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
        return new double[]{0.05, 0.08}; // 5% HP, 8% MP per rest
    }

    public static int getMaxLevel() {
        return MAX_LEVEL;
    }
}
