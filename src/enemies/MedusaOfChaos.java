package enemies;

/**
 * Medusa of Chaos - The final boss of the caves.
 * Extremely powerful with multiple devastating attack types and high health.
 * This boss maintains its health across level entries/exits.
 */
public class MedusaOfChaos extends AbstractEnemy {
    
    private static int persistentHp = -1; // Shared HP across all instances
    private static boolean hasBeenCreated = false;
    
    /**
     * Creates a new Medusa of Chaos at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
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
    
    /**
     * Overrides takeDamage to maintain persistent HP.
     */
    @Override
    public boolean takeDamage(int damage) {
        boolean result = super.takeDamage(damage);
        
        // Update persistent HP
        persistentHp = this.hp;
        
        return result;
    }
    
    /**
     * Checks if the boss has been defeated permanently.
     * 
     * @return true if the boss is dead
     */
    public static boolean isBossDefeated() {
        return hasBeenCreated && persistentHp <= 0;
    }
    
    /**
     * Resets the boss state (for new game).
     */
    public static void resetBossState() {
        persistentHp = -1;
        hasBeenCreated = false;
    }
    
    /**
     * Gets the current persistent HP of the boss.
     * 
     * @return The boss's current HP, or -1 if not yet created
     */
    public static int getPersistentHp() {
        return persistentHp;
    }
    
    /**
     * Boss AI turn logic - implements the required Enemy interface method.
     * The Medusa of Chaos uses enhanced AI behavior from AbstractEnemy.
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
