package enemies;

/**
 * An aggressive orc enemy.
 */
public class Orc extends AbstractEnemy {
    
    /**
     * Creates a new orc at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public Orc(int x, int y) {
        super(x, y, "Orc", 35, 8, 15);
    }
    
    @Override
    public void takeTurn(int playerX, int playerY) {
        // Calculate distance to player
        int dx = playerX - x;
        int dy = playerY - y;
        int distanceSquared = dx * dx + dy * dy;
        
        // Orcs are aggressive and will chase from further away
        if (distanceSquared <= 100) { // 10 tiles range
            moveToward(playerX, playerY);
        } else {
            // Orcs tend to move in small groups, so sometimes they stay put
            if (random.nextInt(3) > 0) { // 2/3 chance to move
                int randomDirection = random.nextInt(4);
                switch (randomDirection) {
                    case 0 -> setPosition(x, y - 1); // Up
                    case 1 -> setPosition(x + 1, y); // Right
                    case 2 -> setPosition(x, y + 1); // Down
                    case 3 -> setPosition(x - 1, y); // Left
                }
            }
        }
    }
    
    @Override
    public int getAttackDamage() {
        // Orcs sometimes do critical hits
        if (random.nextInt(5) == 0) { // 20% chance
            return attackDamage * 2; // Critical hit!
        }
        return super.getAttackDamage();
    }
}
