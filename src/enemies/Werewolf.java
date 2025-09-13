package enemies;

/**
 * Werewolf enemy - a fast, aggressive lycanthrope.
 * High speed and dual claw/bite attacks.
 */
public class Werewolf extends AbstractEnemy {
    
    /**
     * Creates a new Werewolf at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public Werewolf(int x, int y) {
        super(x, y, EnemyType.WEREWOLF);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
    
    @Override
    protected String getNoticeMessage() {
        return "A " + getName() + " howls and bares its fangs at you!";
    }
}
