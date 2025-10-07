package items;

/**
 * A light spike trap that deals minor damage to unwary adventurers.
 * Hidden spikes that emerge from the floor when stepped on.
 */
public class SpikeTrap extends AbstractTrap {
    
    private static final int LIGHT_DAMAGE = 5; // Increased from 3 to 5
    private static final String TRAP_NAME = "Spike Trap";
    private static final String DESCRIPTION = "Hidden spikes in the floor that emerge when stepped on. Causes minor injuries.";
    private static final String TRIGGER_MSG = "Sharp spikes emerge from the floor beneath your feet!";
    private static final String DAMAGE_MSG = "The spikes pierce your boots, causing minor wounds.";
    
    public SpikeTrap() {
        super(TRAP_NAME, DESCRIPTION, LIGHT_DAMAGE, TRIGGER_MSG, DAMAGE_MSG);
    }
    
    @Override
    public Item copy() {
        return new SpikeTrap();
    }
}