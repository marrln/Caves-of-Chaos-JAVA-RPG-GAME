package config;

import config.EnemySpawnConfig.LevelSpawnConfig;
import enemies.EnemyType;
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
        // Level 1
        Map<EnemyType, Integer> level1 = new HashMap<>();
        level1.put(EnemyType.SLIME, 3);
        level1.put(EnemyType.SKELETON, 2);
        LEVEL_CONFIGS.put(1, new LevelSpawnConfig(level1, false));

        // Level 2
        Map<EnemyType, Integer> level2 = new HashMap<>();
        level2.put(EnemyType.SLIME, 2);
        level2.put(EnemyType.ORC, 3);
        level2.put(EnemyType.SKELETON, 2);
        LEVEL_CONFIGS.put(2, new LevelSpawnConfig(level2, false));

        // Level 3
        Map<EnemyType, Integer> level3 = new HashMap<>();
        level3.put(EnemyType.SLIME, 2);
        level3.put(EnemyType.ORC, 2);
        level3.put(EnemyType.SKELETON, 2);
        level3.put(EnemyType.WEREWOLF, 1);
        LEVEL_CONFIGS.put(3, new LevelSpawnConfig(level3, false));

        // Level 4
        Map<EnemyType, Integer> level4 = new HashMap<>();
        level4.put(EnemyType.ARMORED_ORC, 2);
        level4.put(EnemyType.ARMORED_SKELETON, 2);
        level4.put(EnemyType.WEREWOLF, 1);
        level4.put(EnemyType.ORC, 1);
        LEVEL_CONFIGS.put(4, new LevelSpawnConfig(level4, false));

        // Level 5
        Map<EnemyType, Integer> level5 = new HashMap<>();
        level5.put(EnemyType.ARMORED_ORC, 2);
        level5.put(EnemyType.GREATSWORD_SKELETON, 2);
        level5.put(EnemyType.WEREWOLF, 1);
        LEVEL_CONFIGS.put(5, new LevelSpawnConfig(level5, false));

        // Level 6
        Map<EnemyType, Integer> level6 = new HashMap<>();
        level6.put(EnemyType.ELITE_ORC, 1);
        level6.put(EnemyType.ARMORED_ORC, 2);
        level6.put(EnemyType.WEREBEAR, 1);
        LEVEL_CONFIGS.put(6, new LevelSpawnConfig(level6, false));

        // Level 7
        Map<EnemyType, Integer> level7 = new HashMap<>();
        level7.put(EnemyType.ORC_RIDER, 1);
        level7.put(EnemyType.ELITE_ORC, 1);
        level7.put(EnemyType.ARMORED_SKELETON, 2);
        level7.put(EnemyType.WEREBEAR, 1);
        LEVEL_CONFIGS.put(7, new LevelSpawnConfig(level7, false));

        // Level 8
        Map<EnemyType, Integer> level8 = new HashMap<>();
        level8.put(EnemyType.ARMORED_ORC, 2);
        level8.put(EnemyType.GREATSWORD_SKELETON, 2);
        level8.put(EnemyType.WEREBEAR, 1);
        LEVEL_CONFIGS.put(8, new LevelSpawnConfig(level8, false));

        // Level 9
        Map<EnemyType, Integer> level9 = new HashMap<>();
        level9.put(EnemyType.ELITE_ORC, 2);
        level9.put(EnemyType.ORC_RIDER, 1);
        level9.put(EnemyType.WEREBEAR, 1);
        LEVEL_CONFIGS.put(9, new LevelSpawnConfig(level9, false));

        // Level 10 - Boss
        Map<EnemyType, Integer> level10 = new HashMap<>();
        level10.put(EnemyType.MEDUSA_OF_CHAOS, 1);
        LEVEL_CONFIGS.put(10, new LevelSpawnConfig(level10, true));
    }


    public static LevelSpawnConfig getSpawnConfig(int level) {
        return LEVEL_CONFIGS.get(level);
    }

    public static int getMaxLevel() {
        return LEVEL_CONFIGS.keySet().stream().mapToInt(Integer::intValue).max().orElse(1);
    }
    
    public static boolean isBossLevel(int level) {
        LevelSpawnConfig config = getSpawnConfig(level);
        return config != null && config.isBossLevel;
    }
    
    public static List<EnemyType> getEnemyTypesForLevel(int level) {
        LevelSpawnConfig config = getSpawnConfig(level);
        return config != null ? new ArrayList<>(config.enemyCounts.keySet()) : new ArrayList<>();
    }
    
    public static int getTotalEnemiesForLevel(int level) {
        LevelSpawnConfig config = getSpawnConfig(level);
        return config != null ? config.totalEnemies : 0;
    }
    
    public static void setSpawnConfig(int level, Map<EnemyType, Integer> enemyCounts, boolean isBossLevel) {
        LEVEL_CONFIGS.put(level, new LevelSpawnConfig(enemyCounts, isBossLevel));
    }
}
