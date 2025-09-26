package map;

import items.Item;
import java.util.Optional;

/**
 * Represents a single tile in the game map.
 * Handles tile type, blocking status, fog of war, and contained items.
 */
public class Tile {

    // ===== Tile Types =====
    public static final int FLOOR       = 0;
    public static final int WALL        = 1;
    public static final int DOOR        = 2;
    public static final int STAIRS_UP   = 3;
    public static final int STAIRS_DOWN = 4;

    // ===== Fog of War States =====
    public enum FogState {
        UNDISCOVERED,  // Not yet seen
        DISCOVERED,    // Seen before but not currently visible
        VISIBLE        // Currently visible
    }

    // ===== Instance Fields =====
    private final int type;
    private boolean explored;
    private boolean blocked;
    private FogState fogState = FogState.UNDISCOVERED;
    private Optional<Item> item = Optional.empty();

    // ===== Constructor =====
    public Tile(int type) {
        this.type = type;
        this.explored = false;
        this.blocked = switch (type) {
            case WALL -> true;
            default -> false;
        };
    }

    // ===== Tile Properties =====

    public int getType() { return type; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public boolean isExplored() { return explored; }
    public void setExplored(boolean explored) { this.explored = explored; }
    public FogState getFogState() { return fogState; }

    public void setFogState(FogState fogState) {
        this.fogState = fogState;
        if (fogState != FogState.UNDISCOVERED) {
            this.explored = true;
        }
    }

    // ===== Item Management =====
    
    public boolean hasItem() { return item.isPresent(); }
    public Optional<Item> getItem() { return item; }
    public void setItem(Item item) { this.item = Optional.ofNullable(item); }

    public Optional<Item> removeItem() {
        Optional<Item> removed = this.item;
        this.item = Optional.empty();
        return removed;
    }
}
