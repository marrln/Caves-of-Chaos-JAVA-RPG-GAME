package enemies;

/**
 * Base interface for all enemies in the game.
 */
public interface Enemy {

    int getX();
    int getY();
    void setPosition(int x, int y);
    int getHp();
    int getMaxHp();
    void setHp(int hp);
    String getName();
    int getExpReward();
    boolean isDead();
    
    /**
     * Updates the enemy's AI and behavior in real-time.
     * 
     * @param playerX The player's x position
     * @param playerY The player's y position
     */
    void update(int playerX, int playerY);
    
    /**
     * Makes the enemy take damage.
     * 
     * @param damage The amount of damage to take
     * @return true if the enemy died from this damage, false otherwise
     */
    boolean takeDamage(int damage);
    
    /**
     * Gets the amount of damage this enemy deals with its attacks.
     * 
     * @return The attack damage
     */
    int getAttackDamage();
    
    /**
     * Checks if the enemy has noticed the player.
     * 
     * @return true if the enemy has noticed the player, false otherwise
     */
    boolean hasNoticedPlayer();
}
