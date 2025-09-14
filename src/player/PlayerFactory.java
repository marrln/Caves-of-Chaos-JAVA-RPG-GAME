package player;

/**
 * Factory class for creating different player types.
 * Uses the Factory pattern to encapsulate player creation logic.
 */
public class PlayerFactory {
    
    // Player class types
    public static final String CLASS_WIZARD = "wizard";
    public static final String CLASS_DUELIST = "duelist";

    public static AbstractPlayer createPlayer(String playerClass, int x, int y) {
        switch (playerClass.toLowerCase()) {
            case CLASS_WIZARD -> {
                return new Wizard(x, y);
            }
            case CLASS_DUELIST -> {
                return new Duelist(x, y);
            }
            default -> throw new IllegalArgumentException("Invalid player class: " + playerClass);
        }
    }
}
