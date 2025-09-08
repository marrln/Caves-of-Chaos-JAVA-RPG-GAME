package core;

import java.util.Random;

/**
 * Represents the game map with tiles and navigation.
 */
public class GameMap {
    
    private final Tile[][] tiles;
    private final int width;
    private final int height;
    private final int entranceX;
    private final int entranceY;
    private final int exitX;
    private final int exitY;
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
        
        // Generate random entrance and exit positions
        this.entranceX = random.nextInt(width - 4) + 2;
        this.entranceY = random.nextInt(height - 4) + 2;
        
        // Place exit far from entrance
        int exitQuadrant = random.nextInt(4);
        switch (exitQuadrant) {
            case 0 -> {
                // Top left
                this.exitX = random.nextInt(width / 2 - 2) + 2;
                this.exitY = random.nextInt(height / 2 - 2) + 2;
            }
            case 1 -> {
                // Top right
                this.exitX = random.nextInt(width / 2 - 2) + width / 2;
                this.exitY = random.nextInt(height / 2 - 2) + 2;
            }
            case 2 -> {
                // Bottom left
                this.exitX = random.nextInt(width / 2 - 2) + 2;
                this.exitY = random.nextInt(height / 2 - 2) + height / 2;
            }
            default -> {
                // Bottom right
                this.exitX = random.nextInt(width / 2 - 2) + width / 2;
                this.exitY = random.nextInt(height / 2 - 2) + height / 2;
            }
        }
        
        // Set entrance and exit tiles
        tiles[entranceX][entranceY] = new Tile(Tile.STAIRS_UP);
        tiles[exitX][exitY] = new Tile(Tile.STAIRS_DOWN);
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
     */
    public void generateCaves(double fillProbability, int iterations) {
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
        
        // Ensure entrance and exit are floor tiles
        tiles[entranceX][entranceY] = new Tile(Tile.STAIRS_UP);
        tiles[exitX][exitY] = new Tile(Tile.STAIRS_DOWN);
        
        // Make sure there's floor around entrance and exit
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                
                if (isInBounds(entranceX + dx, entranceY + dy)) {
                    tiles[entranceX + dx][entranceY + dy] = new Tile(Tile.FLOOR);
                }
                
                if (isInBounds(exitX + dx, exitY + dy)) {
                    tiles[exitX + dx][exitY + dy] = new Tile(Tile.FLOOR);
                }
            }
        }
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
