package items;

import config.ItemConfig;
import player.AbstractPlayer;

/**
 * A weapon that can be equipped by a player to increase damage output.
 * May have special effects like lifesteal or stat boosts.
 */
public class Weapon extends Item {
    
    private final int damageBonus;
    private final String playerClass; // "duelist" or "wizard" only
    private final ItemConfig.WeaponEffect effect;

    public Weapon(String name, int damageBonus, String playerClass, ItemConfig.WeaponEffect effect) {
        super(name, ItemType.WEAPON, createDescription(damageBonus, effect));
        this.damageBonus = damageBonus;
        this.playerClass = playerClass;
        this.effect = effect;
    }

    // ====== GETTERS ======
    public int getDamageBonus() { return damageBonus; }
    public String getPlayerClass() { return playerClass; }
    public ItemConfig.WeaponEffect getEffect() { return effect; }
    
    /**
     * Creates a description based on weapon stats and effects.
     */
    private static String createDescription(int damageBonus, ItemConfig.WeaponEffect effect) {
        StringBuilder desc = new StringBuilder("Weapon (+" + damageBonus + " dmg)");
        if (effect != ItemConfig.WeaponEffect.NONE) {
            desc.append(" - ");
            desc.append(switch (effect) {
                case LIFESTEAL -> "Restores HP when dealing damage";
                case MAX_HP_BOOST -> "Increases max HP by 5";
                case MAX_MP_BOOST -> "Increases max MP by 5";
                default -> "";
            });
        }
        return desc.toString();
    }

    // ====== USAGE ======
    @Override
    public boolean canUse(AbstractPlayer player) {
        // Check if weapon is for the correct player class
        String playerClassName = player.getClass().getSimpleName().toLowerCase();
        return playerClassName.contains(playerClass);
    }
    
    /**
     * Applies weapon effect when equipped (for stat boosts).
     */
    public void applyEquipEffect(AbstractPlayer player) {
        switch (effect) {
            case MAX_HP_BOOST -> player.addMaxHp(5);
            case MAX_MP_BOOST -> player.addMaxMp(5);
            default -> {} // No effect on equip
        }
    }
    
    /**
     * Removes weapon effect when unequipped (for stat boosts).
     */
    public void removeEquipEffect(AbstractPlayer player) {
        switch (effect) {
            case MAX_HP_BOOST -> player.addMaxHp(-5);
            case MAX_MP_BOOST -> player.addMaxMp(-5);
            default -> {} // No effect to remove
        }
    }
    
    /**
     * Applies weapon effect when dealing damage (for lifesteal).
     * 
     * @param player The player wielding the weapon
     * @param damageDealt The amount of damage dealt to enemy
     */
    public void applyOnHitEffect(AbstractPlayer player, int damageDealt) {
        if (effect == ItemConfig.WeaponEffect.LIFESTEAL) {
            int healAmount = Math.max(1, damageDealt / 10); // 10% lifesteal, minimum 1
            player.restoreHp(healAmount);
            player.triggerHealingEffect();
        }
    }

    @Override
    public boolean use(AbstractPlayer player) {
        // Weapons are equipped/unequipped, not consumed
        if (canUse(player)) {
            if (player.getEquippedWeapon() == this) {
                player.unequipWeapon();
                return true;
            } else {
                return player.equipWeapon(this);
            }
        }
        return false;
    }

    // ====== DISPLAY ======
    @Override
    public String getDisplayName() {
        return getName() + " (+" + damageBonus + " dmg)";
    }

    @Override
    public Item copy() {
        return new Weapon(getName(), damageBonus, playerClass, effect);
    }
}
