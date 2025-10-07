package config;

import config.ItemConfig.PotionType;
import config.ItemConfig.TrapType;
import config.ItemConfig.WeaponType;

// Handles item spawn logic, scaling, and type distributions per level
public class ItemSpawnConfig {

    // ===== SPAWN RATES =====
    private static final double CONSUMABLE_CHANCE = 0.60;
    private static final double WEAPON_CHANCE = 0.25;
    // Trap chance = 0.15 (shared equally among all trap types)

    // ===== LEVEL SCALING =====
    private static final int BASE_ITEMS_PER_LEVEL = 4;
    private static final int ITEMS_PER_LEVEL_DIVISOR = 3; // +1 item every 3 levels
    private static final int RANDOM_ITEMS_VARIANCE = 2;   // +0–1 random items

    // ===== WEAPON RULES =====
    private static final int WEAPON_DISABLED_LEVELS = 2; // Weapons start at level 3
    private static final int BASE_WEAPON_DAMAGE = 5;
    private static final int WEAPON_DAMAGE_PER_LEVEL = 3;

    // ===== POTION STATS =====
    private static final int BASE_HP_HEAL = 15;
    private static final int HP_PER_LEVEL = 5;
    private static final int BASE_MP_RESTORE = 15;
    private static final int MP_PER_LEVEL = 5;

    // ===== POTION DISTRIBUTION =====
    public static final PotionType[] MANA_USER_POTIONS = {
        PotionType.LESSER_MANA, PotionType.MANA, PotionType.GREATER_MANA,
        PotionType.ELIXIR, PotionType.ELIXIR,
        PotionType.LESSER_HEAL, PotionType.HEAL, PotionType.GREATER_HEAL
    };

    public static final PotionType[] NON_MANA_USER_POTIONS = {
        PotionType.LESSER_HEAL, PotionType.HEAL, PotionType.GREATER_HEAL, PotionType.SUPREME_HEAL,
        PotionType.LESSER_HEAL, PotionType.HEAL, PotionType.GREATER_HEAL, PotionType.SUPREME_HEAL
    };

    // ===== ITEM COUNT =====
    public static int getItemCountForLevel(int level, double randomSeed) {
        int baseItems = BASE_ITEMS_PER_LEVEL + (level / ITEMS_PER_LEVEL_DIVISOR);
        int variance = (int) (randomSeed * RANDOM_ITEMS_VARIANCE);
        return baseItems + variance;
    }

    // ===== ITEM TYPE =====
    public static String getItemTypeFromRoll(double roll, int level) {
        boolean weaponsEnabled = level > WEAPON_DISABLED_LEVELS;

        double consumableChance = CONSUMABLE_CHANCE;
        double weaponChance = weaponsEnabled ? WEAPON_CHANCE : 0;
        // double trapChance = 1.0 - (consumableChance + weaponChance);

        if (roll < consumableChance) return "consumable";
        if (roll < consumableChance + weaponChance) return "weapon";
        return "trap";
    }

    // ===== POTIONS =====
    public static PotionType getRandomPotionType(boolean playerUsesMana, int roll) {
        PotionType[] pool = playerUsesMana ? MANA_USER_POTIONS : NON_MANA_USER_POTIONS;
        int index = Math.max(0, Math.min(roll, pool.length - 1));
        return pool[index];
    }

    public static int calculatePotionHp(PotionType type, int level) {
        int baseHeal = BASE_HP_HEAL + (level * HP_PER_LEVEL);
        return (int) (baseHeal * type.hpMultiplier);
    }

    public static int calculatePotionMp(PotionType type, int level) {
        int baseMana = BASE_MP_RESTORE + (level * MP_PER_LEVEL);
        return (int) (baseMana * type.mpMultiplier);
    }

    // ===== WEAPONS =====
    public static int calculateWeaponDamage(WeaponType weaponType, int level) {
        int baseDamage = BASE_WEAPON_DAMAGE + (level * WEAPON_DAMAGE_PER_LEVEL);
        return baseDamage + weaponType.bonusDamage;
    }

    // ===== TRAPS =====
    public static TrapType getTrapTypeFromRoll(double roll) {
        // Equal probability among all trap types
        TrapType[] traps = TrapType.values();
        int index = (int) (roll * traps.length);
        index = Math.min(index, traps.length - 1);
        return traps[index];
    }
}
