package items;

import config.ItemConfig;
import player.AbstractPlayer;

/**
 * A weapon that can be equipped by a player to increase damage output.
 * May have special effects like lifesteal or stat boosts.
 */
public class Weapon extends Item {
    
    // ====== CONSTANTS ======
    private static final int MAX_STAT_BOOST = 10;  // HP/MP boost amount
    private static final int LIFESTEAL_PERCENT = 10;  // Percentage of damage returned as HP
    
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
    public String getEffectDescription() { return getEffectDescriptionText(effect); }
    

    private static String createDescription(int damageBonus, ItemConfig.WeaponEffect effect) {
        StringBuilder desc = new StringBuilder("Weapon (+" + damageBonus + " dmg)");
        String effectText = getEffectDescriptionText(effect);
        if (!effectText.isEmpty()) {
            desc.append(" - ").append(effectText);
        }
        return desc.toString();
    }

    private static String getEffectDescriptionText(ItemConfig.WeaponEffect effect) {
        return switch (effect) {
            case LIFESTEAL -> LIFESTEAL_PERCENT + "% of damage dealt is offered to you as HP";
            case MAX_HP_BOOST -> "Increases max HP by " + MAX_STAT_BOOST;
            case MAX_MP_BOOST -> "Increases max MP by " + MAX_STAT_BOOST;
            default -> "";
        };
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
            case MAX_HP_BOOST -> player.addMaxHp(MAX_STAT_BOOST);
            case MAX_MP_BOOST -> player.addMaxMp(MAX_STAT_BOOST);
            default -> {} // No effect on equip
        }
    }
    
    /**
     * Removes weapon effect when unequipped (for stat boosts).
     */
    public void removeEquipEffect(AbstractPlayer player) {
        switch (effect) {
            case MAX_HP_BOOST -> player.addMaxHp(-MAX_STAT_BOOST);
            case MAX_MP_BOOST -> player.addMaxMp(-MAX_STAT_BOOST);
            default -> {} // No effect to remove
        }
    }
    
    public void applyOnHitEffect(AbstractPlayer player, int damageDealt) {
        if (effect == ItemConfig.WeaponEffect.LIFESTEAL) {
            int healAmount = Math.max(1, damageDealt * LIFESTEAL_PERCENT / 100);
            player.restoreHp(healAmount);
            player.triggerHealingEffect();
        }
    }

    @Override
    public boolean use(AbstractPlayer player) {
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
