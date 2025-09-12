package utils;

import java.util.Random;

/**
 * Utility class for dice-based calculations in combat and game mechanics.
 * Provides methods for rolling dice, calculating damage ranges, and handling
 * probability-based events like critical hits.
 */
public class Dice {
    private static final Random random = new Random();
    
    /**
     * Rolls a single die with the specified number of sides.
     * 
     * @param sides The number of sides on the die (must be positive)
     * @return A random number between 1 and sides (inclusive)
     */
    public static int roll(int sides) {
        if (sides <= 0) {
            throw new IllegalArgumentException("Dice must have at least 1 side");
        }
        return random.nextInt(sides) + 1;
    }
    
    /**
     * Rolls multiple dice and returns the sum.
     * 
     * @param count The number of dice to roll
     * @param sides The number of sides on each die
     * @return The sum of all dice rolls
     */
    public static int roll(int count, int sides) {
        if (count <= 0 || sides <= 0) {
            throw new IllegalArgumentException("Count and sides must be positive");
        }
        
        int total = 0;
        for (int i = 0; i < count; i++) {
            total += roll(sides);
        }
        return total;
    }
    
    /**
     * Rolls dice with a modifier (e.g., 2d6+3).
     * 
     * @param count The number of dice to roll
     * @param sides The number of sides on each die
     * @param modifier The modifier to add to the total
     * @return The sum of dice rolls plus the modifier
     */
    public static int roll(int count, int sides, int modifier) {
        return roll(count, sides) + modifier;
    }
    
    /**
     * Calculates damage based on dice configuration.
     * 
     * @param baseDamage The base damage value
     * @param diceSides The number of sides on the damage die
     * @param variationPercent The percentage of base damage that can vary (0-100)
     * @return The calculated damage value
     */
    public static int calculateDamage(int baseDamage, int diceSides, int variationPercent) {
        if (variationPercent <= 0) {
            return baseDamage;
        }
        
        int variation = (baseDamage * variationPercent) / 100;
        int diceRoll = roll(diceSides);
        
        // Map dice roll to variation range
        double rollPercent = (double) diceRoll / diceSides;
        int actualVariation = (int) (variation * rollPercent);
        
        return baseDamage + actualVariation - (variation / 2);
    }
    
    /**
     * Checks if a percentage-based event occurs (like critical hits).
     * 
     * @param percentage The chance percentage (0-100)
     * @return true if the event occurs, false otherwise
     */
    public static boolean checkChance(int percentage) {
        if (percentage <= 0) return false;
        if (percentage >= 100) return true;
        
        return random.nextInt(100) < percentage;
    }
    
    /**
     * Selects an attack type based on probability weights.
     * 
     * @param attackChances Array of chances for each attack type (should sum to 100)
     * @return The index of the selected attack type (0-based)
     */
    public static int selectAttackType(int[] attackChances) {
        if (attackChances.length == 0) {
            return 0;
        }
        
        int roll = random.nextInt(100);
        int cumulative = 0;
        
        for (int i = 0; i < attackChances.length; i++) {
            cumulative += attackChances[i];
            if (roll < cumulative) {
                return i;
            }
        }
        
        // Fallback to last attack type if weights don't sum to 100
        return attackChances.length - 1;
    }
}
