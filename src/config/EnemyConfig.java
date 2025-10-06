package config;

import enemies.EnemyType;

public class EnemyConfig {

    public static class EnemyStats {
        public final int baseHp, expReward, movementSpeed, attackCooldown, noticeRadius;
        public final int[] attackChances;
        public final int[] attackDice;      // number of dice per attack
        public final int[] attackDiceSides; // sides per die
        public final int[] attackModifiers; // flat bonus per attack

        public EnemyStats(int baseHp, int expReward, int movementSpeed, int attackCooldown, 
                        int noticeRadius,
                        int[] attackChances, 
                        int[] attackDice, 
                        int[] attackDiceSides, 
                        int[] attackModifiers) {
            this.baseHp = baseHp;
            this.expReward = expReward;
            this.movementSpeed = movementSpeed;
            this.attackCooldown = attackCooldown;
            this.noticeRadius = noticeRadius;
            this.attackChances = attackChances;
            this.attackDice = attackDice;
            this.attackDiceSides = attackDiceSides;
            this.attackModifiers = attackModifiers;
        }
    }

    public static EnemyStats getStats(EnemyType type) {
        return switch (type) {
            case SLIME -> new EnemyStats(
                8, 100, 1, 
                2200, 3,
                new int[]{60, 40, 0}, // attackChances
                new int[]{1, 1, 0},   // attackDice
                new int[]{6, 8, 0},   // attackDiceSides
                new int[]{0, 0, 0}    // attackModifiers
            );
            case ORC -> new EnemyStats(
                18, 100, 1, 
                2000, 6,
                new int[]{50, 50, 0}, // attackChances
                new int[]{2, 2, 0},   // attackDice
                new int[]{6, 6, 0},   // attackDiceSides
                new int[]{0, 5, 0}    // attackModifiers
            );
            case SKELETON -> new EnemyStats(
                30, 200, 1, 
                2000, 5,
                new int[]{50, 50, 0}, // attackChances
                new int[]{1, 1, 0},   // attackDice
                new int[]{8, 8, 0},   // attackDiceSides
                new int[]{0, 5, 0}    // attackModifiers
            );
            case ARMORED_SKELETON -> new EnemyStats(
                55, 200, 1, 
                1100, 6,
                new int[]{60, 40, 0}, // attackChances
                new int[]{2, 2, 0},   // attackDice
                new int[]{6, 6, 0},   // attackDiceSides
                new int[]{0, 10, 0}   // attackModifiers
            );
            case ARMORED_ORC -> new EnemyStats(
                45, 500, 1, 
                2000, 6,
                new int[]{40, 40, 20}, // attackChances
                new int[]{2, 2, 3},    // attackDice
                new int[]{6, 6, 6},    // attackDiceSides
                new int[]{2, 2, 2}     // attackModifiers
            );
            case GREATSWORD_SKELETON -> new EnemyStats(
                65, 500, 1, 
                2000, 6,
                new int[]{40, 40, 20}, // attackChances
                new int[]{3, 3, 4},    // attackDice
                new int[]{6, 6, 6},    // attackDiceSides
                new int[]{2, 0, 5}     // attackModifiers
            );
            case WEREWOLF -> new EnemyStats(
                70, 800, 2, 
                2000, 5,
                new int[]{50, 50, 0}, // attackChances
                new int[]{4, 4, 0},   // attackDice
                new int[]{6, 6, 0},   // attackDiceSides
                new int[]{5, 10, 0}   // attackModifiers
            );
            case WEREBEAR -> new EnemyStats(
                100, 1000, 3, 
                2000, 8,
                new int[]{40, 40, 20}, // attackChances
                new int[]{2, 3, 4},    // attackDice
                new int[]{6, 6, 6},    // attackDiceSides
                new int[]{10, 0, 15}   // attackModifiers
            );
            case ELITE_ORC -> new EnemyStats(
                200, 1500, 2, 
                2000, 8,
                new int[]{40, 40, 20}, // attackChances
                new int[]{5, 4, 4},    // attackDice
                new int[]{6, 6, 6},    // attackDiceSides
                new int[]{10, 0, 5}    // attackModifiers
            );
            case ORC_RIDER -> new EnemyStats(
                280, 1500, 3, 
                2000, 8,
                new int[]{40, 40, 20}, // attackChances
                new int[]{5, 4, 4},    // attackDice
                new int[]{7, 7, 7},    // attackDiceSides
                new int[]{10, 2, 5}    // attackModifiers
            );
            case MEDUSA_OF_CHAOS -> new EnemyStats(
                1500, 10000, 4, 
                1000, 10,
                new int[]{100, 0, 0}, // attackChances
                new int[]{10, 0, 0},  // attackDice
                new int[]{6, 0, 0},   // attackDiceSides
                new int[]{10, 0, 0}   // attackModifiers
            );
        };
    }

    public static int getBaseMovementCooldown() {
        return 800;
    }
}
