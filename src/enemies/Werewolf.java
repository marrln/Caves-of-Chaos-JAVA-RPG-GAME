package enemies;

public class Werewolf extends AbstractEnemy {
    
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
