package enemies;

/**
 * Basic Skeleton enemy - an undead creature with bone-based attacks.
 * Slightly weaker than Orcs but has good accuracy.
 */
public class Skeleton extends AbstractEnemy {
    
    /**
     * Creates a new Skeleton at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public Skeleton(int x, int y) {
        super(x, y, EnemyType.SKELETON);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
    
    @Override
    protected String getNoticeMessage() {
        return "A " + getName() + "'s bones rattle as it spots you!";
    }
}
