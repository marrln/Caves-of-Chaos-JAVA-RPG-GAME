package player;

/**
 * Duelist player class. Specializes in physical attacks and agility.
 */
public class Duelist extends AbstractPlayer {

    public Duelist(int x, int y) {
        super(x, y);
        
        // Base stats for Duelist
        this.maxHp = 100;
        this.hp = maxHp;

        // Duelists do not use mana
        this.maxMp = 0;
        this.mp = maxMp;

        this.name = "Duelist";
    }
    
    @Override
    public void attack(int attackType) {
        if (attackType == 1) {
            // TODO: Implement primary attack logic
        } else if (attackType == 2) {
            // TODO: Implement secondary attack logic
        }
    }

    @Override
    public void useItem(int slot) {
        // TODO: Implement inventory and item usage
    }
}