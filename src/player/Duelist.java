package player;

public class Duelist extends AbstractPlayer {
    
    // ===== HP/MP Progression =====
    private static final int[][] BASE_STATS = {
        {35, 0},   // Level 1
        {60, 0},   // Level 2
        {80, 0},   // Level 3
        {90, 0},   // Level 4
        {100, 0},  // Level 5
        {140, 0}   // Level 6
    };

    // ===== Attack Progression =====
    // [levelIndex][attackIndex] = {diceCount, diceSides, diceBonus, mpCost, cooldown}
    private static final int[][][] ATTACK_TABLE = {
        {{1, 8, 0, 0, 3000}, {1, 8, 5, 0, 6000}},  // Level 1
        {{2, 6, 1, 0, 2800}, {2, 6, 6, 0, 5500}},  // Level 2
        {{2, 6, 2, 0, 2600}, {2, 6, 7, 0, 5000}},  // Level 3
        {{2, 8, 3, 0, 2400}, {2, 8, 8, 0, 4500}},  // Level 4
        {{3, 6, 4, 0, 2200}, {3, 6, 9, 0, 4200}},  // Level 5
        {{3, 8, 5, 0, 2000}, {3, 8, 10, 0, 4000}}  // Level 6
    };

    private static final String[] ATTACK_NAMES = {"Quick Strike", "Power Attack"};

    public Duelist(int x, int y) {
        super(x, y);
        this.name = "Duelist";
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

    public static String getAttackName(int attackType) {
        return switch (attackType) {
            case 1 -> ATTACK_NAMES[0];
            case 2 -> ATTACK_NAMES[1];
            default -> "Unknown Attack";
        };
    }
}
