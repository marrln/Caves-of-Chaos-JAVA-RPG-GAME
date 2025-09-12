package enemies;

import java.util.Random;

/**
 * Factory class for creating different enemy types.
 * Handles creation based on enemy types and level-appropriate spawning.
 */
public class EnemyFactory {
    
    private static final Random random = new Random();
    
    /**
     * Enemy difficulty tiers for level-based spawning.
     */
    public enum Tier {
        BASIC,     // Levels 1-3: Slime, Orc, Skeleton, Werewolf
        ADVANCED,  // Levels 4-6: Armored variants, Elite Orc, etc.
        ELITE,     // Levels 7-9: Powerful enemies
        BOSS       // Final level: Boss only
    }
    
    /**
     * Creates a random enemy appropriate for the given tier.
     * 
     * @param x The x position
     * @param y The y position
     * @param tier The difficulty tier
     * @return A new enemy instance
     */
    public static Enemy createRandomEnemy(int x, int y, Tier tier) {
        return switch (tier) {
            case BASIC -> createBasicEnemy(x, y);
            case ADVANCED -> createAdvancedEnemy(x, y);
            case ELITE -> createEliteEnemy(x, y);
            case BOSS -> new MedusaOfChaos(x, y);
        };
    }
    
    /**
     * Creates an enemy of the specified type.
     * 
     * @param type The enemy type
     * @param x The x position
     * @param y The y position
     * @return A new enemy instance
     */
    public static Enemy createEnemy(EnemyType type, int x, int y) {
        switch (type) {
            case SLIME:
                return new Slime(x, y);
            case ORC:
                return new Orc(x, y);
            case SKELETON:
                return new Skeleton(x, y);
            case WEREWOLF:
                return new Werewolf(x, y);
            case ARMORED_ORC:
                return new ArmoredOrc(x, y);
            case ARMORED_SKELETON:
                return new ArmoredSkeleton(x, y);
            case ELITE_ORC:
                return new EliteOrc(x, y);
            case GREATSWORD_SKELETON:
                return new GreatswordSkeleton(x, y);
            case ORC_RIDER:
                return new OrcRider(x, y);
            case WEREBEAR:
                return new Werebear(x, y);
            case MEDUSA_OF_CHAOS:
                return new MedusaOfChaos(x, y);
            default:
                throw new IllegalArgumentException("Unknown enemy type: " + type);
        }
    }
    
    /**
     * Creates an enemy from a string identifier (for backwards compatibility).
     * 
     * @param typeName The enemy type name
     * @param x The x position
     * @param y The y position
     * @return A new enemy instance
     */
    public static Enemy createEnemy(String typeName, int x, int y) {
        EnemyType type = parseEnemyType(typeName);
        return createEnemy(type, x, y);
    }
    
    /**
     * Creates a basic tier enemy (levels 1-3).
     */
    private static Enemy createBasicEnemy(int x, int y) {
        EnemyType[] basicTypes = {
            EnemyType.SLIME, EnemyType.ORC, EnemyType.SKELETON, EnemyType.WEREWOLF
        };
        EnemyType type = basicTypes[random.nextInt(basicTypes.length)];
        return createEnemy(type, x, y);
    }
    
    /**
     * Creates an advanced tier enemy (levels 4-6).
     */
    private static Enemy createAdvancedEnemy(int x, int y) {
        EnemyType[] advancedTypes = {
            EnemyType.ARMORED_ORC, EnemyType.ARMORED_SKELETON, 
            EnemyType.ELITE_ORC, EnemyType.GREATSWORD_SKELETON
        };
        EnemyType type = advancedTypes[random.nextInt(advancedTypes.length)];
        return createEnemy(type, x, y);
    }
    
    /**
     * Creates an elite tier enemy (levels 7-9).
     */
    private static Enemy createEliteEnemy(int x, int y) {
        EnemyType[] eliteTypes = {
            EnemyType.ORC_RIDER, EnemyType.WEREBEAR,
            EnemyType.GREATSWORD_SKELETON, EnemyType.ELITE_ORC
        };
        EnemyType type = eliteTypes[random.nextInt(eliteTypes.length)];
        return createEnemy(type, x, y);
    }
    
    /**
     * Parses a string enemy type name to EnemyType enum.
     * 
     * @param typeName The string name
     * @return The corresponding EnemyType
     */
    private static EnemyType parseEnemyType(String typeName) {
        switch (typeName.toLowerCase().replace(" ", "_")) {
            case "slime":
                return EnemyType.SLIME;
            case "orc":
                return EnemyType.ORC;
            case "skeleton":
                return EnemyType.SKELETON;
            case "werewolf":
                return EnemyType.WEREWOLF;
            case "armored_orc":
            case "armored orc":
                return EnemyType.ARMORED_ORC;
            case "armored_skeleton":
            case "armored skeleton":
                return EnemyType.ARMORED_SKELETON;
            case "elite_orc":
            case "elite orc":
                return EnemyType.ELITE_ORC;
            case "greatsword_skeleton":
            case "greatsword skeleton":
                return EnemyType.GREATSWORD_SKELETON;
            case "orc_rider":
            case "orc rider":
                return EnemyType.ORC_RIDER;
            case "werebear":
                return EnemyType.WEREBEAR;
            case "medusa_of_chaos":
            case "medusa of chaos":
                return EnemyType.MEDUSA_OF_CHAOS;
            default:
                throw new IllegalArgumentException("Unknown enemy type: " + typeName);
        }
    }
    
    /**
     * Gets the appropriate tier for a given level.
     * 
     * @param level The current level (1-based)
     * @param maxLevel The maximum level in the game
     * @return The appropriate tier for enemy spawning
     */
    public static Tier getTierForLevel(int level, int maxLevel) {
        if (level >= maxLevel) {
            return Tier.BOSS;
        } else if (level >= maxLevel - 2) { // Last 2-3 levels before boss
            return Tier.ELITE;
        } else if (level >= 4) {
            return Tier.ADVANCED;
        } else {
            return Tier.BASIC;
        }
    }
}
