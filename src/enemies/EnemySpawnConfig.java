package enemies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration for enemy spawning across different cave levels.
 * Defines which enemies spawn on which levels and in what quantities.
 */
public class EnemySpawnConfig {
    
    /**
     * Configuration for spawning enemies on a specific level.
     */
    public static class LevelSpawnConfig {
        public final Map<EnemyType, Integer> enemyCounts;
        public final int totalEnemies;
        public final boolean isBossLevel;
        
        public LevelSpawnConfig(Map<EnemyType, Integer> enemyCounts, boolean isBossLevel) {
            this.enemyCounts = new HashMap<>(enemyCounts);
            this.totalEnemies = enemyCounts.values().stream().mapToInt(Integer::intValue).sum();
            this.isBossLevel = isBossLevel;
        }
    }
    
    // Static configuration for all levels
    private static final Map<Integer, LevelSpawnConfig> LEVEL_CONFIGS = new HashMap<>();
    
    static {
        initializeLevelConfigs();
    }
    
    /**
     * Initializes the spawn configurations for all levels.
     */
    private static void initializeLevelConfigs() {
        // Level 1 - Basic enemies
        Map<EnemyType, Integer> level1 = new HashMap<>();
        level1.put(EnemyType.SLIME, 3);
        level1.put(EnemyType.SKELETON, 2);
        LEVEL_CONFIGS.put(1, new LevelSpawnConfig(level1, false));
        
        // Level 2 - More basic enemies
        Map<EnemyType, Integer> level2 = new HashMap<>();
        level2.put(EnemyType.SLIME, 2);
        level2.put(EnemyType.ORC, 3);
        level2.put(EnemyType.SKELETON, 2);
        LEVEL_CONFIGS.put(2, new LevelSpawnConfig(level2, false));
        
        // Level 3 - Basic + Werewolf introduction
        Map<EnemyType, Integer> level3 = new HashMap<>();
        level3.put(EnemyType.ORC, 2);
        level3.put(EnemyType.SKELETON, 2);
        level3.put(EnemyType.WEREWOLF, 1);
        level3.put(EnemyType.SLIME, 2);
        LEVEL_CONFIGS.put(3, new LevelSpawnConfig(level3, false));
        
        // Level 4 - Advanced enemies introduction
        Map<EnemyType, Integer> level4 = new HashMap<>();
        level4.put(EnemyType.ARMORED_ORC, 2);
        level4.put(EnemyType.ARMORED_SKELETON, 2);
        level4.put(EnemyType.WEREWOLF, 1);
        level4.put(EnemyType.ORC, 1);
        LEVEL_CONFIGS.put(4, new LevelSpawnConfig(level4, false));
        
        // Level 5 - Boss level (Final level)
        Map<EnemyType, Integer> level5 = new HashMap<>();
        level5.put(EnemyType.MEDUSA_OF_CHAOS, 1);
        LEVEL_CONFIGS.put(5, new LevelSpawnConfig(level5, true));
    }
    
    /**
     * Gets the spawn configuration for the specified level.
     * 
     * @param level The level number (1-based)
     * @return The spawn configuration, or null if level doesn't exist
     */
    public static LevelSpawnConfig getSpawnConfig(int level) {
        return LEVEL_CONFIGS.get(level);
    }
    
    /**
     * Gets the maximum configured level.
     * 
     * @return The highest level number
     */
    public static int getMaxLevel() {
        return LEVEL_CONFIGS.keySet().stream().mapToInt(Integer::intValue).max().orElse(1);
    }
    
    /**
     * Checks if the specified level is a boss level.
     * 
     * @param level The level number
     * @return true if it's a boss level
     */
    public static boolean isBossLevel(int level) {
        LevelSpawnConfig config = getSpawnConfig(level);
        return config != null && config.isBossLevel;
    }
    
    /**
     * Gets all enemy types that can spawn on the specified level.
     * 
     * @param level The level number
     * @return List of enemy types, or empty list if level doesn't exist
     */
    public static List<EnemyType> getEnemyTypesForLevel(int level) {
        LevelSpawnConfig config = getSpawnConfig(level);
        return config != null ? new ArrayList<>(config.enemyCounts.keySet()) : new ArrayList<>();
    }
    
    /**
     * Gets the total number of enemies that should spawn on the specified level.
     * 
     * @param level The level number
     * @return The total enemy count, or 0 if level doesn't exist
     */
    public static int getTotalEnemiesForLevel(int level) {
        LevelSpawnConfig config = getSpawnConfig(level);
        return config != null ? config.totalEnemies : 0;
    }
    
    /**
     * Adds or updates the spawn configuration for a level.
     * This allows for runtime modification of spawn configs.
     * 
     * @param level The level number
     * @param enemyCounts Map of enemy types to spawn counts
     * @param isBossLevel Whether this is a boss level
     */
    public static void setSpawnConfig(int level, Map<EnemyType, Integer> enemyCounts, boolean isBossLevel) {
        LEVEL_CONFIGS.put(level, new LevelSpawnConfig(enemyCounts, isBossLevel));
    }
}
