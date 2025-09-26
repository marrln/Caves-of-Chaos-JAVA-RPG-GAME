package items;

/**
 * Duelist sword weapon - provides damage bonus for Duelist class.
 */
public class DuelistSword extends WeaponItem {
    
    public DuelistSword() {
        super("Duelist Sword", "A swift blade for agile fighters", 
              8, "duelist"); // +8 damage, Duelist only
    }
    
    @Override
    public String getWeaponType() {
        return "sword";
    }
    
    @Override
    public Item copy() {
        return new DuelistSword();
    }
}