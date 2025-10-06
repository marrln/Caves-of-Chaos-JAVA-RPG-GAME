package graphics;

import config.Config;
import config.StyleConfig;
import java.awt.*;
import java.awt.image.BufferedImage;
import map.FogOfWar;
import map.GameMap;
import map.Tile;
import ui.Camera;

/**
 * Handles rendering of tiles with PNG graphics when available,
 * fallback to colored rectangles when graphics are disabled or assets are missing.
 * Supports fog of war and efficient batch rendering.
 */
public class TileRenderer {

    // ====== CONFIG & ASSETS ======
    private final AssetManager assetManager = AssetManager.getInstance();
    private final int tileSize = Config.getIntSetting("tile_size");
    private final boolean useGraphics = Config.getBoolSetting("use_graphics");

    // ====== FALLBACK COLORS ======
    private final Color wallColor       = StyleConfig.getColor("tileWall", Color.DARK_GRAY);
    private final Color floorColor      = StyleConfig.getColor("tileFloor", Color.LIGHT_GRAY);
    private final Color stairsDownColor = StyleConfig.getColor("tileStairsDown", Color.YELLOW);
    private final Color stairsUpColor   = StyleConfig.getColor("tileStairsUp", Color.ORANGE);
    private final Color entranceColor   = StyleConfig.getColor("tileEntrance", Color.PINK);
    private final Color unknownColor    = StyleConfig.getColor("tileUnknown", Color.MAGENTA);

    // ====== FOG OF WAR COLORS ======
    private final Color fogUndiscovered = StyleConfig.getColor("fogUndiscovered", new Color(0, 0, 0, 255));
    private final Color fogDiscovered   = StyleConfig.getColor("fogDiscovered", new Color(15, 20, 35));
    private final Color fogEdge         = StyleConfig.getColor("fogEdge", new Color(10, 15, 25));

    public TileRenderer() {
        if (useGraphics) assetManager.preloadTileAssets();
    }

    // ====== TILE RENDERING ======
    public void renderTile(Graphics g, Tile tile, int screenX, int screenY, boolean isEntrance) {
        Color fallback = getFallbackColor(tile, isEntrance);
        if (!useGraphics || !renderWithGraphics(g, tile, screenX, screenY, isEntrance)) {
            g.setColor(fallback);
            g.fillRect(screenX, screenY, tileSize, tileSize);
        }
    }

    private boolean renderWithGraphics(Graphics g, Tile tile, int x, int y, boolean isEntrance) {
        String assetId = getTileAssetId(tile, isEntrance);
        if (assetId == null) return false;

        BufferedImage img = assetManager.loadImage(assetId);
        if (img == null) return false;

        Image drawImg = (img.getWidth() != tileSize || img.getHeight() != tileSize)
                        ? img.getScaledInstance(tileSize, tileSize, Image.SCALE_SMOOTH)
                        : img;
        g.drawImage(drawImg, x, y, null);
        return true;
    }

    private String getTileAssetId(Tile tile, boolean isEntrance) {
        // Priority: Always render stairs up/down tiles correctly, even if they're at entrance
        return switch (tile.getType()) {
            case Tile.WALL -> "wall";
            case Tile.FLOOR -> isEntrance ? "spawn_point" : "floor";
            case Tile.STAIRS_DOWN -> "stairs_down";
            case Tile.STAIRS_UP -> "stairs_up";
            default -> null;
        };
    }

    private Color getFallbackColor(Tile tile, boolean isEntrance) {
        // Priority: Always render stairs up/down tiles correctly, even if they're at entrance
        return switch (tile.getType()) {
            case Tile.WALL -> wallColor;
            case Tile.FLOOR -> isEntrance ? entranceColor : floorColor;
            case Tile.STAIRS_DOWN -> stairsDownColor;
            case Tile.STAIRS_UP -> stairsUpColor;
            default -> unknownColor;
        };
    }

    // ====== ITEM RENDERING ======
    public void renderTileWithItem(Graphics g, Tile tile, int x, int y, boolean isEntrance) {
        // First render the base tile
        renderTile(g, tile, x, y, isEntrance);
        
        // Then render item indicator
        if (useGraphics) {
            // Try to use graphics first - look for specific item graphics
            String itemAssetId = "floor_with_item"; // Generic item indicator
            BufferedImage img = assetManager.loadImage(itemAssetId);
            if (img != null) {
                Image drawImg = (img.getWidth() != tileSize || img.getHeight() != tileSize)
                                ? img.getScaledInstance(tileSize, tileSize, Image.SCALE_SMOOTH)
                                : img;
                g.drawImage(drawImg, x, y, null);
                return;
            }
        }
        
        // Fallback: Draw a small red rectangle to indicate item
        g.setColor(Color.RED);
        int itemSize = tileSize / 4;
        int itemX = x + (tileSize - itemSize) / 2;
        int itemY = y + (tileSize - itemSize) / 2;
        g.fillRect(itemX, itemY, itemSize, itemSize);
    }

    // ====== FOG OF WAR RENDERING ======
    public void renderFogOverlay(Graphics g, Tile tile, int screenX, int screenY,
                                 FogOfWar fogOfWar, int mapX, int mapY, boolean debugNoFog) {
        if (debugNoFog) return;
        Graphics2D g2 = (Graphics2D) g;

        switch (tile.getFogState()) {
            case UNDISCOVERED -> {
                g2.setColor(fogUndiscovered);
                g2.fillRect(screenX, screenY, tileSize, tileSize);
            }
            case DISCOVERED -> {
                Composite oldComp = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                g2.setColor(fogDiscovered);
                g2.fillRect(screenX, screenY, tileSize, tileSize);
                g2.setComposite(oldComp);
            }
            case VISIBLE -> {
                if (fogOfWar != null) {
                    float strength = fogOfWar.getVisibilityStrength(mapX, mapY);
                    if (strength < 1.0f) {
                        float fogAlpha = (1.0f - strength) * 0.6f;
                        if (fogAlpha > 0.05f) {
                            Composite oldComp = g2.getComposite();
                            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fogAlpha));
                            g2.setColor(fogEdge);
                            g2.fillRect(screenX, screenY, tileSize, tileSize);
                            g2.setComposite(oldComp);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Renders a warm torch-like glow across the entire visible area.
     * Creates a warm yellow/orange tint that's strongest near the player and fades with distance.
     */
    public void renderTorchGlow(Graphics g, int playerMapX, int playerMapY, Camera camera, 
                                int panelWidth, int panelHeight, FogOfWar fogOfWar) {
        if (fogOfWar == null) return;
        
        Graphics2D g2 = (Graphics2D) g;
        Composite oldComp = g2.getComposite();
        
        // Torch parameters - covers entire visible area
        final Color TORCH_COLOR = new Color(255, 180, 60); // Warm orange-yellow
        final float BASE_ALPHA = 0.08f; // Base warmth for all visible tiles
        final float MAX_BOOST = 0.20f; // Extra intensity near player
        final float BOOST_RADIUS = 5.0f; // tiles where boost applies
        
        // Get visible area bounds from camera
        int startX = Math.max(0, camera.getX());
        int startY = Math.max(0, camera.getY());
        int endX = camera.getX() + (panelWidth / tileSize) + 2;
        int endY = camera.getY() + (panelHeight / tileSize) + 2;
        
        for (int mapY = startY; mapY < endY; mapY++) {
            for (int mapX = startX; mapX < endX; mapX++) {
                // Only render on visible tiles
                if (!fogOfWar.isVisible(mapX, mapY)) continue;
                
                // Calculate distance from player for intensity boost
                float dx = mapX - playerMapX;
                float dy = mapY - playerMapY;
                float distance = (float)Math.sqrt(dx * dx + dy * dy);
                
                // Base warmth for all visible tiles, with boost near player
                float torchAlpha = BASE_ALPHA;
                if (distance <= BOOST_RADIUS) {
                    float intensity = 1.0f - (distance / BOOST_RADIUS);
                    intensity = (float)((1 - Math.cos(intensity * Math.PI)) / 2); // Cosine falloff for smooth glow
                    torchAlpha += intensity * MAX_BOOST;
                }
                
                // Scale by visibility strength (dimmer at fog edges)
                float visibilityStrength = fogOfWar.getVisibilityStrength(mapX, mapY);
                torchAlpha *= visibilityStrength;
                
                if (torchAlpha > 0.02f) {
                    int screenX = (mapX - camera.getX()) * tileSize;
                    int screenY = (mapY - camera.getY()) * tileSize;
                    
                    // Check if tile is on screen
                    if (screenX >= -tileSize && screenX < panelWidth && 
                        screenY >= -tileSize && screenY < panelHeight) {
                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, torchAlpha));
                        g2.setColor(TORCH_COLOR);
                        g2.fillRect(screenX, screenY, tileSize, tileSize);
                    }
                }
            }
        }
        
        g2.setComposite(oldComp);
    }

    public void renderTileWithFog(Graphics g, Tile tile, int screenX, int screenY, int mapX, int mapY,
                                  boolean isEntrance, FogOfWar fogOfWar, boolean debugNoFog) {
        // Check if tile has an item and render accordingly
        if (tile.hasItem()) {
            renderTileWithItem(g, tile, screenX, screenY, isEntrance);
        } else {
            renderTile(g, tile, screenX, screenY, isEntrance);
        }
        renderFogOverlay(g, tile, screenX, screenY, fogOfWar, mapX, mapY, debugNoFog);
    }

    // ====== BATCH RENDERING ======
    public void renderVisibleArea(Graphics g, GameMap gameMap, Camera camera,
                                  FogOfWar fogOfWar, boolean debugNoFog, int panelWidth, int panelHeight) {
        int startX = Math.max(0, camera.getX());
        int startY = Math.max(0, camera.getY());
        int endX = Math.min(gameMap.getWidth(), camera.getX() + (panelWidth / tileSize) + 2);
        int endY = Math.min(gameMap.getHeight(), camera.getY() + (panelHeight / tileSize) + 2);

        int entranceX = gameMap.getEntranceX();
        int entranceY = gameMap.getEntranceY();

        for (int mapY = startY; mapY < endY; mapY++) {
            for (int mapX = startX; mapX < endX; mapX++) {
                Tile tile = gameMap.getTile(mapX, mapY);
                if (tile == null) continue;

                int screenX = (mapX - camera.getX()) * tileSize;
                int screenY = (mapY - camera.getY()) * tileSize;
                boolean isEntrance = (mapX == entranceX && mapY == entranceY);

                renderTileWithFog(g, tile, screenX, screenY, mapX, mapY, isEntrance, fogOfWar, debugNoFog);
            }
        }
    }

    public void renderVisibleAreaFallback(Graphics g, GameMap gameMap, Camera camera,
                                          FogOfWar fogOfWar, boolean debugNoFog, int panelWidth, int panelHeight) {
        int startX = Math.max(0, camera.getX());
        int startY = Math.max(0, camera.getY());
        int endX = Math.min(gameMap.getWidth(), camera.getX() + (panelWidth / tileSize) + 2);
        int endY = Math.min(gameMap.getHeight(), camera.getY() + (panelHeight / tileSize) + 2);

        int entranceX = gameMap.getEntranceX();
        int entranceY = gameMap.getEntranceY();

        for (int mapY = startY; mapY < endY; mapY++) {
            for (int mapX = startX; mapX < endX; mapX++) {
                Tile tile = gameMap.getTile(mapX, mapY);
                if (tile == null) continue;

                int screenX = (mapX - camera.getX()) * tileSize;
                int screenY = (mapY - camera.getY()) * tileSize;
                boolean isEntrance = (mapX == entranceX && mapY == entranceY);

                g.setColor(getFallbackColor(tile, isEntrance));
                g.fillRect(screenX, screenY, tileSize, tileSize);
                
                // Draw item indicator if tile has an item
                if (tile.hasItem()) {
                    g.setColor(Color.RED);
                    int itemSize = tileSize / 4;
                    int itemX = screenX + (tileSize - itemSize) / 2;
                    int itemY = screenY + (tileSize - itemSize) / 2;
                    g.fillRect(itemX, itemY, itemSize, itemSize);
                }
                
                renderFogOverlay(g, tile, screenX, screenY, fogOfWar, mapX, mapY, debugNoFog);
            }
        }
    }

    // ====== GETTERS ======
    public int getTileSize() { return tileSize; }
    public boolean isGraphicsEnabled() { return useGraphics; }

    public String getRenderStats() {
        return String.format("TileRenderer: Graphics %s, Size %dpx, %s",
                useGraphics ? "ON" : "OFF", tileSize, assetManager.getCacheStats());
    }
}
