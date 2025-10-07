package config;

import config.ItemConfig.PotionType;
import config.ItemConfig.TrapType;
import config.ItemConfig.WeaponType;

/** Handles item spawn logic, scaling, and distributions per level. */
public class ItemSpawnConfig {

    private static final double CONSUMABLE_CHANCE = 0.60, WEAPON_CHANCE = 0.25; // Trap chance = 0.15
    private static final int BASE_ITEMS_PER_LEVEL = 4, ITEMS_PER_LEVEL_DIVISOR = 3, RANDOM_ITEMS_VARIANCE = 2;
    private static final int WEAPON_DISABLED_LEVELS = 2, BASE_WEAPON_DAMAGE = 5, WEAPON_DAMAGE_PER_LEVEL = 3;
    private static final int BASE_HP_HEAL = 15, HP_PER_LEVEL = 5, BASE_MP_RESTORE = 15, MP_PER_LEVEL = 5;

    public static final PotionType[] MANA_USER_POTIONS = {
        PotionType.LESSER_MANA, PotionType.MANA, PotionType.GREATER_MANA,
        PotionType.ELIXIR, PotionType.ELIXIR,
        PotionType.LESSER_HEAL, PotionType.HEAL, PotionType.GREATER_HEAL
    };

    public static final PotionType[] NON_MANA_USER_POTIONS = {
        PotionType.LESSER_HEAL, PotionType.HEAL, PotionType.GREATER_HEAL, PotionType.SUPREME_HEAL,
        PotionType.LESSER_HEAL, PotionType.HEAL, PotionType.GREATER_HEAL, PotionType.SUPREME_HEAL
    };

    public static int getItemCountForLevel(int level, double randomSeed) {
        return BASE_ITEMS_PER_LEVEL + (level / ITEMS_PER_LEVEL_DIVISOR) + (int) (randomSeed * RANDOM_ITEMS_VARIANCE);
    }

    public static String getItemTypeFromRoll(double roll, int level) {
        double consumableChance = CONSUMABLE_CHANCE;
        double weaponChance = level > WEAPON_DISABLED_LEVELS ? WEAPON_CHANCE : 0;
        return roll < consumableChance ? "consumable"
            : roll < consumableChance + weaponChance ? "weapon"
            : "trap";
    }

    public static PotionType getRandomPotionType(boolean usesMana, int roll) {
        PotionType[] pool = usesMana ? MANA_USER_POTIONS : NON_MANA_USER_POTIONS;
        return pool[Math.max(0, Math.min(roll, pool.length - 1))];
    }

    public static int calculatePotionHp(PotionType type, int level) {
        return (int) ((BASE_HP_HEAL + (level * HP_PER_LEVEL)) * type.hpMultiplier);
    }

    public static int calculatePotionMp(PotionType type, int level) {
        return (int) ((BASE_MP_RESTORE + (level * MP_PER_LEVEL)) * type.mpMultiplier);
    }

    public static int calculateWeaponDamage(WeaponType weapon, int level) {
        return BASE_WEAPON_DAMAGE + (level * WEAPON_DAMAGE_PER_LEVEL) + weapon.bonusDamage;
    }

    public static TrapType getTrapTypeFromRoll(double roll) {
        TrapType[] traps = TrapType.values();
        return traps[Math.min((int) (roll * traps.length), traps.length - 1)];
    }
}
