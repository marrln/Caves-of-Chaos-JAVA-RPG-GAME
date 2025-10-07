package items;

/**
 * A severe explosive trap that deals heavy damage to the unwary.
 * Magical runes that explode with destructive force when disturbed.
 */
public class ExplosiveRuneTrap extends AbstractTrap {
    
    private static final int SEVERE_DAMAGE = 18; // Increased from 12 to 18
    private static final String TRAP_NAME = "Explosive Rune Trap";
    private static final String DESCRIPTION = "Magical runes carved into the floor that explode with destructive force when disturbed.";
    private static final String TRIGGER_MSG = "Ancient runes flare to life beneath your feet and explode!";
    private static final String DAMAGE_MSG = "The magical explosion tears at your flesh and armor!";
    
    public ExplosiveRuneTrap() {
        super(TRAP_NAME, DESCRIPTION, SEVERE_DAMAGE, TRIGGER_MSG, DAMAGE_MSG);
    }
    
    @Override
    public Item copy() {
        return new ExplosiveRuneTrap();
    }
}