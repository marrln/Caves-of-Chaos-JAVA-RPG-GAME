package items;

import player.AbstractPlayer;

/**
 * A weapon that can be equipped by a player to increase damage output.
 */
public class Weapon extends Item {
    
    private final int damageBonus;
    private final String playerClass; // "duelist", "wizard", or "any"
    private final String weaponType;  // "sword", "staff", etc.

    public Weapon(String name, String description, int damageBonus, String playerClass, String weaponType) {
        super(name, ItemType.WEAPON, description);
        this.damageBonus = damageBonus;
        this.playerClass = playerClass;
        this.weaponType = weaponType;
    }

    // ====== GETTERS ======
    public int getDamageBonus() { return damageBonus; }
    public String getPlayerClass() { return playerClass; }
    public String getWeaponType() { return weaponType; }

    // ====== USAGE ======
    @Override
    public boolean canUse(AbstractPlayer player) {
        // Check if weapon is for the correct player class
        String playerClassName = player.getClass().getSimpleName().toLowerCase();
        return playerClass.equals("any") || playerClassName.contains(playerClass);
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
        return new Weapon(getName(), getDescription(), damageBonus, playerClass, weaponType);
    }
}
