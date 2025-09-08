package enemies;

/**
 * Base interface for all enemies in the game.
 */
public interface Enemy {
    
    /**
     * Gets the enemy's current x position.
     * 
     * @return The x position
     */
    int getX();
    
    /**
     * Gets the enemy's current y position.
     * 
     * @return The y position
     */
    int getY();
    
    /**
     * Sets the enemy's position.
     * 
     * @param x The new x position
     * @param y The new y position
     */
    void setPosition(int x, int y);
    
    /**
     * Gets the enemy's current health.
     * 
     * @return The current HP
     */
    int getHp();
    
    /**
     * Gets the enemy's maximum health.
     * 
     * @return The max HP
     */
    int getMaxHp();
    
    /**
     * Sets the enemy's health to a new value.
     * 
     * @param hp The new health value
     */
    void setHp(int hp);
    
    /**
     * Gets the name of the enemy.
     * 
     * @return The enemy's name
     */
    String getName();
    
    /**
     * Gets the amount of experience rewarded when this enemy is defeated.
     * 
     * @return The experience reward
     */
    int getExpReward();
    
    /**
     * Checks if the enemy is dead.
     * 
     * @return true if the enemy is dead, false otherwise
     */
    boolean isDead();
    
    /**
     * Makes the enemy take its turn in the game loop.
     * 
     * @param playerX The player's x position
     * @param playerY The player's y position
     */
    void takeTurn(int playerX, int playerY);
    
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
}
