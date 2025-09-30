package map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents the game map with tiles and navigation.
 * Supports cave generation, path connectivity, and entrance/exit placement.
 */
public class GameMap {

    private final Tile[][] tiles;
    private final int width, height;
    private int entranceX, entranceY;
    private int exitX, exitY;
    private final Random random;

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[width][height];
        this.random = new Random();

        // Initialize all tiles as walls
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                tiles[x][y] = new Tile(Tile.WALL);

        entranceX = entranceY = exitX = exitY = -1;
    }

    // === BASIC TILE ACCESS ===

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getEntranceX() { return entranceX; }
    public int getEntranceY() { return entranceY; }
    public int getExitX() { return exitX; }
    public int getExitY() { return exitY; }

    public boolean isInBounds(int x, int y) { return x >= 0 && x < width && y >= 0 && y < height; }
    public Tile getTile(int x, int y) { return isInBounds(x, y) ? tiles[x][y] : null; }
    public void setTile(int x, int y, Tile tile) { if (isInBounds(x, y)) tiles[x][y] = tile; }

    public boolean isBlocked(int x, int y) {
        Tile tile = getTile(x, y);
        return tile == null || tile.isBlocked();
    }

    private boolean isEdgeTile(int x, int y) {
        return x == 0 || y == 0 || x == width - 1 || y == height - 1;
    }

    // === CAVE GENERATION ===

    public void generateCaves(double fillProbability, int iterations, boolean isFinalLevel, boolean isFirstLevel) {
        boolean validMap = false;
        int attempts = 0;
        final int maxAttempts = 10;

        while (!validMap && attempts++ < maxAttempts) {
            randomFillMap(fillProbability);
            for (int i = 0; i < iterations; i++) iterateCellularAutomata();

            if (findAndPlaceStairs(isFinalLevel, isFirstLevel) && validateAndSealMap()) {
                validMap = true;
            }
        }

        if (!validMap) {
            System.err.println("Failed to generate valid cave after " + maxAttempts + " attempts.");
        }
    }

    private void randomFillMap(double fillProbability) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (isEdgeTile(x, y)) tiles[x][y] = new Tile(Tile.WALL);
                else tiles[x][y] = (random.nextDouble() < fillProbability) ? new Tile(Tile.WALL) : new Tile(Tile.FLOOR);
            }
        }
    }

    private void iterateCellularAutomata() {
        Tile[][] newTiles = new Tile[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (isEdgeTile(x, y)) newTiles[x][y] = new Tile(Tile.WALL);
                else {
                    int wallCount = countNeighboringWalls(x, y);
                    Tile current = tiles[x][y];
                    if (current.getType() == Tile.WALL)
                        newTiles[x][y] = (wallCount >= 4) ? new Tile(Tile.WALL) : new Tile(Tile.FLOOR);
                    else
                        newTiles[x][y] = (wallCount >= 5) ? new Tile(Tile.WALL) : new Tile(Tile.FLOOR);
                }
            }
        }

        for (int x = 0; x < width; x++) System.arraycopy(newTiles[x], 0, tiles[x], 0, height);
    }

    private int countNeighboringWalls(int x, int y) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                if (tiles[x + dx][y + dy].getType() == Tile.WALL) count++;
        return count;
    }

    // === STAIRS PLACEMENT & PATHS ===

    private boolean findAndPlaceStairs(boolean isFinalLevel, boolean isFirstLevel) {
        List<int[]> floorTiles = new ArrayList<>();

        for (int x = 3; x < width - 3; x++)
            for (int y = 3; y < height - 3; y++)
                if (!isBlocked(x, y) && nearbyFloorCount(x, y) >= 6)
                    floorTiles.add(new int[]{x, y});

        if (floorTiles.size() < 2) return false;

        int entranceIndex = random.nextInt(Math.min(floorTiles.size() / 4 + 1, floorTiles.size()));
        int[] entrancePos = floorTiles.get(entranceIndex);
        entranceX = entrancePos[0]; entranceY = entrancePos[1];

        int[] exitPos = null; double maxDistance = 0;
        int startIdx = Math.max(0, floorTiles.size() * 3 / 4);
        for (int i = startIdx; i < floorTiles.size(); i++) {
            int[] pos = floorTiles.get(i);
            double distance = Math.hypot(pos[0] - entranceX, pos[1] - entranceY);
            if (distance > maxDistance) { maxDistance = distance; exitPos = pos; }
        }
        if (exitPos == null) exitPos = floorTiles.get(floorTiles.size() - 1);
        exitX = exitPos[0]; exitY = exitPos[1];

        ensureAreaAccessible(entranceX, entranceY, 2);
        ensureAreaAccessible(exitX, exitY, 2);
        createPath(entranceX, entranceY, exitX, exitY);

        tiles[entranceX][entranceY] = new Tile(isFirstLevel ? Tile.FLOOR : Tile.STAIRS_UP);
        tiles[exitX][exitY] = new Tile(isFinalLevel ? Tile.FLOOR : Tile.STAIRS_DOWN);

        return true;
    }

    private int nearbyFloorCount(int x, int y) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                if (!isBlocked(x + dx, y + dy)) count++;
        return count;
    }

    private void ensureAreaAccessible(int cx, int cy, int radius) {
        for (int dx = -radius; dx <= radius; dx++)
            for (int dy = -radius; dy <= radius; dy++) {
                int x = cx + dx, y = cy + dy;
                if (isInBounds(x, y) && !isEdgeTile(x, y)) tiles[x][y] = new Tile(Tile.FLOOR);
            }
    }

    private void createPath(int x1, int y1, int x2, int y2) {
        List<utils.LineUtils.Point> points = utils.LineUtils.getLinePoints(x1, y1, x2, y2);
        for (utils.LineUtils.Point p : points) {
            if (isInBounds(p.x, p.y) && !isEdgeTile(p.x, p.y)) {
                tiles[p.x][p.y] = new Tile(Tile.FLOOR);
                createCorridorWidth(p.x, p.y);
            }
        }
    }

    private void createCorridorWidth(int cx, int cy) {
        int[] dx = {-1, 1, 0, 0};
        int[] dy = {0, 0, -1, 1};
        for (int i = 0; i < 4; i++) {
            int x = cx + dx[i], y = cy + dy[i];
            if (isInBounds(x, y) && !isEdgeTile(x, y)) tiles[x][y] = new Tile(Tile.FLOOR);
        }
    }

    // === MAP VALIDATION ===

    private boolean validateMap() {
        int floorCount = 0, totalTiles = width * height;
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                if (tiles[x][y].getType() != Tile.WALL) floorCount++;

        boolean connected = isPathConnected(entranceX, entranceY, exitX, exitY);
        return ((double) floorCount / totalTiles >= 0.25) && connected;
    }

    /**
     * Validates the map and seals off any isolated caverns to ensure all accessible areas
     * are connected to the main cave system. This prevents enemies from spawning in unreachable areas.
     */
    private boolean validateAndSealMap() {
        // First, check basic map validity
        if (!validateMap()) {
            return false;
        }

        // Use flood-fill to identify all tiles accessible from the entrance
        boolean[][] accessible = new boolean[width][height];
        markAccessibleTiles(entranceX, entranceY, accessible);

        // Seal off any floor tiles that are not accessible
        int sealedTiles = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (tiles[x][y].getType() == Tile.FLOOR && !accessible[x][y]) {
                    tiles[x][y] = new Tile(Tile.WALL);
                    sealedTiles++;
                }
            }
        }

        // Verify the exit is still accessible after sealing
        return isPathConnected(entranceX, entranceY, exitX, exitY);
    }

    /**
     * Uses flood-fill algorithm to mark all tiles accessible from the starting position.
     * This identifies the main connected cave system.
     */
    private void markAccessibleTiles(int startX, int startY, boolean[][] accessible) {
        if (!isInBounds(startX, startY) || accessible[startX][startY] || tiles[startX][startY].isBlocked()) {
            return;
        }

        accessible[startX][startY] = true;

        // Recursively mark all connected floor tiles
        markAccessibleTiles(startX + 1, startY, accessible);
        markAccessibleTiles(startX - 1, startY, accessible);
        markAccessibleTiles(startX, startY + 1, accessible);
        markAccessibleTiles(startX, startY - 1, accessible);
    }

    private boolean isPathConnected(int startX, int startY, int endX, int endY) {
        boolean[][] visited = new boolean[width][height];
        return floodFill(startX, startY, endX, endY, visited);
    }

    private boolean floodFill(int x, int y, int targetX, int targetY, boolean[][] visited) {
        if (!isInBounds(x, y) || visited[x][y] || tiles[x][y].isBlocked()) return false;
        if (x == targetX && y == targetY) return true;
        visited[x][y] = true;
        return floodFill(x + 1, y, targetX, targetY, visited) ||
               floodFill(x - 1, y, targetX, targetY, visited) ||
               floodFill(x, y + 1, targetX, targetY, visited) ||
               floodFill(x, y - 1, targetX, targetY, visited);
    }
}