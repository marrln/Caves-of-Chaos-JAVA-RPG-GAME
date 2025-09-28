package items;

import player.AbstractPlayer;

/**
 * A general potion that restores HP and/or MP when consumed.
 */
public class Potion extends ConsumableItem {
    
    private final int hpRestore;
    private final int mpRestore;
    
    public Potion(String name, String description, int hpRestore, int mpRestore) {
        super(name, description, 1, 5); // Potions stack up to 5
        this.hpRestore = hpRestore;
        this.mpRestore = mpRestore;
    }
    
    @Override
    public boolean canUse(AbstractPlayer player) {
        return true; // Potions can always be used
    }
    
    @Override
    protected boolean applyEffect(AbstractPlayer player) {
        boolean used = false;
        
        if (hpRestore > 0) {
            int actualHpRestored = player.restoreHp(hpRestore);
            used = actualHpRestored > 0;
            player.triggerHealingEffect();
        }
        
        if (mpRestore > 0) {
            int actualMpRestored = player.restoreMp(mpRestore);
            used = actualMpRestored > 0 || used;
            player.triggerManaEffect();
        }
        
        return used;
    }
    
    @Override
    public String getDisplayName() {
        if (quantity > 1) {
            return getName() + " (" + quantity + ")";
        }
        return getName();
    }
    
    @Override
    public Item copy() {
        Potion copy = new Potion(getName(), getDescription(), hpRestore, mpRestore);
        copy.quantity = this.quantity;
        return copy;
    }
}