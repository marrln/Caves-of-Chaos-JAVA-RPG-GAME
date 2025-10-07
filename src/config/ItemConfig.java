package config;

/**
 * Central item definitions and balance configuration.
 * Defines all item types, their stats, effects, and class restrictions.
 * 
 * Separated from ItemSpawnConfig to follow Single Responsibility Principle:
 * - ItemConfig: Pure item definitions (what items exist, their properties)
 * - ItemSpawnConfig: Spawn mechanics (when/where items appear, scaling formulas)
 */
public class ItemConfig {

    // ===== POTION TYPES =====
    public enum PotionType {

        // Healing Potions
        LESSER_HEAL("Draught of Red Moss", 1.0, 0.0),          
        HEAL("Phial of Restored Flesh", 2.0, 0.0),              
        GREATER_HEAL("Tonic of the Deep Heart", 3.0, 0.0),      
        SUPREME_HEAL("Elixir of Primordial Blood", 4.0, 0.0), 
    
        // Mana Potions
        LESSER_MANA("Philter of Echoing Thought", 0.0, 1.0),    
        MANA("Essence of Luminous Fungi", 0.0, 2.0),           
        GREATER_MANA("Draught of Abyssal Insight", 0.0, 3.0),   

        // Hybrid Elixirs (restore both HP and MP)
        ELIXIR("Elixir of the Chaos Vein", 0.6, 0.6);           

        public final String name;
        public final double hpMultiplier;
        public final double mpMultiplier;

        PotionType(String name, double hpMultiplier, double mpMultiplier) {
            this.name = name;
            this.hpMultiplier = hpMultiplier;
            this.mpMultiplier = mpMultiplier;
        }
    }


    // ===== WEAPON EFFECTS =====
    public enum WeaponEffect {
        NONE,           // No special effect
        LIFESTEAL,      // Heals player when dealing damage (10% of damage dealt)
        MAX_HP_BOOST,   // Increases max HP by 5 when equipped
        MAX_MP_BOOST    // Increases max MP by 5 when equipped
    }

    // ===== WEAPON TYPES =====
    public enum WeaponType {
        // Duelist weapons
        IRON_SWORD("Oathkeeper Blade", 0, "duelist", WeaponEffect.NONE),
        WAR_HAMMER("Skullcrusher", 2, "duelist", WeaponEffect.NONE),
        BLOODFANG("Bloodfang", 1, "duelist", WeaponEffect.LIFESTEAL),
        
        // Wizard weapons
        ELVEN_BLADE("Moonlight Edge", 1, "wizard", WeaponEffect.NONE),
        MAGIC_STAFF("Stormcaller's Staff", 1, "wizard", WeaponEffect.NONE),
        SOULBOUND_ORB("Soulbound Orb", 0, "wizard", WeaponEffect.MAX_MP_BOOST),
        
        // Special weapons (rare)
        TITANS_GRIP("Titan's Grip", 1, "duelist", WeaponEffect.MAX_HP_BOOST),
        ARCHMAGE_STAFF("Archmage's Staff", 2, "wizard", WeaponEffect.MAX_MP_BOOST);

        public final String name;
        public final int bonusDamage;  // Added to base damage
        public final String playerClass;  // "duelist" or "wizard" only
        public final WeaponEffect effect;

        WeaponType(String name, int bonusDamage, String playerClass, WeaponEffect effect) {
            this.name = name;
            this.bonusDamage = bonusDamage;
            this.playerClass = playerClass;
            this.effect = effect;
        }
    }

    // ===== TRAP TYPES =====
    public enum TrapType {
        SPIKE("Spike Trap"),
        POISON_DART("Poison Dart Trap"),
        EXPLOSIVE_RUNE("Explosive Rune Trap");

        public final String name;

        TrapType(String name) {
            this.name = name;
        }
    }

    // ===== PUBLIC API: ITEM UTILITIES =====

    public static WeaponType[] getAvailableWeapons(String playerClass) {
        return switch (playerClass.toLowerCase()) {
            case "duelist" -> new WeaponType[] {
                WeaponType.IRON_SWORD,
                WeaponType.WAR_HAMMER,
                WeaponType.BLOODFANG,
                WeaponType.TITANS_GRIP
            };
            case "wizard" -> new WeaponType[] {
                WeaponType.ELVEN_BLADE,
                WeaponType.MAGIC_STAFF,
                WeaponType.SOULBOUND_ORB,
                WeaponType.ARCHMAGE_STAFF
            };
            default -> new WeaponType[0]; // No weapons for unknown classes
        };
    }

    public static String createPotionDescription(int hpAmount, int mpAmount) {
        if (hpAmount > 0 && mpAmount > 0) {
            return "Restores " + hpAmount + " HP and " + mpAmount + " MP";
        } else if (hpAmount > 0) {
            return "Restores " + hpAmount + " HP";
        } else {
            return "Restores " + mpAmount + " MP";
        }
    }

    public static int getPotionTypeCount() {
        return PotionType.values().length;
    }
}
