package enemies;

/**
 * A basic skeleton enemy.
 */
public class Skeleton extends AbstractEnemy {
    
    /**
     * Creates a new skeleton at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public Skeleton(int x, int y) {
        super(x, y, "Skeleton", 25, 5, 10);
    }
    
    @Override
    public void takeTurn(int playerX, int playerY) {
        // Calculate distance to player
        int dx = playerX - x;
        int dy = playerY - y;
        int distanceSquared = dx * dx + dy * dy;
        
        // Skeletons are a bit slower, so they only move every other turn
        if (random.nextBoolean()) {
            // Move toward player if within range
            if (distanceSquared <= 64) { // 8 tiles range
                moveToward(playerX, playerY);
            } else {
                // Skeletons patrol randomly when not chasing
                int randomDirection = random.nextInt(4);
                switch (randomDirection) {
                    case 0:
                        setPosition(x, y - 1); // Up
                        break;
                    case 1:
                        setPosition(x + 1, y); // Right
                        break;
                    case 2:
                        setPosition(x, y + 1); // Down
                        break;
                    case 3:
                        setPosition(x - 1, y); // Left
                        break;
                }
            }
        }
    }
}
