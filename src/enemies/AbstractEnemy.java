package enemies;

import java.util.Random;

/**
 * Abstract base class for all enemies.
 */
public abstract class AbstractEnemy implements Enemy {
    
    protected int x, y;           // Position
    protected int hp, maxHp;      // Health
    protected String name;        // Enemy name
    protected int expReward;      // Experience reward
    protected int attackDamage;   // Base attack damage
    protected Random random;      // For random behavior
    
    /**
     * Creates a new enemy at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     * @param name The enemy's name
     * @param maxHp The maximum health
     * @param attackDamage The attack damage
     * @param expReward The experience reward
     */
    public AbstractEnemy(int x, int y, String name, int maxHp, int attackDamage, int expReward) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.attackDamage = attackDamage;
        this.expReward = expReward;
        this.random = new Random();
    }
    
    @Override
    public int getX() {
        return x;
    }
    
    @Override
    public int getY() {
        return y;
    }
    
    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public int getHp() {
        return hp;
    }
    
    @Override
    public int getMaxHp() {
        return maxHp;
    }
    
    @Override
    public void setHp(int hp) {
        this.hp = Math.max(0, Math.min(hp, maxHp));
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public int getExpReward() {
        return expReward;
    }
    
    @Override
    public boolean isDead() {
        return hp <= 0;
    }
    
    @Override
    public boolean takeDamage(int damage) {
        hp -= damage;
        if (hp < 0) hp = 0;
        return isDead();
    }
    
    @Override
    public int getAttackDamage() {
        // Apply some randomness to damage
        int variation = Math.max(1, attackDamage / 5);
        return attackDamage - variation + random.nextInt(variation * 2 + 1);
    }
    
    @Override
    public void takeTurn(int playerX, int playerY) {
        // Calculate distance to player
        int dx = playerX - x;
        int dy = playerY - y;
        int distanceSquared = dx * dx + dy * dy;
        
        // Default behavior: Move toward player if within range
        if (distanceSquared <= 100) { // 10 tiles range
            moveToward(playerX, playerY);
        } else {
            // Random movement if player is out of range
            int randomDirection = random.nextInt(4);
            switch (randomDirection) {
                case 0 -> setPosition(x, y - 1); // Up
                case 1 -> setPosition(x + 1, y); // Right
                case 2 -> setPosition(x, y + 1); // Down
                case 3 -> setPosition(x - 1, y); // Left
            }
        }
    }
    
    /**
     * Attempts to move the enemy toward the player.
     * 
     * @param playerX The player's x position
     * @param playerY The player's y position
     */
    protected void moveToward(int playerX, int playerY) {
        // Determine which direction to move
        int newX = x;
        int newY = y;
        
        // Move on the axis with the greater distance
        int dx = playerX - x;
        int dy = playerY - y;
        
        if (Math.abs(dx) > Math.abs(dy)) {
            // Move horizontally
            newX += (dx > 0) ? 1 : -1;
        } else {
            // Move vertically
            newY += (dy > 0) ? 1 : -1;
        }
        
        // TODO: Check if the new position is valid (not blocked by wall, etc.)
        // For now, just update the position
        setPosition(newX, newY);
    }
}
