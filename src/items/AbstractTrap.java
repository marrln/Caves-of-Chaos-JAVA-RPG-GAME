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
    
    /**
     * Creates a new trap with specified properties.
     * 
     * @param name The display name of the trap
     * @param description The trap's description
     * @param damage The HP damage this trap deals
     * @param triggerMessage Message shown when trap is triggered
     * @param damageMessage Message shown when damage is dealt
     */
    protected AbstractTrap(String name, String description, int damage, 
                          String triggerMessage, String damageMessage) {
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
    public boolean use(AbstractPlayer player) {
        // Apply trap damage (logging handled by GameController)
        player.takeDamage(damage);
        
        // The trap is consumed after being triggered
        return true;
    }
    
    /**
     * Gets the damage this trap deals.
     * 
     * @return The damage amount
     */
    public int getDamage() {
        return damage;
    }
    
    /**
     * Gets the message displayed when the trap is triggered.
     * 
     * @return The trigger message
     */
    public String getTriggerMessage() {
        return triggerMessage;
    }
    
    /**
     * Gets the message displayed when damage is dealt.
     * 
     * @return The damage message
     */
    public String getDamageMessage() {
        return damageMessage;
    }
    
    @Override
    public String getDisplayName() {
        // Traps don't show quantity
        return getName();
    }
}