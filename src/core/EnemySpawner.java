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
 * Ensures enemies only spawn on floor tiles, respects entrance safe zone,
 * and uses weighted preference for tiles near the exit.
 */
public class EnemySpawner {

    private static final Random random = new Random();
    private static final int MAX_SPAWN_ATTEMPTS = 100;
    private static final int ENTRANCE_SAFE_RADIUS = 8;
    private static final double EXIT_PREFERENCE_WEIGHT = 0.7;

    public static List<Enemy> spawnEnemiesForLevel(GameMap map, int level) {
        List<Enemy> spawned = new ArrayList<>();
        EnemySpawnConfig.LevelSpawnConfig config = EnemySpawnConfig.getSpawnConfig(level);
        if (config == null) return spawned;

        List<int[]> floorTiles = getFloorTiles(map);
        List<int[]> spawnTiles = getSafeSpawnTiles(map, floorTiles);
        if (spawnTiles.isEmpty()) return spawned;

        for (Map.Entry<EnemyType, Integer> entry : config.enemyCounts.entrySet()) {
            EnemyType type = entry.getKey();
            int count = entry.getValue();
            for (int i = 0; i < count; i++) {
                int[] pos = findValidSpawnPosition(spawnTiles, spawned);
                if (pos != null) spawned.add(EnemyFactory.createEnemy(type, pos[0], pos[1]));
            }
        }
        return spawned;
    }

    private static List<int[]> getFloorTiles(GameMap map) {
        List<int[]> tiles = new ArrayList<>();
        for (int x = 0; x < map.getWidth(); x++)
            for (int y = 0; y < map.getHeight(); y++)
                if (map.getTile(x, y) != null && map.getTile(x, y).getType() == Tile.FLOOR)
                    tiles.add(new int[]{x, y});
        return tiles;
    }

    // ===== SAFE SPAWN TILES (weighted) =====
    private static List<int[]> getSafeSpawnTiles(GameMap map, List<int[]> allTiles) {
        int ex = map.getEntranceX(), ey = map.getEntranceY();
        int exitX = map.getExitX(), exitY = map.getExitY();
        if (ex < 0 || exitX < 0) return allTiles;

        List<int[]> result = new ArrayList<>();
        double widthThird = map.getWidth() / 3.0;

        for (int[] tile : allTiles) {
            double distEntrance = Math.hypot(tile[0] - ex, tile[1] - ey);
            if (distEntrance < ENTRANCE_SAFE_RADIUS) continue;

            double distExit = Math.hypot(tile[0] - exitX, tile[1] - exitY);
            if (distExit < widthThird && random.nextDouble() < EXIT_PREFERENCE_WEIGHT) {
                result.add(tile);
            } else if (distExit >= widthThird) {
                result.add(tile);
            }
        }
        return result;
    }

    private static int[] findValidSpawnPosition(List<int[]> tiles, List<Enemy> existing) {
        if (tiles.isEmpty()) return null;

        for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
            int[] candidate = tiles.get(random.nextInt(tiles.size()));
            if (existing.stream().noneMatch(e -> e.getX() == candidate[0] && e.getY() == candidate[1]))
                return candidate;
        }

        for (int[] candidate : tiles)
            if (existing.stream().noneMatch(e -> e.getX() == candidate[0] && e.getY() == candidate[1]))
                return candidate;

        return null;
    }

    public static List<Enemy> spawnEnemiesAroundPoint(GameMap map, int cx, int cy, int radius, EnemyType type, int count) {
        List<Enemy> spawned = new ArrayList<>();
        List<int[]> valid = new ArrayList<>();

        for (int x = cx - radius; x <= cx + radius; x++)
            for (int y = cy - radius; y <= cy + radius; y++)
                if (x >= 0 && y >= 0 && x < map.getWidth() && y < map.getHeight() &&
                    map.getTile(x, y).getType() == Tile.FLOOR)
                    valid.add(new int[]{x, y});

        for (int i = 0; i < count && !valid.isEmpty(); i++) {
            int[] pos = findValidSpawnPosition(valid, spawned);
            if (pos != null) spawned.add(EnemyFactory.createEnemy(type, pos[0], pos[1]));
        }
        return spawned;
    }

    public static void clearEnemies(List<Enemy> enemies) { enemies.clear(); }
    public static boolean shouldSpawnBoss(int level) { return EnemySpawnConfig.isBossLevel(level) && !MedusaOfChaos.isBossDefeated(); }
}
