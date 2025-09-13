package enemies;

/**
 * Armored Skeleton enemy - an undead warrior with heavy protection.
 * Better armor and more dangerous than basic skeletons.
 */
public class ArmoredSkeleton extends AbstractEnemy {
    
    /**
     * Creates a new Armored Skeleton at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public ArmoredSkeleton(int x, int y) {
        super(x, y, EnemyType.ARMORED_SKELETON);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
    
    @Override
    protected String getNoticeMessage() {
        return "An " + getName() + " has noticed your presence, an ugly noise is emanating as it's approaching!";
    }
}
