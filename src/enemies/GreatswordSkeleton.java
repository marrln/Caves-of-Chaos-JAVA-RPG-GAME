package enemies;

/**
 * Greatsword Skeleton enemy - an undead warrior wielding a massive two-handed sword.
 * Slow but extremely powerful with devastating attacks.
 */
public class GreatswordSkeleton extends AbstractEnemy {
    
    /**
     * Creates a new Greatsword Skeleton at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public GreatswordSkeleton(int x, int y) {
        super(x, y, EnemyType.GREATSWORD_SKELETON);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
}
