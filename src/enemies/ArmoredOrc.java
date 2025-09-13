package enemies;

/**
 * Armored Orc enemy - a heavily protected orc warrior.
 * Higher health and defense with three different attack types.
 */
public class ArmoredOrc extends AbstractEnemy {
    
    /**
     * Creates a new Armored Orc at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public ArmoredOrc(int x, int y) {
        super(x, y, EnemyType.ARMORED_ORC);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
    
    @Override
    protected String getNoticeMessage() {
        return "An " + getName() + " has noticed your presence, its armor clanks as it charges!";
    }
}
