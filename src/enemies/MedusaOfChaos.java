package enemies;

/**
 * Medusa of Chaos - The final boss of the caves.
 * This boss maintains its health across level entries/exits.
 */
public class MedusaOfChaos extends AbstractEnemy {
    
    private static int persistentHp = -1; // Shared HP across all instances
    private static boolean hasBeenCreated = false;

    public MedusaOfChaos(int x, int y) {
        super(x, y, EnemyType.MEDUSA_OF_CHAOS);
        
        // Initialize persistent HP on first creation
        if (!hasBeenCreated) {
            persistentHp = this.hp;
            hasBeenCreated = true;
        } else {
            // Restore previous HP state
            if (persistentHp > 0) {
                this.hp = persistentHp;
            }
        }
    }

    @Override
    public boolean takeDamage(int damage) {
        boolean result = super.takeDamage(damage);
        
        // Update persistent HP
        persistentHp = this.hp;
        
        return result;
    }

    public static boolean isBossDefeated() {
        return hasBeenCreated && persistentHp <= 0;
    }
    
    public static int getPersistentHp() {
        return persistentHp;
    }
    
    /**
     * Boss AI turn logic - implements the required Enemy interface method.
     * The Medusa of Chaos uses behavior from AbstractEnemy.
     */
    @Override
    public void update(int playerX, int playerY) {
        // Call the parent implementation from AbstractEnemy
        super.update(playerX, playerY);
    }
    
    @Override
    protected String getNoticeMessage() {
        return "The " + getName() + "'s serpentine hair writhes as her gaze falls upon you! 'Another fool seeks to challenge me!'";
    }
}
