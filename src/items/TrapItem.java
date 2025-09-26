package items;

/**
 * Trap item that damages enemies in adjacent tiles.
 */
public class TrapItem extends ConsumableItem {
    
    private final int damage;
    
    public TrapItem(int quantity) {
        super("Trap", "Places a trap that damages enemies", quantity, 3);
        this.damage = 15; // Configurable trap damage
    }
    
    @Override
    public boolean canUse(player.AbstractPlayer player) {
        // Can always use traps if we have them
        return true;
    }
    
    @Override
    protected boolean applyEffect(player.AbstractPlayer player) {
        // Place trap at player's current position
        // This will need integration with the game map system
        // For now, we'll return true to consume the item
        
        // TODO: Implement trap placement logic
        // - Add trap to game map at player position
        // - Set trap to trigger when enemy steps on it
        // - Handle trap damage and removal
        
        return true;
    }
    
    @Override
    public Item copy() {
        return new TrapItem(1);
    }
    
    @Override
    public String getDisplayName() {
        return String.format("%s (%d dmg)", super.getDisplayName(), damage);
    }
    
    public int getDamage() {
        return damage;
    }
}