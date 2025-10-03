package core;

import config.EnemySpawnConfig;
import enemies.Enemy;
import enemies.EnemyFactory;
import enemies.EnemyType;
import enemies.MedusaOfChaos;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import map.GameMap;
import map.Tile;

/**
 * Handles spawning of enemies on game maps based on level configuration.
 * Ensures enemies only spawn on floor tiles and maintains proper distribution.
 * Also manages boss spawning and clearing enemies.
 */
public class EnemySpawner {
    
    private static final Random random = new Random();
    private static final int MAX_SPAWN_ATTEMPTS = 100; // Max attempts to find a valid spawn position
    private static final int ENTRANCE_SAFE_RADIUS = 8;  // Keep entrance area clear of enemies
    private static final double EXIT_PREFERENCE_WEIGHT = 0.7; // 70% chance to prefer exit area
    
    public static List<Enemy> spawnEnemiesForLevel(GameMap gameMap, int level) {
        List<Enemy> spawnedEnemies = new ArrayList<>();
        
        EnemySpawnConfig.LevelSpawnConfig spawnConfig = EnemySpawnConfig.getSpawnConfig(level);
        if (spawnConfig == null) {
            System.err.println("[ENEMY SPAWNER] No spawn configuration found for level " + level);
            return spawnedEnemies;
        }
        
        // Get all available floor tiles for spawning
        List<int[]> floorTiles = getFloorTiles(gameMap);
        
        // Get safe floor tiles (away from entrance, prefer near exit)
        List<int[]> safeFloorTiles = getSafeSpawnTiles(gameMap, floorTiles);
        
        // Use safe tiles if available, otherwise fall back to all floor tiles
        List<int[]> spawnTiles = safeFloorTiles.isEmpty() ? floorTiles : safeFloorTiles;
        
        if (spawnTiles.isEmpty()) {
            System.err.println("[ENEMY SPAWNER] No floor tiles available for enemy spawning on level " + level);
            return spawnedEnemies;
        }
        
        // Spawn each type of enemy according to configuration
        for (Map.Entry<EnemyType, Integer> entry : spawnConfig.enemyCounts.entrySet()) {
            EnemyType enemyType = entry.getKey();
            int count = entry.getValue();
            
            for (int i = 0; i < count; i++) {
                int[] spawnPosition = findValidSpawnPosition(spawnTiles, spawnedEnemies);
                if (spawnPosition != null) {
                    Enemy enemy = EnemyFactory.createEnemy(enemyType, spawnPosition[0], spawnPosition[1]);
                    spawnedEnemies.add(enemy);
                    
                } 
            }
        }
        return spawnedEnemies;
    }
    
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
     * Filters floor tiles to create a safe spawn list:
     * - Excludes tiles too close to entrance (player spawn)
     * - Prefers tiles closer to exit (stairs down)
     */
    private static List<int[]> getSafeSpawnTiles(GameMap gameMap, List<int[]> allFloorTiles) {
        int entranceX = gameMap.getEntranceX();
        int entranceY = gameMap.getEntranceY();
        int exitX = gameMap.getExitX();
        int exitY = gameMap.getExitY();
        
        // If entrance/exit not set, return all tiles
        if (entranceX < 0 || exitX < 0) {
            return allFloorTiles;
        }
        
        List<int[]> safeTiles = new ArrayList<>();
        List<int[]> exitNearTiles = new ArrayList<>();
        
        for (int[] tile : allFloorTiles) {
            int x = tile[0];
            int y = tile[1];
            
            // Calculate distance from entrance
            double distanceFromEntrance = Math.hypot(x - entranceX, y - entranceY);
            
            // Skip tiles too close to entrance (safe zone for player)
            if (distanceFromEntrance < ENTRANCE_SAFE_RADIUS) {
                continue;
            }
            
            // Calculate distance from exit
            double distanceFromExit = Math.hypot(x - exitX, y - exitY);
            
            // Tiles closer to exit are preferred
            if (distanceFromExit < gameMap.getWidth() / 3.0) {
                exitNearTiles.add(tile);
            } else {
                safeTiles.add(tile);
            }
        }
        
        // Combine lists with preference for exit-near tiles
        List<int[]> result = new ArrayList<>();
        
        // Add exit-near tiles with weighted probability
        if (!exitNearTiles.isEmpty() && random.nextDouble() < EXIT_PREFERENCE_WEIGHT) {
            result.addAll(exitNearTiles);
        }
        
        // Always include remaining safe tiles
        result.addAll(safeTiles);
        
        return result;
    }

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
            
            if (!occupied) { return candidate; }
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
            
            if (!occupied) { return candidate; }
        }
        
        return null; // No valid position found
    }
    
    public static List<Enemy> spawnEnemiesAroundPoint(GameMap gameMap, int centerX, int centerY, int radius, EnemyType enemyType, int count) {
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

    public static void clearEnemies(List<Enemy> enemies) {
        enemies.clear();
    }
    
    public static boolean shouldSpawnBoss(int level) {
        if (!EnemySpawnConfig.isBossLevel(level)) {
            return false;
        }
        // Check if boss has been defeated
        return !MedusaOfChaos.isBossDefeated();
    }
}
