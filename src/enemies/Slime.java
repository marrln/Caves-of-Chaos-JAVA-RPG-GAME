package enemies;

public class Slime extends AbstractEnemy {
    
    public Slime(int x, int y) {
        super(x, y, EnemyType.SLIME);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
    
    @Override
    protected String getNoticeMessage() {
        return "A green " + getName() + " bubbles and oozes toward you!";
    }
}
