package items;

import java.util.ArrayList;
import java.util.List;

public class Inventory {

    private static final int INVENTORY_SIZE = 9;
    private final Item[] slots;
    
    // Result information for weapon upgrades
    private boolean lastAddWasWeaponUpgrade = false;
    private boolean lastAddWasWeaponDowngrade = false;

    public Inventory() { this.slots = new Item[INVENTORY_SIZE]; }

    // Overloaded method for backward compatibility
    public boolean addItem(Item item) {
        return addItem(item, null);
    }

    public boolean addItem(Item item, player.AbstractPlayer player) {
        if (item == null) return false;
        
        // Reset upgrade flags
        lastAddWasWeaponUpgrade = false;
        lastAddWasWeaponDowngrade = false;

        // Auto-stack consumables by name
        if (item instanceof ConsumableItem consumable) {
            for (Item existingItem : slots) {
                if (existingItem instanceof ConsumableItem existing &&
                    existing.getName().equals(consumable.getName()) &&
                    existing.canStack(consumable)) {
                    existing.addQuantity(consumable.getQuantity());
                    return true;
                }
            }
        }

        // Weapon upgrade logic: keep the stronger weapon
        if (item instanceof Weapon newWeapon) {
            return handleWeaponAddition(newWeapon, player);
        }

        // Place in first empty slot
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (slots[i] == null) {
                slots[i] = item;
                return true;
            }
        }

        return false;
    }

    /**
     * Handles adding a weapon with upgrade logic.
     * If a weapon with the same name exists, keeps the one with higher damage.
     * Automatically re-equips if the replaced weapon was equipped.
     */
    private boolean handleWeaponAddition(Weapon newWeapon, player.AbstractPlayer player) {
        // Look for existing weapon with same name
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (slots[i] instanceof Weapon existingWeapon && 
                existingWeapon.getName().equals(newWeapon.getName())) {
                
                boolean wasEquipped = (player != null && player.getEquippedWeapon() == existingWeapon);
                
                // Keep the stronger weapon
                if (newWeapon.getDamageBonus() > existingWeapon.getDamageBonus()) {
                    slots[i] = newWeapon;
                    lastAddWasWeaponUpgrade = true;
                    
                    // Re-equip the stronger weapon if the old one was equipped
                    if (wasEquipped && player != null) {
                        player.equipWeapon(newWeapon);
                    }
                    return true;
                } else {
                    // Keep existing stronger weapon, discard new weaker one
                    lastAddWasWeaponDowngrade = true;
                    return true; // Return true because we "handled" it (by discarding)
                }
            }
        }

        // No existing weapon with same name, add to empty slot
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            if (slots[i] == null) {
                slots[i] = newWeapon;
                return true;
            }
        }

        return false; // Inventory full
    }
    
    // Query methods for weapon upgrade results
    public boolean wasLastAddWeaponUpgrade() { return lastAddWasWeaponUpgrade; }
    public boolean wasLastAddWeaponDowngrade() { return lastAddWasWeaponDowngrade; }

    public Item getItem(int slotNumber) { return isValidSlot(slotNumber) ? slots[slotNumber - 1] : null; }

    public boolean useItem(int slotNumber, player.AbstractPlayer player) {
        Item item = getItem(slotNumber);
        if (item == null || !item.canUse(player)) return false;

        boolean used = item.use(player);

        // Check if the consumable item is now empty (ConsumableItem.use() already calls consume())
        if (used && item instanceof ConsumableItem consumable) {
            if (consumable.getQuantity() <= 0) slots[slotNumber - 1] = null;
        }

        return used;
    }

    public Item removeItem(int slotNumber) { return isValidSlot(slotNumber) ? removeAt(slotNumber - 1) : null; }
    private Item removeAt(int index) { Item removed = slots[index]; slots[index] = null; return removed; }

    public boolean hasEmptySlot() {
        for (Item item : slots) if (item == null) return true;
        return false;
    }

    public int getEmptySlotCount() {
        int count = 0;
        for (Item item : slots) if (item == null) count++;
        return count;
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        for (Item item : slots) if (item != null) items.add(item);
        return items;
    }

    public Item[] getSlots() { return slots.clone(); }
    public boolean isFull() { return !hasEmptySlot(); }
    public void clear() { for (int i = 0; i < INVENTORY_SIZE; i++) slots[i] = null; }
    private boolean isValidSlot(int slotNumber) { return slotNumber >= 1 && slotNumber <= INVENTORY_SIZE; }
}
