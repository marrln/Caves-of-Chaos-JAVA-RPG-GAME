package enemies;

public class ArmoredOrc extends AbstractEnemy {

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
