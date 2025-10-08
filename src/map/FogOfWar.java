package map;

import utils.PathfindingAlgorithms;

/**
 * Manages fog of war for the game map with smooth gradient visibility effects.
 */
public class FogOfWar {

    private final boolean[][] visible;
    private final boolean[][] discovered;
    private final float[][] visibilityStrength; // Gradient: 0.0 to 1.0
    private final int width, height;

    public FogOfWar(int width, int height) {
        this.width = width;
        this.height = height;
        this.visible = new boolean[width][height];
        this.discovered = new boolean[width][height];
        this.visibilityStrength = new float[width][height];
    }

    // === GETTERS ===

    public boolean isVisible(int x, int y) { return inBounds(x, y) && visible[x][y]; }
    public boolean isDiscovered(int x, int y) { return inBounds(x, y) && discovered[x][y]; }
    public float getVisibilityStrength(int x, int y) { return inBounds(x, y) ? visibilityStrength[x][y] : 0f; }
    private boolean inBounds(int x, int y) { return x >= 0 && x < width && y >= 0 && y < height; }

    // === SETTERS ===

    public void setVisible(int x, int y, boolean visible) {
        if (!inBounds(x, y)) return;
        this.visible[x][y] = visible;
        if (visible) discovered[x][y] = true;
    }

    public void setDiscovered(int x, int y, boolean discovered) {
        if (inBounds(x, y)) this.discovered[x][y] = discovered;
    }

    // === VISIBILITY MANAGEMENT ===

    public void resetVisibility() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                visible[x][y] = false;
                visibilityStrength[x][y] = 0f;
            }
        }
    }

    // Updates visibility around a player with gradient-based line-of-sight.
    public void updateVisibility(GameMap map, int playerX, int playerY, int viewRadius) {
        resetVisibility();

        // Update previously visible tiles to discovered
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Tile tile = map.getTile(x, y);
                if (tile != null && tile.getFogState() == Tile.FogState.VISIBLE) {
                    tile.setFogState(Tile.FogState.DISCOVERED);
                }
            }
        }

        // Gradient visibility
        int minX = Math.max(0, playerX - viewRadius - 1);
        int maxX = Math.min(width - 1, playerX + viewRadius + 1);
        int minY = Math.max(0, playerY - viewRadius - 1);
        int maxY = Math.min(height - 1, playerY + viewRadius + 1);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                double distance = Math.hypot(playerX - x, playerY - y);

                if (distance <= viewRadius + 1.0 && hasLineOfSight(map, playerX, playerY, x, y)) {
                    float strength = calculateStrength(distance, viewRadius);

                    visibilityStrength[x][y] = strength;
                    setVisible(x, y, strength > 0.1f);

                    Tile tile = map.getTile(x, y);
                    if (tile != null) tile.setFogState(Tile.FogState.VISIBLE);
                }
            }
        }
    }

    private float calculateStrength(double distance, int viewRadius) {
        if (distance <= viewRadius - 1.0) return 1f;
        if (distance <= viewRadius) return 0.8f + 0.2f * (float)(viewRadius - distance);
        return Math.max(0f, 1f - (float)(distance - viewRadius));
    }

    // === LINE OF SIGHT ===

    private boolean hasLineOfSight(GameMap map, int x1, int y1, int x2, int y2) {
        if (x1 == x2 && y1 == y2) return true;

        return PathfindingAlgorithms.hasLineOfSight(x1, y1, x2, y2, (x, y) -> {
            Tile tile = map.getTile(x, y);
            return tile != null && tile.getType() == Tile.WALL;
        });
    }
}