package enemies;

import java.util.Random;

/**
 * Factory class for creating different enemy types.
 */
public class EnemyFactory {
    
    private static final Random random = new Random();
    
    /**
     * Enemy difficulty levels.
     */
    public enum Difficulty {
        EASY, MEDIUM, HARD, BOSS
    }
    
    /**
     * Creates a random enemy at the specified position with the given difficulty.
     * 
     * @param x The x position
     * @param y The y position
     * @param difficulty The difficulty level
     * @return A new enemy instance
     */
    public static Enemy createRandomEnemy(int x, int y, Difficulty difficulty) {
        // Adjust probabilities based on difficulty
        int enemyType;
        
        enemyType = switch (difficulty) {
            case EASY -> random.nextInt(2);
            case MEDIUM -> random.nextInt(3);
            case HARD -> random.nextInt(2) + 2;
            case BOSS -> 4;
            default -> 0;
        }; // 0-1
        // 0-2
        // 2-3
        
        // Create enemy based on type
        return switch (enemyType) {
            case 0 -> new Skeleton(x, y);
            case 1 -> new Orc(x, y);
            case 2 -> new ArmoredSkeleton(x, y);
            case 3 -> new EliteOrc(x, y);
            case 4 -> new SerpentOfChaos(x, y);
            default -> new Skeleton(x, y);
        };
    }
    
    /**
     * Creates an enemy of the specified type at the given position.
     * 
     * @param type The enemy type
     * @param x The x position
     * @param y The y position
     * @return A new enemy instance
     */
    public static Enemy createEnemy(String type, int x, int y) {
        switch (type.toLowerCase()) {
            case "skeleton" -> {
                return new Skeleton(x, y);
            }
            case "orc" -> {
                return new Orc(x, y);
            }
            case "armored_skeleton" -> {
                return new ArmoredSkeleton(x, y);
            }
            case "elite_orc" -> {
                return new EliteOrc(x, y);
            }
            case "serpent_of_chaos" -> {
                return new SerpentOfChaos(x, y);
            }
            default -> throw new IllegalArgumentException("Invalid enemy type: " + type);
        }
    }
    
    /**
     * Creates an armored skeleton enemy at the specified position.
     */
    public static class ArmoredSkeleton extends Skeleton {
        public ArmoredSkeleton(int x, int y) {
            super(x, y);
            this.name = "Armored Skeleton";
            this.maxHp = 40;
            this.hp = maxHp;
            this.attackDamage = 7;
            this.expReward = 20;
        }
        
        @Override
        public boolean takeDamage(int damage) {
            // Armored skeletons have damage reduction
            int reducedDamage = Math.max(1, damage - 2);
            return super.takeDamage(reducedDamage);
        }
    }
    
    /**
     * Creates an elite orc enemy at the specified position.
     */
    public static class EliteOrc extends Orc {
        public EliteOrc(int x, int y) {
            super(x, y);
            this.name = "Elite Orc";
            this.maxHp = 60;
            this.hp = maxHp;
            this.attackDamage = 12;
            this.expReward = 30;
        }
        
        @Override
        public int getAttackDamage() {
            // Elite orcs have an even higher critical hit chance
            if (random.nextInt(4) == 0) { // 25% chance
                return attackDamage * 2; // Critical hit!
            }
            return super.getAttackDamage();
        }
    }
    
    /**
     * Creates a serpent of chaos boss enemy at the specified position.
     */
    public static class SerpentOfChaos extends AbstractEnemy {
        private int specialAttackCooldown = 0;
        
        public SerpentOfChaos(int x, int y) {
            super(x, y, "Serpent of Chaos", 200, 15, 100);
        }
        
        @Override
        public void takeTurn(int playerX, int playerY) {
            // Calculate distance to player
            int dx = playerX - x;
            int dy = playerY - y;
            int distanceSquared = dx * dx + dy * dy;
            
            // Decrement cooldown
            if (specialAttackCooldown > 0) {
                specialAttackCooldown--;
            }
            
            // Boss has special behavior
            if (distanceSquared <= 144) { // 12 tiles range
                // If close enough and cooldown is ready, perform special attack
                if (specialAttackCooldown == 0 && distanceSquared <= 4) {
                    // Special attack logic would go here
                    specialAttackCooldown = 3; // Reset cooldown
                } else {
                    // Move toward player
                    moveToward(playerX, playerY);
                }
            } else {
                // Random movement if player is out of range
                if (random.nextBoolean()) { // 50% chance to move
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
            // Boss has a chance for devastating attacks
            if (random.nextInt(5) == 0) { // 20% chance
                return attackDamage * 3; // Devastating hit!
            }
            return super.getAttackDamage();
        }
        
        @Override
        public boolean takeDamage(int damage) {
            // Boss has damage reduction
            int reducedDamage = Math.max(1, damage - 5);
            return super.takeDamage(reducedDamage);
        }
    }
}
