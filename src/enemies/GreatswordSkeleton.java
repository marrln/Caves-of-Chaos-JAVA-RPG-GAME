package enemies;
public class GreatswordSkeleton extends AbstractEnemy {
    
    public GreatswordSkeleton(int x, int y) {
        super(x, y, EnemyType.GREATSWORD_SKELETON);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
    
    @Override
    protected String getNoticeMessage() {
        return "A " + getName() + "'s massive sword trails along the ground as it's approaching you!";
    }
}
