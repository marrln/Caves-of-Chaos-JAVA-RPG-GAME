package items;

/**
 * A weapon that can be equipped by a player to increase damage output.
 */
public class Weapon extends WeaponItem {
    
    private final String weaponType;
    
    /**
     * Creates a new weapon.
     * 
     * @param name The name of the weapon
     * @param description The description of the weapon
     * @param damageBonus The bonus damage the weapon provides
     * @param playerClass The player class that can use this weapon ("any", "duelist", or "wizard")
     * @param weaponType The type of weapon for display
     */
    public Weapon(String name, String description, int damageBonus, String playerClass, String weaponType) {
        super(name, description, damageBonus, playerClass);
        this.weaponType = weaponType;
    }
    
    @Override
    public String getWeaponType() {
        return weaponType;
    }
    
    @Override
    public String getDisplayName() {
        return getName() + " (+" + getDamageBonus() + " dmg)";
    }
    
    @Override
    public Item copy() {
        return new Weapon(getName(), getDescription(), getDamageBonus(), getPlayerClass(), weaponType);
    }
}