package enemies;

public class Skeleton extends AbstractEnemy {

    public Skeleton(int x, int y) {
        super(x, y, EnemyType.SKELETON);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
    
    @Override
    protected String getNoticeMessage() {
        return "A " + getName() + "'s bones rattle as it spots you!";
    }
}
