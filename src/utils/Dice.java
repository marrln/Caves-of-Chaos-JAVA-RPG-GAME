package utils;

import java.util.Random;

/**
 * Utility class for dice-based calculations in combat and game mechanics.
 * Provides methods for rolling dice, calculating damage ranges, and handling
 * probability-based events 
 */
public class Dice {
    private static final Random random = new Random();
    
    public static int rolldice(int multiplier, int sides, int modifier) {
        int result = random.nextInt(sides) + 1;
        return multiplier * result + modifier;
    }
    
    public static int calculateDamage(int baseDamage, int diceSides, int damageMultiplier) {
        return rolldice(baseDamage, diceSides, damageMultiplier);
    }
    
    // Checks if a percentage-based event occurs (like critical hits).
    public static boolean checkChance(int percentage) {
        if (percentage <= 0) return false;
        if (percentage >= 100) return true;
        
        return random.nextInt(100) < percentage;
    }
    
    public static int selectAttackType(int[] attackChances) {

        int total = 0;
        for (int chance : attackChances) total += chance;

        int roll = random.nextInt(total);
        int cumulative = 0;

        for (int i = 0; i < attackChances.length; i++) {
            cumulative += attackChances[i];
            if (roll < cumulative) return i;
        }

        // Fallback (should not happen if attackChances sum > 0)
        return attackChances.length - 1;
    }
}
