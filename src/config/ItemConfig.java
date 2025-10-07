package config;

/** Defines item types, stats, effects, and class restrictions. */
public class ItemConfig {

    // ===== POTIONS =====
    public enum PotionType {
        LESSER_HEAL("Draught of Red Moss", 1.0, 0.0),
        HEAL("Phial of Restored Flesh", 2.0, 0.0),
        GREATER_HEAL("Tonic of the Deep Heart", 3.0, 0.0),
        SUPREME_HEAL("Elixir of Primordial Blood", 4.0, 0.0),
        LESSER_MANA("Philter of Echoing Thought", 0.0, 1.0),
        MANA("Essence of Luminous Fungi", 0.0, 2.0),
        GREATER_MANA("Draught of Abyssal Insight", 0.0, 3.0),
        ELIXIR("Elixir of the Chaos Vein", 0.6, 0.6);

        public final String name;
        public final double hpMultiplier, mpMultiplier;

        PotionType(String name, double hpMultiplier, double mpMultiplier) {
            this.name = name;
            this.hpMultiplier = hpMultiplier;
            this.mpMultiplier = mpMultiplier;
        }
    }

    // ===== WEAPON EFFECTS =====
    public enum WeaponEffect { NONE, LIFESTEAL, MAX_HP_BOOST, MAX_MP_BOOST }

    // ===== WEAPONS =====
    public enum WeaponType {
        // Duelist
        IRON_SWORD("Oathkeeper Blade", 0, "duelist", WeaponEffect.NONE),
        WAR_HAMMER("Skullcrusher", 2, "duelist", WeaponEffect.NONE),
        BLOODFANG("Bloodfang", 1, "duelist", WeaponEffect.LIFESTEAL),
        TITANS_GRIP("Titan's Grip", 1, "duelist", WeaponEffect.MAX_HP_BOOST),

        // Wizard
        ELVEN_BLADE("Moonlight Edge", 1, "wizard", WeaponEffect.NONE),
        MAGIC_STAFF("Stormcaller's Staff", 1, "wizard", WeaponEffect.NONE),
        SOULBOUND_ORB("Soulbound Orb", 0, "wizard", WeaponEffect.MAX_MP_BOOST),
        ARCHMAGE_STAFF("Archmage's Staff", 2, "wizard", WeaponEffect.MAX_MP_BOOST);

        public final String name, playerClass;
        public final int bonusDamage;
        public final WeaponEffect effect;

        WeaponType(String name, int bonusDamage, String playerClass, WeaponEffect effect) {
            this.name = name;
            this.bonusDamage = bonusDamage;
            this.playerClass = playerClass;
            this.effect = effect;
        }
    }

    // ===== TRAPS =====
    public enum TrapType {
        SPIKE("Spike Trap"),
        POISON_DART("Poison Dart Trap"),
        EXPLOSIVE_RUNE("Explosive Rune Trap");

        public final String name;
        TrapType(String name) { this.name = name; }
    }

    // ===== UTILITIES =====
    public static WeaponType[] getAvailableWeapons(String playerClass) {
        return switch (playerClass.toLowerCase()) {
            case "duelist" -> new WeaponType[] {
                WeaponType.IRON_SWORD, WeaponType.WAR_HAMMER,
                WeaponType.BLOODFANG, WeaponType.TITANS_GRIP
            };
            case "wizard" -> new WeaponType[] {
                WeaponType.ELVEN_BLADE, WeaponType.MAGIC_STAFF,
                WeaponType.SOULBOUND_ORB, WeaponType.ARCHMAGE_STAFF
            };
            default -> new WeaponType[0];
        };
    }

    public static String createPotionDescription(int hp, int mp) {
        return hp > 0 && mp > 0 ? "Restores " + hp + " HP and " + mp + " MP"
             : hp > 0 ? "Restores " + hp + " HP"
             : "Restores " + mp + " MP";
    }

    public static int getPotionTypeCount() {
        return PotionType.values().length;
    }
}
