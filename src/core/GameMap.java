package core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents the game map with tiles and navigation.
 */
public class GameMap {
    
    private final Tile[][] tiles;
    private final int width;
    private final int height;
    private int entranceX;
    private int entranceY;
    private int exitX;
    private int exitY;
    private final Random random;
    
    /**
     * Creates a new map with the specified dimensions.
     * 
     * @param width The width of the map
     * @param height The height of the map
     */
    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[width][height];
        this.random = new Random();
        
        // Initialize all tiles as walls
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = new Tile(Tile.WALL);
            }
        }
        
        // Entrance and exit will be placed AFTER map generation
        this.entranceX = -1;
        this.entranceY = -1;
        this.exitX = -1;
        this.exitY = -1;
    }
    
    /**
     * Checks if the specified position is within map bounds.
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @return true if the position is within bounds, false otherwise
     */
    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
    
    /**
     * Gets the tile at the specified position.
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @return The tile at the position, or null if out of bounds
     */
    public Tile getTile(int x, int y) {
        if (isInBounds(x, y)) {
            return tiles[x][y];
        }
        return null;
    }
    
    /**
     * Sets the tile at the specified position.
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @param tile The tile to set
     */
    public void setTile(int x, int y, Tile tile) {
        if (isInBounds(x, y)) {
            tiles[x][y] = tile;
        }
    }
    
    /**
     * Checks if the specified position is blocked.
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @return true if the position is blocked, false otherwise
     */
    public boolean isBlocked(int x, int y) {
        Tile tile = getTile(x, y);
        return tile == null || tile.isBlocked();
    }
    
    /**
     * Gets the width of the map.
     * 
     * @return The map width
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Gets the height of the map.
     * 
     * @return The map height
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * Gets the x coordinate of the entrance.
     * 
     * @return The entrance x coordinate
     */
    public int getEntranceX() {
        return entranceX;
    }
    
    /**
     * Gets the y coordinate of the entrance.
     * 
     * @return The entrance y coordinate
     */
    public int getEntranceY() {
        return entranceY;
    }
    
    /**
     * Gets the x coordinate of the exit.
     * 
     * @return The exit x coordinate
     */
    public int getExitX() {
        return exitX;
    }
    
    /**
     * Gets the y coordinate of the exit.
     * 
     * @return The exit y coordinate
     */
    public int getExitY() {
        return exitY;
    }
    
    /**
     * Generates a dungeon map using a cellular automata algorithm.
     * 
     * @param fillProbability The probability of a cell starting as a wall
     * @param iterations The number of cellular automata iterations
     * @param isFinalLevel Whether this is the final level (no down stairs needed)
     */
    public void generateCaves(double fillProbability, int iterations, boolean isFinalLevel) {
        boolean validMapGenerated = false;
        int attempts = 0;
        final int maxAttempts = 10;
        
        while (!validMapGenerated && attempts < maxAttempts) {
            attempts++;
            
            // Initially fill the map randomly
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // Keep borders as walls
                    if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                        tiles[x][y] = new Tile(Tile.WALL);
                    } else {
                        // Random fill
                        if (random.nextDouble() < fillProbability) {
                            tiles[x][y] = new Tile(Tile.WALL);
                        } else {
                            tiles[x][y] = new Tile(Tile.FLOOR);
                        }
                    }
                }
            }
            
            // Run cellular automata iterations
            for (int i = 0; i < iterations; i++) {
                iterateCellularAutomata();
            }
            
            // NOW find good positions for entrance and exit
            if (findAndPlaceStairs(isFinalLevel)) {
                // Validate that the map has enough open space and connectivity
                if (validateMap()) {
                    validMapGenerated = true;
                    System.out.println("Generated valid cave map on attempt " + attempts);
                    System.out.println("Entrance placed at: (" + entranceX + ", " + entranceY + ")");
                    System.out.println("Exit placed at: (" + exitX + ", " + exitY + ")");
                }
            }
        }
        
        // If we couldn't generate a valid map, create a simple fallback
        if (!validMapGenerated) {
            System.out.println("Failed to generate valid cave after " + maxAttempts + " attempts, using fallback");
            createFallbackMap(isFinalLevel);
        }
    }
    
    /**
     * Finds suitable positions for entrance and exit stairs after map generation.
     * Places stairs in good floor locations that are well-separated.
     * 
     * @param isFinalLevel Whether this is the final level (no down stairs needed)
     * @return true if stairs were successfully placed, false otherwise
     */
    private boolean findAndPlaceStairs(boolean isFinalLevel) {
        // Find all floor tiles that could be good candidates
        List<int[]> floorTiles = new ArrayList<>();
        
        // Look for floor tiles not too close to borders and with good clearance
        for (int x = 3; x < width - 3; x++) {
            for (int y = 3; y < height - 3; y++) {
                if (!isBlocked(x, y)) {
                    // Check that there's some open space around this position
                    int nearbyFloorCount = 0;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (!isBlocked(x + dx, y + dy)) {
                                nearbyFloorCount++;
                            }
                        }
                    }
                    
                    // Good position if most nearby tiles are floor
                    if (nearbyFloorCount >= 6) {
                        floorTiles.add(new int[]{x, y});
                    }
                }
            }
        }
        
        if (floorTiles.size() < 2) {
            System.out.println("Not enough good floor positions found for stairs");
            return false;
        }
        
        // Choose entrance from first quarter of candidates
        int entranceIndex = random.nextInt(Math.min(floorTiles.size() / 4 + 1, floorTiles.size()));
        int[] entrancePos = floorTiles.get(entranceIndex);
        this.entranceX = entrancePos[0];
        this.entranceY = entrancePos[1];
        
        // Choose exit from last quarter, ensuring it's far from entrance
        int[] exitPos = null;
        double maxDistance = 0;
        int startIdx = Math.max(0, floorTiles.size() * 3 / 4);
        
        for (int i = startIdx; i < floorTiles.size(); i++) {
            int[] pos = floorTiles.get(i);
            double distance = Math.sqrt(Math.pow(pos[0] - entranceX, 2) + Math.pow(pos[1] - entranceY, 2));
            if (distance > maxDistance) {
                maxDistance = distance;
                exitPos = pos;
            }
        }
        
        if (exitPos == null) {
            // Fallback: just pick the last position
            exitPos = floorTiles.get(floorTiles.size() - 1);
        }
        
        this.exitX = exitPos[0];
        this.exitY = exitPos[1];
        
        // Ensure areas around stairs are accessible
        ensureAreaAccessible(entranceX, entranceY, 2);
        ensureAreaAccessible(exitX, exitY, 2);
        
        // Create a path between entrance and exit to ensure connectivity
        createPath(entranceX, entranceY, exitX, exitY);
        
        // Place the actual stair tiles
        tiles[entranceX][entranceY] = new Tile(Tile.STAIRS_UP);
        
        // Only place down stairs if this is not the final level
        if (!isFinalLevel) {
            tiles[exitX][exitY] = new Tile(Tile.STAIRS_DOWN);
        } else {
            // On final level, the exit remains a regular floor tile
            tiles[exitX][exitY] = new Tile(Tile.FLOOR);
        }
        
        return true;
    }
    
    /**
     * Ensures an area around the specified coordinates is accessible (floor tiles).
     * 
     * @param centerX The center x coordinate
     * @param centerY The center y coordinate  
     * @param radius The radius around the center to clear
     */
    private void ensureAreaAccessible(int centerX, int centerY, int radius) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int x = centerX + dx;
                int y = centerY + dy;
                
                if (isInBounds(x, y) && !(x == 0 || x == width - 1 || y == 0 || y == height - 1)) {
                    tiles[x][y] = new Tile(Tile.FLOOR);
                }
            }
        }
    }
    
    /**
     * Creates a guaranteed path between two points using a simple line algorithm.
     * 
     * @param x1 Start x coordinate
     * @param y1 Start y coordinate
     * @param x2 End x coordinate
     * @param y2 End y coordinate
     */
    private void createPath(int x1, int y1, int x2, int y2) {
        // Create an L-shaped path (horizontal then vertical)
        int currentX = x1;
        int currentY = y1;
        
        // Move horizontally first
        while (currentX != x2) {
            if (isInBounds(currentX, currentY) && !(currentX == 0 || currentX == width - 1 || currentY == 0 || currentY == height - 1)) {
                tiles[currentX][currentY] = new Tile(Tile.FLOOR);
                // Clear adjacent tiles to make corridor wider
                if (isInBounds(currentX, currentY - 1) && currentY - 1 > 0) {
                    tiles[currentX][currentY - 1] = new Tile(Tile.FLOOR);
                }
                if (isInBounds(currentX, currentY + 1) && currentY + 1 < height - 1) {
                    tiles[currentX][currentY + 1] = new Tile(Tile.FLOOR);
                }
            }
            currentX += (x2 > x1) ? 1 : -1;
        }
        
        // Move vertically
        while (currentY != y2) {
            if (isInBounds(currentX, currentY) && !(currentX == 0 || currentX == width - 1 || currentY == 0 || currentY == height - 1)) {
                tiles[currentX][currentY] = new Tile(Tile.FLOOR);
                // Clear adjacent tiles to make corridor wider
                if (isInBounds(currentX - 1, currentY) && currentX - 1 > 0) {
                    tiles[currentX - 1][currentY] = new Tile(Tile.FLOOR);
                }
                if (isInBounds(currentX + 1, currentY) && currentX + 1 < width - 1) {
                    tiles[currentX + 1][currentY] = new Tile(Tile.FLOOR);
                }
            }
            currentY += (y2 > y1) ? 1 : -1;
        }
    }
    
    /**
     * Validates that the generated map has sufficient open space and connectivity.
     * 
     * @return true if the map is valid, false otherwise
     */
    private boolean validateMap() {
        int floorCount = 0;
        int totalTiles = width * height;
        
        // Count floor tiles
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (tiles[x][y].getType() == Tile.FLOOR || 
                    tiles[x][y].getType() == Tile.STAIRS_UP || 
                    tiles[x][y].getType() == Tile.STAIRS_DOWN) {
                    floorCount++;
                }
            }
        }
        
        // Ensure at least 25% of the map is open space
        double openSpaceRatio = (double) floorCount / totalTiles;
        
        // Check connectivity between entrance and exit using flood fill
        boolean isConnected = isPathConnected(entranceX, entranceY, exitX, exitY);
        
        return openSpaceRatio >= 0.25 && isConnected;
    }
    
    /**
     * Checks if there's a connected path between two points using flood fill.
     * 
     * @param startX Start x coordinate
     * @param startY Start y coordinate
     * @param endX End x coordinate
     * @param endY End y coordinate
     * @return true if path exists, false otherwise
     */
    private boolean isPathConnected(int startX, int startY, int endX, int endY) {
        boolean[][] visited = new boolean[width][height];
        return floodFill(startX, startY, endX, endY, visited);
    }
    
    /**
     * Recursive flood fill to check connectivity.
     */
    private boolean floodFill(int x, int y, int targetX, int targetY, boolean[][] visited) {
        if (!isInBounds(x, y) || visited[x][y] || tiles[x][y].isBlocked()) {
            return false;
        }
        
        if (x == targetX && y == targetY) {
            return true;
        }
        
        visited[x][y] = true;
        
        // Check all 4 directions
        return floodFill(x + 1, y, targetX, targetY, visited) ||
               floodFill(x - 1, y, targetX, targetY, visited) ||
               floodFill(x, y + 1, targetX, targetY, visited) ||
               floodFill(x, y - 1, targetX, targetY, visited);
    }
    
    /**
     * Creates a simple fallback map when generation fails.
     */
    private void createFallbackMap(boolean isFinalLevel) {
        // Fill with walls
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = new Tile(Tile.WALL);
            }
        }
        
        // Create a simple rectangular room in the center
        int roomWidth = Math.min(width - 10, 20);
        int roomHeight = Math.min(height - 10, 15);
        int startX = (width - roomWidth) / 2;
        int startY = (height - roomHeight) / 2;
        
        for (int x = startX; x < startX + roomWidth; x++) {
            for (int y = startY; y < startY + roomHeight; y++) {
                tiles[x][y] = new Tile(Tile.FLOOR);
            }
        }
        
        // Place entrance and exit in the room
        tiles[startX + 2][startY + 2] = new Tile(Tile.STAIRS_UP);
        
        // Only place down stairs if this is not the final level
        if (!isFinalLevel) {
            tiles[startX + roomWidth - 3][startY + roomHeight - 3] = new Tile(Tile.STAIRS_DOWN);
        } else {
            // On final level, the exit remains a regular floor tile
            tiles[startX + roomWidth - 3][startY + roomHeight - 3] = new Tile(Tile.FLOOR);
        }
        
        // Set entrance and exit coordinates for the fallback map
        this.entranceX = startX + 2;
        this.entranceY = startY + 2;
        this.exitX = startX + roomWidth - 3;
        this.exitY = startY + roomHeight - 3;
    }
    
    /**
     * Performs one iteration of cellular automata for cave generation.
     */
    private void iterateCellularAutomata() {
        Tile[][] newTiles = new Tile[width][height];
        
        // Initialize all border tiles as walls
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    newTiles[x][y] = new Tile(Tile.WALL);
                } else {
                    // Count neighboring wall tiles
                    int wallCount = 0;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (tiles[x + dx][y + dy].getType() == Tile.WALL) {
                                wallCount++;
                            }
                        }
                    }
                    
                    // Apply cellular automata rules
                    if (tiles[x][y].getType() == Tile.WALL) {
                        // Wall remains if it has 4 or more wall neighbors
                        newTiles[x][y] = (wallCount >= 4) ? 
                                new Tile(Tile.WALL) : new Tile(Tile.FLOOR);
                    } else {
                        // Floor becomes wall if it has 5 or more wall neighbors
                        newTiles[x][y] = (wallCount >= 5) ? 
                                new Tile(Tile.WALL) : new Tile(Tile.FLOOR);
                    }
                }
            }
        }
        
        // Update the map
        for (int x = 0; x < width; x++) {
            System.arraycopy(newTiles[x], 0, tiles[x], 0, height);
        }
    }
}
