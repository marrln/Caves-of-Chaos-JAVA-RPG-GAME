package player;

/**
 * Duelist player class. Specializes in physical attacks and agility.
 */
public class Duelist extends AbstractPlayer {
    
    private int stamina;
    private int maxStamina;
    
    /**
     * Creates a new Duelist at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public Duelist(int x, int y) {
        super(x, y);
        
        // Base stats for Duelist
        this.maxHp = 100;
        this.hp = maxHp;
        this.maxMp = 40;
        this.mp = maxMp;
        this.maxStamina = 120;
        this.stamina = maxStamina;
        this.name = "Duelist";
    }
    
    @Override
    public void attack(int attackType) {
        if (attackType == 1) {
            // Primary attack: Rapier Thrust (costs 10 stamina)
            if (stamina >= 10) {
                stamina -= 10;
                // TODO: Implement rapier thrust attack logic
            }
        } else if (attackType == 2) {
            // Secondary attack: Whirlwind (costs 25 stamina)
            if (stamina >= 25) {
                stamina -= 25;
                // TODO: Implement whirlwind attack logic
            }
        }
    }
    
    @Override
    public void rest() {
        // Restore 8% of max HP and 10% of stamina
        hp = Math.min(maxHp, hp + (int)(maxHp * 0.08));
        stamina = Math.min(maxStamina, stamina + (int)(maxStamina * 0.1));
        mp = Math.min(maxMp, mp + (int)(maxMp * 0.03)); // Small MP recovery
    }
    
    @Override
    public void useItem(int slot) {
        // TODO: Implement inventory and item usage
    }
    
    /**
     * Gets the duelist's current stamina.
     * 
     * @return The current stamina
     */
    public int getStamina() {
        return stamina;
    }
    
    /**
     * Gets the duelist's maximum stamina.
     * 
     * @return The max stamina
     */
    public int getMaxStamina() {
        return maxStamina;
    }
    
    /**
     * Sets the duelist's stamina to a new value, clamped to valid range.
     * 
     * @param stamina The new stamina value
     */
    public void setStamina(int stamina) {
        this.stamina = Math.max(0, Math.min(stamina, maxStamina));
    }
}
