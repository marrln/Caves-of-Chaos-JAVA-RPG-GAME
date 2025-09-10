package map;

/**
 * Represents a single tile in the game map.
 */
public class Tile {
    
    // Tile types
    public static final int FLOOR = 0;
    public static final int WALL = 1;
    public static final int DOOR = 2;
    public static final int STAIRS_UP = 3;
    public static final int STAIRS_DOWN = 4;
    public static final int WATER = 5;
    public static final int LAVA = 6;
    
    /**
     * Fog of war state for a tile.
     */
    public enum FogState {
        UNDISCOVERED,  // Not seen yet
        DISCOVERED,    // Seen before but not currently visible
        VISIBLE        // Currently visible
    }
    
    private final int type;
    private boolean explored;
    private boolean blocked;
    private FogState fogState = FogState.UNDISCOVERED;
    
    /**
     * Creates a new tile of the specified type.
     * 
     * @param type The type of tile
     */
    public Tile(int type) {
        this.type = type;
        this.explored = false;
        
        // Set blocked status based on tile type
        switch (type) {
            case WALL:
            case WATER:
            case LAVA:
                this.blocked = true;
                break;
            default:
                this.blocked = false;
        }
    }
    
    /**
     * Checks if this tile blocks movement.
     * 
     * @return true if the tile blocks movement, false otherwise
     */
    public boolean isBlocked() {
        return blocked;
    }
    
    /**
     * Sets whether this tile blocks movement.
     * 
     * @param blocked true to block movement, false to allow movement
     */
    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
    
    /**
     * Gets the type of this tile.
     * 
     * @return The tile type
     */
    public int getType() {
        return type;
    }
    
    /**
     * Checks if this tile has been explored by the player.
     * 
     * @return true if the tile has been explored, false otherwise
     */
    public boolean isExplored() {
        return explored;
    }
    
    /**
     * Sets whether this tile has been explored by the player.
     * 
     * @param explored true if the tile has been explored, false otherwise
     */
    public void setExplored(boolean explored) {
        this.explored = explored;
    }
    
    /**
     * Gets the current fog state of this tile.
     * 
     * @return The fog state
     */
    public FogState getFogState() {
        return fogState;
    }
    
    /**
     * Sets the fog state of this tile.
     * 
     * @param fogState The new fog state
     */
    public void setFogState(FogState fogState) {
        this.fogState = fogState;
        
        // Update explored status based on fog state
        if (fogState != FogState.UNDISCOVERED) {
            this.explored = true;
        }
    }
}
