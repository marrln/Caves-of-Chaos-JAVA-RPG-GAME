package items;

/**
 * Base class for consumable items (potions, traps).
 * Supports stacking up to a maximum quantity.
 */
public abstract class ConsumableItem extends Item {
    
    protected int quantity;
    protected final int maxStack;
    
    public ConsumableItem(String name, String description, int quantity, int maxStack) {
        super(name, ItemType.CONSUMABLE, description);
        this.quantity = Math.max(1, quantity);
        this.maxStack = maxStack;
    }
    
    public int getQuantity() { return quantity; }
    public int getMaxStack() { return maxStack; }
    
    public boolean canAddQuantity(int amount) { return quantity + amount <= maxStack; }
    public void addQuantity(int amount) { quantity = Math.min(maxStack, quantity + amount); }
    
    public boolean consume() {
        if (quantity > 0) {
            quantity--;
            return true;
        }
        return false;
    }
    
    public boolean canStack(ConsumableItem other) { return this.getClass().equals(other.getClass()) && canAddQuantity(other.getQuantity()); }
    public boolean isEmpty() { return quantity <= 0; }
    
    @Override
    public boolean use(player.AbstractPlayer player) {
        if (canUse(player) && consume()) {
            return applyEffect(player);
        }
        return false;
    }

    @Override public String getDisplayName() { return name + " " + quantity + "/" + maxStack; }
    protected abstract boolean applyEffect(player.AbstractPlayer player);
}