package enemies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import map.GameMap;
import map.Tile;

/**
 * Handles spawning of enemies on game maps based on level configuration.
 * Ensures enemies only spawn on floor tiles and maintains proper distribution.
 */
public class EnemySpawner {
    
    private static final Random random = new Random();
    private static final int MAX_SPAWN_ATTEMPTS = 100; // Prevent infinite loops
    
    /**
     * Spawns enemies for the specified level on the given map.
     * 
     * @param gameMap The map to spawn enemies on
     * @param level The current level (1-based)
     * @return List of spawned enemies
     */
    public static List<Enemy> spawnEnemiesForLevel(GameMap gameMap, int level) {
        System.out.println("[ENEMY SPAWNER] Starting spawn process for level " + level);
        List<Enemy> spawnedEnemies = new ArrayList<>();
        
        EnemySpawnConfig.LevelSpawnConfig spawnConfig = EnemySpawnConfig.getSpawnConfig(level);
        if (spawnConfig == null) {
            System.err.println("[ENEMY SPAWNER] No spawn configuration found for level " + level);
            return spawnedEnemies;
        }
        
        System.out.println("[ENEMY SPAWNER] Found spawn config for level " + level + 
                          " with " + spawnConfig.totalEnemies + " total enemies");
        
        // Get all available floor tiles for spawning
        List<int[]> floorTiles = getFloorTiles(gameMap);
        System.out.println("[ENEMY SPAWNER] Found " + floorTiles.size() + " floor tiles for spawning");
        
        if (floorTiles.isEmpty()) {
            System.err.println("[ENEMY SPAWNER] No floor tiles available for enemy spawning on level " + level);
            return spawnedEnemies;
        }
        
        // Spawn each type of enemy according to configuration
        for (Map.Entry<EnemyType, Integer> entry : spawnConfig.enemyCounts.entrySet()) {
            EnemyType enemyType = entry.getKey();
            int count = entry.getValue();
            
            for (int i = 0; i < count; i++) {
                int[] spawnPosition = findValidSpawnPosition(floorTiles, spawnedEnemies);
                if (spawnPosition != null) {
                    Enemy enemy = EnemyFactory.createEnemy(enemyType, spawnPosition[0], spawnPosition[1]);
                    spawnedEnemies.add(enemy);
                    
                    System.out.println("[ENEMY SPAWNER] Spawned " + enemyType.getDisplayName() + 
                                     " at (" + spawnPosition[0] + ", " + spawnPosition[1] + ")");
                } else {
                    System.err.println("[ENEMY SPAWNER] Could not find valid spawn position for " + enemyType.getDisplayName());
                }
            }
        }
        
        System.out.println("[ENEMY SPAWNER] Completed spawning " + spawnedEnemies.size() + " enemies on level " + level);
        return spawnedEnemies;
    }
    
    /**
     * Gets all floor tiles from the map that can be used for spawning.
     * 
     * @param gameMap The game map
     * @return List of [x, y] coordinates of floor tiles
     */
    private static List<int[]> getFloorTiles(GameMap gameMap) {
        List<int[]> floorTiles = new ArrayList<>();
        
        for (int x = 0; x < gameMap.getWidth(); x++) {
            for (int y = 0; y < gameMap.getHeight(); y++) {
                Tile tile = gameMap.getTile(x, y);
                if (tile != null && tile.getType() == Tile.FLOOR) {
                    floorTiles.add(new int[]{x, y});
                }
            }
        }
        
        return floorTiles;
    }
    
    /**
     * Finds a valid spawn position that doesn't conflict with existing enemies.
     * 
     * @param floorTiles List of available floor tiles
     * @param existingEnemies List of already spawned enemies
     * @return [x, y] coordinates of a valid spawn position, or null if none found
     */
    private static int[] findValidSpawnPosition(List<int[]> floorTiles, List<Enemy> existingEnemies) {
        if (floorTiles.isEmpty()) {
            return null;
        }
        
        // Try random positions up to MAX_SPAWN_ATTEMPTS times
        for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
            int[] candidate = floorTiles.get(random.nextInt(floorTiles.size()));
            
            // Check if position is occupied by another enemy
            boolean occupied = false;
            for (Enemy enemy : existingEnemies) {
                if (enemy.getX() == candidate[0] && enemy.getY() == candidate[1]) {
                    occupied = true;
                    break;
                }
            }
            
            if (!occupied) {
                return candidate;
            }
        }
        
        // If we couldn't find a random position, try all positions sequentially
        for (int[] candidate : floorTiles) {
            boolean occupied = false;
            for (Enemy enemy : existingEnemies) {
                if (enemy.getX() == candidate[0] && enemy.getY() == candidate[1]) {
                    occupied = true;
                    break;
                }
            }
            
            if (!occupied) {
                return candidate;
            }
        }
        
        return null; // No valid position found
    }
    
    /**
     * Spawns enemies around a specific area (useful for testing or special spawning).
     * 
     * @param gameMap The game map
     * @param centerX Center X coordinate
     * @param centerY Center Y coordinate
     * @param radius Radius around the center to spawn in
     * @param enemyType Type of enemy to spawn
     * @param count Number of enemies to spawn
     * @return List of spawned enemies
     */
    public static List<Enemy> spawnEnemiesAroundPoint(GameMap gameMap, int centerX, int centerY, 
                                                      int radius, EnemyType enemyType, int count) {
        List<Enemy> spawnedEnemies = new ArrayList<>();
        List<int[]> validPositions = new ArrayList<>();
        
        // Find valid positions within radius
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int y = centerY - radius; y <= centerY + radius; y++) {
                if (x >= 0 && y >= 0 && x < gameMap.getWidth() && y < gameMap.getHeight()) {
                    Tile tile = gameMap.getTile(x, y);
                    if (tile != null && tile.getType() == Tile.FLOOR) {
                        validPositions.add(new int[]{x, y});
                    }
                }
            }
        }
        
        // Spawn enemies at random valid positions
        for (int i = 0; i < count && !validPositions.isEmpty(); i++) {
            int[] position = findValidSpawnPosition(validPositions, spawnedEnemies);
            if (position != null) {
                Enemy enemy = EnemyFactory.createEnemy(enemyType, position[0], position[1]);
                spawnedEnemies.add(enemy);
            }
        }
        
        return spawnedEnemies;
    }
    
    /**
     * Clears all enemies from a list (useful when changing levels).
     * 
     * @param enemies The list of enemies to clear
     */
    public static void clearEnemies(List<Enemy> enemies) {
        enemies.clear();
    }
    
    /**
     * Checks if the boss should be spawned on this level and handles boss persistence.
     * 
     * @param level The current level
     * @return true if the boss should be spawned (hasn't been defeated yet)
     */
    public static boolean shouldSpawnBoss(int level) {
        if (!EnemySpawnConfig.isBossLevel(level)) {
            return false;
        }
        
        // Check if boss has been defeated
        return !MedusaOfChaos.isBossDefeated();
    }
}
