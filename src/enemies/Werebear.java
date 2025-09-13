package enemies;

/**
 * Werebear enemy - a massive, powerful lycanthrope.
 * Extremely high health and devastating attacks with area intimidation.
 */
public class Werebear extends AbstractEnemy {
    
    /**
     * Creates a new Werebear at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public Werebear(int x, int y) {
        super(x, y, EnemyType.WEREBEAR);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
    
    @Override
    protected String getNoticeMessage() {
        return "A " + getName() + " has noticed you! A thunderous roar echoes through the Caves of Chaos!";
    }
}
