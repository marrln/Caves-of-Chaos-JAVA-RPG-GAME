package player;

/**
 * Abstract base class for all player characters.
 */
public abstract class AbstractPlayer {
    protected int x, y;       // Position in the map
    protected int hp, maxHp;  // Health
    protected int mp, maxMp;  // Mana (0 for non-magical classes)
    protected String name;    // Character name
    protected int level;      // Character level
    
    /**
     * Creates a new player with the given position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public AbstractPlayer(int x, int y) {
        this.x = x;
        this.y = y;
        this.level = 1;
    }
    
    /**
     * Gets the player's current x position.
     * 
     * @return The x position
     */
    public int getX() {
        return x;
    }
    
    /**
     * Gets the player's current y position.
     * 
     * @return The y position
     */
    public int getY() {
        return y;
    }
    
    /**
     * Sets the player's position.
     * 
     * @param x The new x position
     * @param y The new y position
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Gets the player's current health.
     * 
     * @return The current HP
     */
    public int getHp() {
        return hp;
    }
    
    /**
     * Gets the player's maximum health.
     * 
     * @return The max HP
     */
    public int getMaxHp() {
        return maxHp;
    }
    
    /**
     * Gets the player's current mana.
     * 
     * @return The current MP
     */
    public int getMp() {
        return mp;
    }
    
    /**
     * Gets the player's maximum mana.
     * 
     * @return The max MP
     */
    public int getMaxMp() {
        return maxMp;
    }
    
    /**
     * Gets the player's name.
     * 
     * @return The player's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the player's name.
     * 
     * @param name The new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the player's level.
     * 
     * @return The player's level
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Performs an attack.
     * 
     * @param attackType The type of attack (1=primary, 2=secondary)
     */
    public abstract void attack(int attackType);
    
    /**
     * Rests to recover health and/or mana.
     */
    public abstract void rest();
    
    /**
     * Uses an item from inventory.
     * 
     * @param slot The inventory slot (1-9)
     */
    public abstract void useItem(int slot);
}
