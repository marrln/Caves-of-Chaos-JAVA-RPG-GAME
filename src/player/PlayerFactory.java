package player;

/**
 * Factory class for creating different player types.
 * Uses the Factory pattern to encapsulate player creation logic.
 */
public class PlayerFactory {
    
    // Player class types
    public static final String CLASS_WIZARD = "wizard";
    public static final String CLASS_DUELIST = "duelist";
    
    /**
     * Creates a new player of the specified class at the given position.
     * 
     * @param playerClass The type of player to create
     * @param x The initial x position
     * @param y The initial y position
     * @return A new player instance
     * @throws IllegalArgumentException if the player class is invalid
     */
    public static AbstractPlayer createPlayer(String playerClass, int x, int y) {
        switch (playerClass.toLowerCase()) {
            case CLASS_WIZARD:
                return new Wizard(x, y);
            case CLASS_DUELIST:
                return new Duelist(x, y);
            default:
                throw new IllegalArgumentException("Invalid player class: " + playerClass);
        }
    }
}
