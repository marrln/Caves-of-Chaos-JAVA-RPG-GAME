package items;

import player.AbstractPlayer;

/**
 * Base class for all trap items in the game.
 * Traps automatically trigger when a player steps on their tile,
 * dealing damage and providing feedback messages.
 */
public abstract class AbstractTrap extends Item {
    
    protected final int damage;
    protected final String triggerMessage;
    protected final String damageMessage;
    
    protected AbstractTrap(String name, String description, int damage, String triggerMessage, String damageMessage) {
        super(name, ItemType.CONSUMABLE, description);
        this.damage = damage;
        this.triggerMessage = triggerMessage;
        this.damageMessage = damageMessage;
    }
    
    @Override
    public boolean canUse(AbstractPlayer player) {
        // Traps can always be triggered (stepped on)
        return true;
    }
    
    @Override
    public boolean use(AbstractPlayer player) { // logging is handled in game loop
        player.takeDamage(damage);
        return true;
    }
    
    public int getDamage() { return damage; }
    public String getTriggerMessage() { return triggerMessage; }
    public String getDamageMessage() { return damageMessage; }
    
    @Override
    public String getDisplayName() {
        // Traps don't show quantity
        return getName();
    }
}