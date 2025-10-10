package enemies;

public class Orc extends AbstractEnemy {
    
    public Orc(int x, int y) {
        super(x, y, EnemyType.ORC);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
    
    @Override
    protected String getNoticeMessage() {
        return "An ugly " + getName() + " snarls and challenges you!";
    }
}
