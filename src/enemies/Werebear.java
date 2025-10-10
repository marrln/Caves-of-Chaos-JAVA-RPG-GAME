package enemies;

public class Werebear extends AbstractEnemy {
    
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
