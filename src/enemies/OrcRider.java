package enemies;

public class OrcRider extends AbstractEnemy {
    
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
