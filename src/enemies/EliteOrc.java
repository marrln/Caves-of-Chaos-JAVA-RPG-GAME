package enemies;

/**
 * Elite Orc enemy - a veteran orc warrior with superior combat skills.
 * High damage output and multiple devastating attack types.
 */
public class EliteOrc extends AbstractEnemy {
    
    /**
     * Creates a new Elite Orc at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public EliteOrc(int x, int y) {
        super(x, y, EnemyType.ELITE_ORC);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
}
