package config;

/**
 * Central item spawning and balance configuration.
 * Defines all item types, their stats, spawn rates, and scaling with level.
 */
public class ItemConfig {

    // ===== SPAWN RATES =====
    private static final double CONSUMABLE_CHANCE = 0.60;
    private static final double WEAPON_CHANCE = 0.25;
    // Trap chance is implicit: 1.0 - CONSUMABLE_CHANCE - WEAPON_CHANCE = 0.15
    
    // ===== WEAPON SPAWN RESTRICTIONS =====
    private static final int MAX_LEVEL = Config.getIntSetting("caveLevelNumber"); // Total number of levels in game
    private static final int WEAPON_DISABLED_LEVELS = MAX_LEVEL / 2; 

    // ===== LEVEL SCALING =====
    private static final int BASE_ITEMS_PER_LEVEL = 3;
    private static final int ITEMS_PER_LEVEL_DIVISOR = 2;  // +1 item every 2 levels
    private static final int RANDOM_ITEMS_VARIANCE = 3;    // +0 to 2 random items

    // ===== POTION SCALING =====
    private static final int BASE_HP_HEAL = 15;
    private static final int HP_PER_LEVEL = 5;
    private static final int BASE_MP_RESTORE = 15;
    private static final int MP_PER_LEVEL = 5;

    // ===== WEAPON SCALING =====
    private static final int BASE_WEAPON_DAMAGE = 5;
    private static final int WEAPON_DAMAGE_PER_LEVEL = 3;

    // ===== TRAP SCALING =====
    private static final double BASE_LIGHT_TRAP_CHANCE = 0.7;
    private static final double MIN_LIGHT_TRAP_CHANCE = 0.1;
    private static final double LIGHT_TRAP_DECREASE_PER_LEVEL = 0.05;
    private static final double BASE_MODERATE_TRAP_CHANCE = 0.2;
    private static final double MODERATE_TRAP_INCREASE_PER_LEVEL = 0.03;

    // ===== POTION TYPES =====
    public enum PotionType {
        MINOR_HEALTH("Minor Health Potion", 1.0, 0.0),
        HEALTH("Health Potion", 1.67, 0.0),           // +10 over base
        GREATER_HEALTH("Greater Health Potion", 2.33, 0.0),  // +20 over base
        SUPERIOR_HEALTH("Superior Health Potion", 3.0, 0.0), // +30 over base
        ULTIMATE_HEALTH("Ultimate Health Potion", 3.67, 0.0), // +40 over base
        ELIXIR_VITALITY("Elixir of Vitality", 2.67, 0.0),     // +25 over base
        REGENERATION("Potion of Regeneration", 2.0, 0.0),     // +15 over base
        DIVINE_HEALING("Potion of Divine Healing", 3.33, 0.0), // +35 over base
        
        MINOR_MANA("Minor Mana Potion", 0.0, 1.0),
        MANA("Mana Potion", 0.0, 1.8),                // +8 over base
        GREATER_MANA("Greater Mana Potion", 0.0, 2.5), // +15 over base
        
        MINOR_ELIXIR("Minor Elixir", 0.5, 0.5),
        SUPERIOR_ELIXIR("Superior Elixir", 0.8, 0.8);

        public final String name;
        public final double hpMultiplier;
        public final double mpMultiplier;

        PotionType(String name, double hpMultiplier, double mpMultiplier) {
            this.name = name;
            this.hpMultiplier = hpMultiplier;
            this.mpMultiplier = mpMultiplier;
        }
    }

    // ===== WEAPON TYPES =====
    public enum WeaponType {
        IRON_SWORD("Iron Sword", "A sturdy iron blade", 0, "duelist", "sword"),
        WAR_HAMMER("War Hammer", "A heavy crushing weapon", 2, "duelist", "hammer"),
        ELVEN_BLADE("Elven Blade", "A swift and precise weapon", 1, "any", "blade"),
        MAGIC_STAFF("Magic Staff", "A staff crackling with energy", 1, "wizard", "staff");

        public final String name;
        public final String description;
        public final int bonusDamage;  // Added to base damage
        public final String playerClass;
        public final String weaponType;

        WeaponType(String name, String description, int bonusDamage, String playerClass, String weaponType) {
            this.name = name;
            this.description = description;
            this.bonusDamage = bonusDamage;
            this.playerClass = playerClass;
            this.weaponType = weaponType;
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

    // ===== POTION DISTRIBUTION =====
    // Maps roll (0-7) to potion types for mana users and non-mana users
    // Wizards get 60% mana potions (indices 0-4) and 40% health potions (indices 5-7)
    private static final PotionType[] MANA_USER_POTIONS = {
        PotionType.MINOR_MANA,      // 0
        PotionType.MANA,            // 1
        PotionType.GREATER_MANA,    // 2
        PotionType.MINOR_ELIXIR,    // 3
        PotionType.SUPERIOR_ELIXIR, // 4
        PotionType.MINOR_HEALTH,    // 5
        PotionType.HEALTH,          // 6
        PotionType.GREATER_HEALTH   // 7
    };

    private static final PotionType[] NON_MANA_USER_POTIONS = {
        PotionType.MINOR_HEALTH,
        PotionType.HEALTH,
        PotionType.GREATER_HEALTH,
        PotionType.SUPERIOR_HEALTH,
        PotionType.ULTIMATE_HEALTH,
        PotionType.ELIXIR_VITALITY,
        PotionType.REGENERATION,
        PotionType.DIVINE_HEALING
    };

    // ===== PUBLIC API: SPAWN CONFIGURATION =====

    public static int getItemCountForLevel(int level, double randomSeed) {
        int baseItems = BASE_ITEMS_PER_LEVEL + (level / ITEMS_PER_LEVEL_DIVISOR);
        int variance = (int)(randomSeed * RANDOM_ITEMS_VARIANCE);
        return baseItems + variance;
    }

    // Weapons don't spawn on first half of levels (weapon chance goes to consumables)
    public static String getItemTypeFromRoll(double roll, int level) {
        boolean weaponsEnabled = level > WEAPON_DISABLED_LEVELS;
        
        if (weaponsEnabled) {
            // Normal spawn rates
            if (roll < CONSUMABLE_CHANCE) return "consumable";
            if (roll < CONSUMABLE_CHANCE + WEAPON_CHANCE) return "weapon";
            return "trap";
        } else {
            // Weapon chance added to consumable chance
            double adjustedConsumableChance = CONSUMABLE_CHANCE + WEAPON_CHANCE;
            if (roll < adjustedConsumableChance) return "consumable";
            return "trap";
        }
    }

    // ===== PUBLIC API: POTIONS =====

    public static PotionType getRandomPotionType(boolean playerUsesMana, int roll) {
        PotionType[] pool = playerUsesMana ? MANA_USER_POTIONS : NON_MANA_USER_POTIONS;
        int index = Math.max(0, Math.min(roll, pool.length - 1));
        return pool[index];
    }

    public static int calculatePotionHp(PotionType type, int level) {
        int baseHeal = BASE_HP_HEAL + (level * HP_PER_LEVEL);
        return (int)(baseHeal * type.hpMultiplier);
    }

    public static int calculatePotionMp(PotionType type, int level) {
        int baseMana = BASE_MP_RESTORE + (level * MP_PER_LEVEL);
        return (int)(baseMana * type.mpMultiplier);
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

    // ===== PUBLIC API: WEAPONS =====

    public static WeaponType[] getAvailableWeapons(String playerClass) {
        return switch (playerClass.toLowerCase()) {
            case "duelist" -> new WeaponType[] {
                WeaponType.IRON_SWORD,
                WeaponType.WAR_HAMMER,
                WeaponType.ELVEN_BLADE
            };
            case "wizard" -> new WeaponType[] {
                WeaponType.MAGIC_STAFF,
                WeaponType.ELVEN_BLADE
            };
            default -> WeaponType.values(); // All weapons for unknown classes
        };
    }

    public static int calculateWeaponDamage(WeaponType weaponType, int level) {
        int baseDamage = BASE_WEAPON_DAMAGE + (level * WEAPON_DAMAGE_PER_LEVEL);
        return baseDamage + weaponType.bonusDamage;
    }

    // ===== PUBLIC API: TRAPS =====

    // Higher levels spawn more dangerous traps
    public static TrapType getTrapTypeFromRoll(int level, double roll) {
        double lightChance = Math.max(
            MIN_LIGHT_TRAP_CHANCE, 
            BASE_LIGHT_TRAP_CHANCE - (level * LIGHT_TRAP_DECREASE_PER_LEVEL)
        );
        double moderateChance = BASE_MODERATE_TRAP_CHANCE + (level * MODERATE_TRAP_INCREASE_PER_LEVEL);

        if (roll < lightChance) {
            return TrapType.SPIKE;
        } else if (roll < lightChance + moderateChance) {
            return TrapType.POISON_DART;
        } else {
            return TrapType.EXPLOSIVE_RUNE;
        }
    }

    public static int getPotionTypeCount() {
        return 8;
    }
}
