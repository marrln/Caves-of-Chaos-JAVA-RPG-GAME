package config;

/**
 * Configuration class for player progression and combat statistics.
 * Centralizes all player-related balance parameters for easy tweaking.
 */
public class PlayerConfig {
    
    /**
     * Configuration data for player stats at each level.
     */
    public static class PlayerLevelStats {
        public final int baseHp;
        public final int baseMp;
        public final int baseDamage;
        public final int criticalChance;      // Percentage chance for critical hits
        public final int criticalMultiplier;  // Damage multiplier for critical hits
        public final int expToNextLevel;      // Experience needed to reach next level
        
        public PlayerLevelStats(int baseHp, int baseMp, int baseDamage, 
                               int criticalChance, int criticalMultiplier, int expToNextLevel) {
            this.baseHp = baseHp;
            this.baseMp = baseMp;
            this.baseDamage = baseDamage;
            this.criticalChance = criticalChance;
            this.criticalMultiplier = criticalMultiplier;
            this.expToNextLevel = expToNextLevel;
        }
    }
    
    /**
     * Configuration for different attack types.
     */
    public static class AttackConfig {
        public final int baseDamage;
        public final int mpCost;              // 0 for physical attacks
        public final int cooldown;            // Milliseconds between uses
        public final int criticalBonus;       // Additional critical chance for this attack
        public final String name;
        
        public AttackConfig(int baseDamage, int mpCost, int cooldown, int criticalBonus, String name) {
            this.baseDamage = baseDamage;
            this.mpCost = mpCost;
            this.cooldown = cooldown;
            this.criticalBonus = criticalBonus;
            this.name = name;
        }
    }
    
    /**
     * Gets the base stats for a Duelist at the specified level.
     * 
     * @param level The character level (1-based)
     * @return The level stats
     */
    public static PlayerLevelStats getDuelistStats(int level) {
        int baseHp = 100 + (level - 1) * 15;        // +15 HP per level
        int baseMp = 0;                             // Duelists don't use mana
        int baseDamage = 8 + (level - 1) * 2;       // +2 damage per level
        int criticalChance = 10 + (level - 1) * 2;  // +2% crit per level, starts at 10%
        int criticalMultiplier = 200;               // 2x damage on crit
        int expToNextLevel = level * 50;            // Increasing exp requirements
        
        return new PlayerLevelStats(baseHp, baseMp, baseDamage, 
                                   criticalChance, criticalMultiplier, expToNextLevel);
    }
    
    /**
     * Gets the base stats for a Wizard at the specified level.
     * 
     * @param level The character level (1-based)
     * @return The level stats
     */
    public static PlayerLevelStats getWizardStats(int level) {
        int baseHp = 80 + (level - 1) * 10;         // +10 HP per level (less than Duelist)
        int baseMp = 150 + (level - 1) * 20;        // +20 MP per level
        int baseDamage = 6 + (level - 1) * 2;       // +2 damage per level (lower base)
        int criticalChance = 8 + (level - 1) * 1;   // +1% crit per level, starts at 8%
        int criticalMultiplier = 180;               // 1.8x damage on crit (lower than Duelist)
        int expToNextLevel = level * 50;            // Same exp curve as Duelist
        
        return new PlayerLevelStats(baseHp, baseMp, baseDamage, 
                                   criticalChance, criticalMultiplier, expToNextLevel);
    }
    
    /**
     * Gets the attack configuration for Duelist attacks.
     * 
     * @param attackType The attack type (1 or 2)
     * @return The attack configuration
     */
    public static AttackConfig getDuelistAttackConfig(int attackType) {
        return switch (attackType) {
            case 1 -> new AttackConfig(
                100,        // baseDamage (100% of player's base damage)
                0,          // mpCost (no mana cost)
                800,        // cooldown (0.8 seconds)
                0,          // criticalBonus (no extra crit chance)
                "Quick Strike"
            );
            case 2 -> new AttackConfig(
                150,        // baseDamage (150% of player's base damage)
                0,          // mpCost
                1200,       // cooldown (1.2 seconds - slower but stronger)
                5,          // criticalBonus (+5% crit chance)
                "Power Attack"
            );
            default -> throw new IllegalArgumentException("Invalid attack type: " + attackType);
        };
    }
    
    /**
     * Gets the attack configuration for Wizard attacks.
     * 
     * @param attackType The attack type (1 or 2)
     * @return The attack configuration
     */
    public static AttackConfig getWizardAttackConfig(int attackType) {
        return switch (attackType) {
            case 1 -> new AttackConfig(
                120,        // baseDamage (120% of base - magic is powerful)
                5,          // mpCost
                1000,       // cooldown (1.0 seconds)
                2,          // criticalBonus (+2% crit chance)
                "Fire Spell"
            );
            case 2 -> new AttackConfig(
                100,        // baseDamage (100% of base but with effects)
                8,          // mpCost
                1400,       // cooldown (1.4 seconds)
                0,          // criticalBonus (no extra crit)
                "Ice Spell"
            );
            default -> throw new IllegalArgumentException("Invalid attack type: " + attackType);
        };
    }
    
    /**
     * Calculates the actual damage for a player attack.
     * 
     * @param playerBaseDamage The player's base damage stat
     * @param attackConfig The attack configuration
     * @param isCritical Whether this is a critical hit
     * @param criticalMultiplier The critical hit multiplier
     * @return The calculated damage
     */
    public static int calculatePlayerDamage(int playerBaseDamage, AttackConfig attackConfig, 
                                           boolean isCritical, int criticalMultiplier) {
        int baseDamage = (playerBaseDamage * attackConfig.baseDamage) / 100;
        
        if (isCritical) {
            return (baseDamage * criticalMultiplier) / 100;
        }
        
        return baseDamage;
    }
    
    /**
     * Gets the rest configuration values.
     * 
     * @return Array containing [hpRestorePercent, mpRestorePercent]
     */
    public static double[] getRestConfiguration() {
        return new double[]{0.05, 0.08}; // 5% HP, 8% MP per rest
    }
}
