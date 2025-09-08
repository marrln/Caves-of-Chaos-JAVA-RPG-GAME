package player;

/**
 * Wizard player class. Focuses on magical abilities.
 */
public class Wizard extends AbstractPlayer {
    
    /**
     * Creates a new Wizard at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public Wizard(int x, int y) {
        super(x, y);
        
        // Base stats for Wizard
        this.maxHp = 80;
        this.hp = maxHp;
        this.maxMp = 150;
        this.mp = maxMp;
    }
    
    @Override
    public void attack(int attackType) {
        if (attackType == 1) {
            // Primary attack: Magic Bolt (costs 5 MP)
            if (mp >= 5) {
                mp -= 5;
                // TODO: Implement magic bolt attack logic
            }
        } else if (attackType == 2) {
            // Secondary attack: Fireball (costs 15 MP)
            if (mp >= 15) {
                mp -= 15;
                // TODO: Implement fireball attack logic
            }
        }
    }
    
    @Override
    public void rest() {
        // Restore 5% of max HP and MP
        hp = Math.min(maxHp, hp + (int)(maxHp * 0.05));
        mp = Math.min(maxMp, mp + (int)(maxMp * 0.08));
    }
    
    @Override
    public void useItem(int slot) {
        // TODO: Implement inventory and item usage
    }
}
