package config;

import enemies.EnemyType;
import java.util.*;

public class EnemySpawnConfig {

    public static class LevelSpawnConfig {
        public final Map<EnemyType, Integer> enemyCounts;
        public final int totalEnemies;
        public final boolean isBossLevel;

        public LevelSpawnConfig(Map<EnemyType, Integer> enemyCounts, boolean isBossLevel) {
            this.enemyCounts = new HashMap<>(enemyCounts);
            this.totalEnemies = enemyCounts.values().stream().mapToInt(Integer::intValue).sum();
            this.isBossLevel = isBossLevel;
        }

        public LevelSpawnConfig(Map<EnemyType, Integer> enemyCounts) { this(enemyCounts, false); }
    }

    private static final Map<Integer, LevelSpawnConfig> LEVEL_CONFIGS = new HashMap<>();

    static { initializeLevelConfigs(); }

    private static void initializeLevelConfigs() {
        LEVEL_CONFIGS.put(1, new LevelSpawnConfig(Map.of(
                EnemyType.SLIME, 3, EnemyType.SKELETON, 2)));

        LEVEL_CONFIGS.put(2, new LevelSpawnConfig(Map.of(
                EnemyType.SLIME, 2, EnemyType.ORC, 3, EnemyType.SKELETON, 2)));

        LEVEL_CONFIGS.put(3, new LevelSpawnConfig(Map.of(
                EnemyType.SLIME, 2, EnemyType.ORC, 2, EnemyType.SKELETON, 2, EnemyType.WEREWOLF, 1)));

        LEVEL_CONFIGS.put(4, new LevelSpawnConfig(Map.of(
                EnemyType.ARMORED_ORC, 2, EnemyType.ARMORED_SKELETON, 2,
                EnemyType.WEREWOLF, 1, EnemyType.ORC, 1)));

        LEVEL_CONFIGS.put(5, new LevelSpawnConfig(Map.of(
                EnemyType.ARMORED_ORC, 2, EnemyType.GREATSWORD_SKELETON, 2, EnemyType.WEREWOLF, 1)));

        LEVEL_CONFIGS.put(6, new LevelSpawnConfig(Map.of(
                EnemyType.ELITE_ORC, 1, EnemyType.ARMORED_ORC, 2, EnemyType.WEREBEAR, 1)));

        LEVEL_CONFIGS.put(7, new LevelSpawnConfig(Map.of(
                EnemyType.ORC_RIDER, 1, EnemyType.ELITE_ORC, 1,
                EnemyType.ARMORED_SKELETON, 2, EnemyType.WEREBEAR, 1)));

        LEVEL_CONFIGS.put(8, new LevelSpawnConfig(Map.of(
                EnemyType.ARMORED_ORC, 2, EnemyType.GREATSWORD_SKELETON, 2, EnemyType.WEREBEAR, 1)));

        LEVEL_CONFIGS.put(9, new LevelSpawnConfig(Map.of(
                EnemyType.ELITE_ORC, 2, EnemyType.ORC_RIDER, 1, EnemyType.WEREBEAR, 1)));

        LEVEL_CONFIGS.put(10, new LevelSpawnConfig(Map.of(
                EnemyType.MEDUSA_OF_CHAOS, 1), true));
    }

    public static LevelSpawnConfig getSpawnConfig(int level) { return LEVEL_CONFIGS.get(level); }

    public static int getMaxLevel() {
        return LEVEL_CONFIGS.keySet().stream().mapToInt(Integer::intValue).max().orElse(1);
    }

    public static boolean isBossLevel(int level) {
        LevelSpawnConfig cfg = getSpawnConfig(level);
        return cfg != null && cfg.isBossLevel;
    }

    public static List<EnemyType> getEnemyTypesForLevel(int level) {
        LevelSpawnConfig cfg = getSpawnConfig(level);
        return cfg != null ? new ArrayList<>(cfg.enemyCounts.keySet()) : new ArrayList<>();
    }

    public static int getTotalEnemiesForLevel(int level) {
        LevelSpawnConfig cfg = getSpawnConfig(level);
        return cfg != null ? cfg.totalEnemies : 0;
    }

    public static void setSpawnConfig(int level, Map<EnemyType, Integer> enemyCounts) {
        LEVEL_CONFIGS.put(level, new LevelSpawnConfig(enemyCounts));
    }

    public static void setSpawnConfig(int level, Map<EnemyType, Integer> enemyCounts, boolean isBossLevel) {
        LEVEL_CONFIGS.put(level, new LevelSpawnConfig(enemyCounts, isBossLevel));
    }
}
