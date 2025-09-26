package items;

/**
 * Base class for weapon items.
 * Weapons provide damage bonuses and have cool names.
 */
public abstract class WeaponItem extends Item {
    
    protected final int damageBonus;
    protected final String playerClass; // "duelist" or "wizard"
    
    public WeaponItem(String name, String description, int damageBonus, String playerClass) {
        super(name, ItemType.WEAPON, description);
        this.damageBonus = damageBonus;
        this.playerClass = playerClass;
    }
    
    // ====== GETTERS ======
    public int getDamageBonus() { return damageBonus; }
    public String getPlayerClass() { return playerClass; }
    
    // ====== USAGE ======
    @Override
    public boolean canUse(player.AbstractPlayer player) {
        // Check if weapon is for the correct player class
        String playerClassName = player.getClass().getSimpleName().toLowerCase();
        return playerClass.equals("any") || playerClassName.contains(playerClass);
    }
    
    @Override
    public boolean use(player.AbstractPlayer player) {
        // Weapons are equipped/unequipped, not consumed
        if (canUse(player)) {
            // If already equipped, unequip it
            if (player.getEquippedWeapon() == this) {
                player.unequipWeapon();
                return true;
            } else {
                // Otherwise, equip this weapon
                return player.equipWeapon(this);
            }
        }
        return false;
    }
    
    // ====== DISPLAY ======
    @Override
    public String getDisplayName() {
        return name + " (+" + damageBonus + ")";
    }
    
    // ====== ABSTRACT METHODS ======
    public abstract String getWeaponType(); // "sword", "staff", etc.
}