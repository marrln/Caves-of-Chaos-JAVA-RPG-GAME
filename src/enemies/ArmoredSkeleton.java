package enemies;

public class ArmoredSkeleton extends AbstractEnemy {

    public ArmoredSkeleton(int x, int y) {
        super(x, y, EnemyType.ARMORED_SKELETON);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
    
    @Override
    protected String getNoticeMessage() {
        return "An " + getName() + " has noticed your presence, an ugly noise is emanating as it's approaching!";
    }
}
