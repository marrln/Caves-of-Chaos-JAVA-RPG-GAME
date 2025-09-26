package items;

/**
 * A moderate poison dart trap that deals substantial damage.
 * Ancient mechanisms that shoot poisoned darts when triggered.
 */
public class PoisonDartTrap extends AbstractTrap {
    
    private static final int MODERATE_DAMAGE = 7;
    private static final String TRAP_NAME = "Poison Dart Trap";
    private static final String DESCRIPTION = "Ancient mechanisms hidden in the walls that shoot poisoned darts when triggered.";
    private static final String TRIGGER_MSG = "A poisoned dart shoots out from the wall and strikes you!";
    private static final String DAMAGE_MSG = "The poison courses through your veins, sapping your strength.";
    
    public PoisonDartTrap() {
        super(TRAP_NAME, DESCRIPTION, MODERATE_DAMAGE, TRIGGER_MSG, DAMAGE_MSG);
    }
    
    @Override
    public Item copy() {
        return new PoisonDartTrap();
    }
}