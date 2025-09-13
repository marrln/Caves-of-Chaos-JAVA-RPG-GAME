package enemies;

/**
 * Basic Orc enemy - a common hostile creature found in the caves.
 * Uses sword attacks with moderate damage and health.
 */
public class Orc extends AbstractEnemy {
    
    /**
     * Creates a new Orc at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public Orc(int x, int y) {
        super(x, y, EnemyType.ORC);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
    
    @Override
    protected String getNoticeMessage() {
        return "An ugly " + getName() + " snarls and challenges you!";
    }
}
