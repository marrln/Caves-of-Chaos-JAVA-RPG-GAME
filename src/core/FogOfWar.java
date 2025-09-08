package core;

/**
 * Manages fog of war for the game map.
 */
public class FogOfWar {
    
    private final boolean[][] visible;
    private final boolean[][] discovered;
    private final int width;
    private final int height;
    
    /**
     * Creates a new fog of war for a map with the specified dimensions.
     * 
     * @param width The width of the map
     * @param height The height of the map
     */
    public FogOfWar(int width, int height) {
        this.width = width;
        this.height = height;
        this.visible = new boolean[width][height];
        this.discovered = new boolean[width][height];
    }
    
    /**
     * Checks if the tile at the specified position is currently visible.
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @return true if the tile is visible, false otherwise
     */
    public boolean isVisible(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return visible[x][y];
        }
        return false;
    }
    
    /**
     * Checks if the tile at the specified position has been discovered.
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @return true if the tile has been discovered, false otherwise
     */
    public boolean isDiscovered(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return discovered[x][y];
        }
        return false;
    }
    
    /**
     * Sets the visibility of the tile at the specified position.
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @param visible true to make the tile visible, false otherwise
     */
    public void setVisible(int x, int y, boolean visible) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            this.visible[x][y] = visible;
            
            // If a tile becomes visible, it's also discovered
            if (visible) {
                discovered[x][y] = true;
            }
        }
    }
    
    /**
     * Sets the discovered state of the tile at the specified position.
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @param discovered true to mark the tile as discovered, false otherwise
     */
    public void setDiscovered(int x, int y, boolean discovered) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            this.discovered[x][y] = discovered;
        }
    }
    
    /**
     * Resets the visibility of all tiles, but keeps their discovered state.
     */
    public void resetVisibility() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                visible[x][y] = false;
            }
        }
    }
    
    /**
     * Updates the visibility around a player using a simple line-of-sight algorithm.
     * 
     * @param map The game map
     * @param playerX The player's x coordinate
     * @param playerY The player's y coordinate
     * @param viewRadius The radius of the player's vision
     */
    public void updateVisibility(GameMap map, int playerX, int playerY, int viewRadius) {
        // Reset visibility first
        resetVisibility();
        
        // Update all tiles to DISCOVERED state if they were previously VISIBLE
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile tile = map.getTile(x, y);
                if (tile != null && tile.getFogState() == Tile.FogState.VISIBLE) {
                    tile.setFogState(Tile.FogState.DISCOVERED);
                }
            }
        }
        
        // Mark the tiles around the player as visible
        for (int x = Math.max(0, playerX - viewRadius); x <= Math.min(width - 1, playerX + viewRadius); x++) {
            for (int y = Math.max(0, playerY - viewRadius); y <= Math.min(height - 1, playerY + viewRadius); y++) {
                // Calculate distance to player
                double distance = Math.sqrt(Math.pow(playerX - x, 2) + Math.pow(playerY - y, 2));
                
                // Check if within view radius
                if (distance <= viewRadius) {
                    // Check line of sight
                    if (hasLineOfSight(map, playerX, playerY, x, y)) {
                        setVisible(x, y, true);
                        
                        // Update the tile's fog state
                        Tile tile = map.getTile(x, y);
                        if (tile != null) {
                            tile.setFogState(Tile.FogState.VISIBLE);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Checks if there's a clear line of sight between two points.
     * 
     * @param map The game map
     * @param x1 The starting x coordinate
     * @param y1 The starting y coordinate
     * @param x2 The ending x coordinate
     * @param y2 The ending y coordinate
     * @return true if there's a clear line of sight, false otherwise
     */
    private boolean hasLineOfSight(GameMap map, int x1, int y1, int x2, int y2) {
        // Bresenham's line algorithm
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int x = x1;
        int y = y1;
        
        while (x != x2 || y != y2) {
            // Check if current position blocks vision (except start and end points)
            if ((x != x1 || y != y1) && (x != x2 || y != y2)) {
                Tile tile = map.getTile(x, y);
                if (tile != null && tile.getType() == Tile.WALL) {
                    return false;
                }
            }
            
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
        
        return true;
    }
}
