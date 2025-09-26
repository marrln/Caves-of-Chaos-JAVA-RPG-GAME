package items;

/**
 * Wizard staff weapon - provides damage bonus for Wizard class.
 */
public class WizardStaff extends WeaponItem {
    
    public WizardStaff() {
        super("Wizard Staff", "A magical staff that enhances spells", 
              6, "wizard"); // +6 damage, Wizard only
    }
    
    @Override
    public String getWeaponType() {
        return "staff";
    }
    
    @Override
    public Item copy() {
        return new WizardStaff();
    }
}