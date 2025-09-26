package items;

/**
 * Base class for all items in the game.
 * Items can be consumables, weapons, or traps.
 */
public abstract class Item {
    
    protected final String name;
    protected final ItemType type;
    protected final String description;
    
    public enum ItemType {
        CONSUMABLE,
        WEAPON,
        TRAP
    }
    
    public Item(String name, ItemType type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }
    
    // ====== GETTERS ======

    public String getName() { return name; }
    public ItemType getType() { return type; }
    public String getDescription() { return description; }
    
    // ====== ABSTRACT METHODS ======

    public abstract boolean canUse(player.AbstractPlayer player);
    public abstract boolean use(player.AbstractPlayer player);
    public abstract String getDisplayName(); // For UI display (includes quantity for consumables)
    public abstract Item copy();             // For creating new instances
    
    @Override
    public String toString() { return name; }
}