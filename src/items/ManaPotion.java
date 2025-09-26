package items;

/**
 * Mana potion that restores player MP.
 */
public class ManaPotion extends ConsumableItem {
    
    private final int manaAmount;
    
    public ManaPotion(int quantity) {
        super("Mana Potion", "Restores mana when consumed", quantity, 8);
        this.manaAmount = 20; // Configurable mana amount
    }
    
    @Override
    public boolean canUse(player.AbstractPlayer player) {
        return player.getMp() < player.getMaxMp();
    }
    
    @Override
    protected boolean applyEffect(player.AbstractPlayer player) {
        int currentMp = player.getMp();
        int maxMp = player.getMaxMp();
        
        if (currentMp < maxMp) {
            player.restoreMp(manaAmount);
            return true;
        }
        return false;
    }
    
    @Override
    public Item copy() { return new ManaPotion(1); }    
    public int getManaAmount() { return manaAmount; }
}