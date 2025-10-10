package enemies;

public class EliteOrc extends AbstractEnemy {

    public EliteOrc(int x, int y) {
        super(x, y, EnemyType.ELITE_ORC);
    }
    
    @Override
    public void update(int playerX, int playerY) {
        super.update(playerX, playerY);
    }
    
    @Override
    protected String getNoticeMessage() {
        return "An " + getName() + " roars a battle cry, it has noticed you!";
    }
}
