package ui;

import config.StyleConfig;
import core.GameController;
import core.GameState;
import input.GameInputHandler;
import map.FogOfWar;
import map.GameMap;
import map.Tile;

import java.awt.*;
import javax.swing.JPanel;
import player.AbstractPlayer;

public class GamePanel extends JPanel {
    private GameState gameState;
    private final GameController controller;
    private static final int TILE_SIZE = 32; // ERROR THIS SHOULD BE LOADED FROM CONFIG
    private Camera camera;

    // Keep track of visible game area in tiles
    private int visibleMapWidth;
    private int visibleMapHeight;

    private GameUIManager uiManager;
    private GameInputHandler inputHandler;
    
    private boolean debugNoFog = false; // Debug flag to disable fog of war
    private int lastKnownLevel = -1;

    public GamePanel(GameState gameState, GameController controller) {
        this.gameState = gameState;
        this.controller = controller;
        setBackground(StyleConfig.getColor("gameBackground", Color.BLACK));
        setFocusable(true);
        
        // Default visible area - will be updated by the GameUIManager
        this.visibleMapWidth = 20;
        this.visibleMapHeight = 15;
        
        // Initialize camera with default size - GameUIManager will set proper size via setVisibleArea()
        GameMap map = gameState.getCurrentMap();
        AbstractPlayer player = gameState.getPlayer();

        camera = new Camera(visibleMapWidth, visibleMapHeight);
        camera.setMapSize(map.getWidth(), map.getHeight());
        
        camera.centerOn(player.getX(), player.getY());
        camera.snapToTarget(player.getX(), player.getY());

        setupInputHandler();
    }
    
    
    /**
     * Forces the camera to center on the player and refresh the display.
     * This should be called after map generation, level changes, or when
     * the view becomes inconsistent.
     */
    public void centerCameraOnPlayer() {
        if (camera != null && gameState != null) {
            AbstractPlayer player = gameState.getPlayer();
            GameMap map = gameState.getCurrentMap();
            
            if (player != null && map != null) {
                // Temporarily disable smoothing for instant centering
                float originalSmoothing = camera.getSmoothing();
                camera.setSmoothing(0.0f);
                
                // Update camera map size and center on player
                camera.setMapSize(map.getWidth(), map.getHeight());
                camera.centerOn(player.getX(), player.getY());
                camera.snapToTarget(player.getX(), player.getY());
                
                // Restore original smoothing
                camera.setSmoothing(originalSmoothing);
                
                // Force a repaint to show the updated view
                repaint();
            }
        }
    }
    
    /**
     * Toggles the fog of war debug mode and refreshes the display.
     * When disabled, all tiles are visible regardless of fog state.
     */
    public void toggleFogOfWar() {
        debugNoFog = !debugNoFog;
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        AbstractPlayer player = gameState.getPlayer();
        GameMap map = gameState.getCurrentMap();
        
        // Check if level has changed and reinitialize camera if needed
        int currentLevel = gameState.getCurrentLevel();
        if (currentLevel != lastKnownLevel) {
            reinitializeCameraForNewLevel(player);
            uiManager.refreshStatusPanel();
            lastKnownLevel = currentLevel;
        }
        
        // Render tiles in the viewport
        for (int vx = 0; vx < camera.getViewWidth(); vx++) {
            for (int vy = 0; vy < camera.getViewHeight(); vy++) {
                int mapX = vx + camera.getX();
                int mapY = vy + camera.getY();
                int screenX = vx * TILE_SIZE;
                int screenY = vy * TILE_SIZE;
                
                Tile tile = map.getTile(mapX, mapY);
                
                if (mapX == map.getEntranceX() && mapY == map.getEntranceY()) {
                    g.setColor(StyleConfig.getColor("tileEntrance", Color.PINK));
                } else {
                    switch (tile.getType()) {
                        case Tile.WALL -> g.setColor(StyleConfig.getColor("tileWall", Color.DARK_GRAY));
                        case Tile.FLOOR -> g.setColor(StyleConfig.getColor("tileFloor", Color.LIGHT_GRAY));
                        case Tile.STAIRS_DOWN -> g.setColor(StyleConfig.getColor("tileStairsDown", Color.YELLOW));
                        case Tile.STAIRS_UP -> g.setColor(StyleConfig.getColor("tileStairsUp", Color.ORANGE));
                        default -> g.setColor(StyleConfig.getColor("tileUnknown", Color.MAGENTA)); // Unknown tile type
                    }
                }
                g.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                
                // Apply fog effects with smooth gradient only if debug mode is off
                if (!debugNoFog) {
                    FogOfWar fogOfWar = gameState.getFogOfWar();
                    Graphics2D g2 = (Graphics2D) g;
                    
                    switch (tile.getFogState()) {
                        case UNDISCOVERED -> {
                            // Completely dark for undiscovered tiles
                            g2.setColor(StyleConfig.getColor("fogUndiscovered", new Color(0, 0, 0, 240)));
                            g2.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                        }
                        case DISCOVERED -> {
                            // Nice blue-grey fog for discovered but not visible tiles
                            Composite oldComp = g2.getComposite();
                            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                            g2.setColor(StyleConfig.getColor("fogDiscovered", new Color(30, 40, 60)));
                            g2.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                            g2.setComposite(oldComp);
                        }
                        case VISIBLE -> {
                            // Apply gradient overlay based on distance for smooth edges
                            if (fogOfWar != null) {
                                float strength = fogOfWar.getVisibilityStrength(mapX, mapY);
                                if (strength < 1.0f) {
                                    // Create subtle gradient at edges of vision
                                    float fogAlpha = (1.0f - strength) * 0.3f;
                                    if (fogAlpha > 0.05f) { // Only apply if noticeable
                                        Composite oldComp = g2.getComposite();
                                        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fogAlpha));
                                        g2.setColor(StyleConfig.getColor("fogEdge", new Color(20, 30, 50)));
                                        g2.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                                        g2.setComposite(oldComp);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Draw player
        int playerScreenX = (player.getX() - camera.getX()) * TILE_SIZE;
        int playerScreenY = (player.getY() - camera.getY()) * TILE_SIZE;
        
        // Normal player drawing
        g.setColor(StyleConfig.getColor("playerBody", Color.CYAN));
        g.fillOval(playerScreenX, playerScreenY, TILE_SIZE, TILE_SIZE);
        // Add a border to make it more visible
        g.setColor(StyleConfig.getColor("playerBorder", Color.WHITE));
        g.drawOval(playerScreenX, playerScreenY, TILE_SIZE, TILE_SIZE);

    }
    
    // Reinitializes the camera for a new level
    private void reinitializeCameraForNewLevel(AbstractPlayer player) {
        camera.centerOn(player.getX(), player.getY());
        camera.snapToTarget(player.getX(), player.getY());
        repaint();
    }

    
    private void setupInputHandler() {
        inputHandler = new GameInputHandler(gameState, controller, this);
        addKeyListener(inputHandler);
    }
    

    public void setUIManager(GameUIManager uiManager) {
        this.uiManager = uiManager;
        // Also pass the UI manager to the input handler for boundary checking
        inputHandler.setUIManager(uiManager);
    }

    public void setVisibleArea(int width, int height) {
        this.visibleMapWidth = width;
        this.visibleMapHeight = height;
        
        camera.setViewSize(width, height);
        AbstractPlayer player = gameState.getPlayer();
        camera.snapToTarget(player.getX(), player.getY());
    }
    
    public int getTileSize() {
        return TILE_SIZE;
    }
    
    public Camera getCamera() {
        return camera;
    }
    
    public int getVisibleMapWidth() {
        return visibleMapWidth;
    }
    
    public int getVisibleMapHeight() {
        return visibleMapHeight;
    }
}
