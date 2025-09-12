package enemies;

/**
 * Slime enemy - a weak, acidic creature.
 * Has low health but can deal acid damage.
 */
public class Slime extends AbstractEnemy {
    
    /**
     * Creates a new Slime at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public Slime(int x, int y) {
        super(x, y, EnemyType.SLIME);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
}
