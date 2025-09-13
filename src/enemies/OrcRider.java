package enemies;

/**
 * Orc Rider enemy - an orc mounted on a war beast.
 * High mobility and charging attacks with extended notice range.
 */
public class OrcRider extends AbstractEnemy {
    
    /**
     * Creates a new Orc Rider at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public OrcRider(int x, int y) {
        super(x, y, EnemyType.ORC_RIDER);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
    
    @Override
    protected String getNoticeMessage() {
        return "An " + getName() + " emerges from the darkness, it's coming for you!";
    }
}
