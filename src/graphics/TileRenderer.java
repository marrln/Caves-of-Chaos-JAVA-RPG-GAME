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
    private final Color fogUndiscovered = StyleConfig.getColor("fogUndiscovered", new Color(0, 0, 0, 240));
    private final Color fogDiscovered   = StyleConfig.getColor("fogDiscovered", new Color(30, 40, 60));
    private final Color fogEdge         = StyleConfig.getColor("fogEdge", new Color(20, 30, 50));

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
        if (isEntrance) return "spawn_point";
        return switch (tile.getType()) {
            case Tile.WALL -> "wall";
            case Tile.FLOOR -> "floor";
            case Tile.STAIRS_DOWN -> "stairs_down";
            case Tile.STAIRS_UP -> "stairs_up";
            default -> null;
        };
    }

    private Color getFallbackColor(Tile tile, boolean isEntrance) {
        if (isEntrance) return entranceColor;
        return switch (tile.getType()) {
            case Tile.WALL -> wallColor;
            case Tile.FLOOR -> floorColor;
            case Tile.STAIRS_DOWN -> stairsDownColor;
            case Tile.STAIRS_UP -> stairsUpColor;
            default -> unknownColor;
        };
    }

    // ====== ITEM RENDERING ======
    public void renderTileWithItem(Graphics g, Tile tile, int x, int y) {
        if (useGraphics && assetManager.hasAsset("floor_with_item")) {
            BufferedImage img = assetManager.loadImage("floor_with_item");
            if (img != null) {
                Image drawImg = (img.getWidth() != tileSize || img.getHeight() != tileSize)
                                ? img.getScaledInstance(tileSize, tileSize, Image.SCALE_SMOOTH)
                                : img;
                g.drawImage(drawImg, x, y, null);
                return;
            }
        }
        g.setColor(floorColor);
        g.fillRect(x, y, tileSize, tileSize);
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
                        float fogAlpha = (1.0f - strength) * 0.3f;
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

    public void renderTileWithFog(Graphics g, Tile tile, int screenX, int screenY, int mapX, int mapY,
                                  boolean isEntrance, FogOfWar fogOfWar, boolean debugNoFog) {
        renderTile(g, tile, screenX, screenY, isEntrance);
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
