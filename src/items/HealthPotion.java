package items;

/**
 * Health potion that restores player HP.
 */
public class HealthPotion extends ConsumableItem {
    
    private final int healAmount;
    
    public HealthPotion(int quantity) {
        super("Health Potion", "Restores health when consumed", quantity, 8);
        this.healAmount = 25; // Configurable heal amount
    }
    
    @Override
    public boolean canUse(player.AbstractPlayer player) {
        return player.getHp() < player.getMaxHp();
    }
    
    @Override
    protected boolean applyEffect(player.AbstractPlayer player) {
        int currentHp = player.getHp();
        int maxHp = player.getMaxHp();
        
        if (currentHp < maxHp) {
            player.restoreHp(healAmount);
            return true;
        }
        return false;
    }
    
    @Override
    public Item copy() {
        return new HealthPotion(1);
    }
    
    public int getHealAmount() {
        return healAmount;
    }
}