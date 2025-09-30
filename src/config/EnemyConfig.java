package config;

import enemies.EnemyType;

public class EnemyConfig {

    public static class EnemyStats {
        public final int baseHp, baseDamage, expReward, movementSpeed, 
                        attackCooldown, noticeRadius, diceSides, 
                        variationPercent;
        public final int[] attackChances, attackDamageMultipliers;

        public EnemyStats(int baseHp, int baseDamage, int expReward, int movementSpeed,
                        int attackCooldown, int noticeRadius, int diceSides,
                        int variationPercent, 
                        int[] attackChances, int[] attackDamageMultipliers) {
            this.baseHp = baseHp;
            this.baseDamage = baseDamage;
            this.expReward = expReward;
            this.movementSpeed = movementSpeed;
            this.attackCooldown = attackCooldown;
            this.noticeRadius = noticeRadius;
            this.diceSides = diceSides;
            this.variationPercent = variationPercent;
            this.attackChances = attackChances;
            this.attackDamageMultipliers = attackDamageMultipliers;
        }
    }

    public static EnemyStats getStats(EnemyType type) {
        return switch (type) {
            case SLIME -> new EnemyStats(15, 3, 5, 1,
                                        2200, 4, 6, 
                                        30,
                                        new int[]{70, 30}, new int[]{100, 150});

            case ORC -> new EnemyStats(25, 5, 10, 1,
                                    2000, 5, 6, 
                                    25,
                                    new int[]{60, 40}, new int[]{100, 140});

            case SKELETON -> new EnemyStats(20, 4, 8, 1,
                                            2000, 6, 6, 
                                            20,
                                            new int[]{75, 25}, new int[]{100, 120});

            case WEREWOLF -> new EnemyStats(30, 6, 15, 2,
                                            2000, 7, 8, 
                                            35,
                                            new int[]{50, 50}, new int[]{100, 130});

            case ARMORED_ORC -> new EnemyStats(40, 7, 20, 1,
                                            2000, 5, 8, 
                                            30,
                                            new int[]{50, 30, 20}, new int[]{100, 140, 110});

            case ARMORED_SKELETON -> new EnemyStats(35, 6, 18, 1,
                                                    1100, 6, 8, 
                                                    25,
                                                    new int[]{60, 25, 15}, new int[]{100, 120, 160});

            case ELITE_ORC -> new EnemyStats(45, 8, 25, 1,
                                            2000, 6, 10, 
                                            40,
                                            new int[]{40, 35, 25}, new int[]{100, 130, 170});

            case GREATSWORD_SKELETON -> new EnemyStats(50, 9, 30, 1,
                                                    2000, 5, 12, 
                                                    45,
                                                    new int[]{30, 40, 30}, new int[]{100, 150, 200});

            case ORC_RIDER -> new EnemyStats(38, 7, 22, 2,
                                            2000, 8, 8, 
                                            35,
                                            new int[]{45, 35, 20}, new int[]{100, 120, 160});

            case WEREBEAR -> new EnemyStats(60, 10, 35, 1,
                                        2000, 6, 12, 
                                        50,
                                        new int[]{40, 35, 25}, new int[]{100, 140, 180});

            case MEDUSA_OF_CHAOS -> new EnemyStats(200, 15, 100, 1,
                                                2000, 10, 20, 
                                                60,
                                                new int[]{30, 35, 35}, new int[]{100, 160, 220});
        };
    }

    /** Base movement cooldown in milliseconds (can be modified by movement speed). */
    public static int getBaseMovementCooldown() {
        return 800;
    }
}
