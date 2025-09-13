package ui;

import config.StyleConfig;
import core.GameController;
import core.GameState;
import graphics.EnemyRenderer;
import graphics.TileRenderer;
import input.GameInputHandler;
import java.awt.*;
import javax.swing.JPanel;
import javax.swing.Timer;
import map.FogOfWar;
import map.GameMap;
import player.AbstractPlayer;

public class GamePanel extends JPanel {
    private GameState gameState;
    private final GameController controller;
    private TileRenderer tileRenderer; // Remove final to allow lazy initialization
    private Camera camera;

    // Keep track of visible game area in tiles
    private int visibleMapWidth;
    private int visibleMapHeight;

    private GameUIManager uiManager;
    private GameInputHandler inputHandler;
    
    private boolean debugNoFog = false; // Debug flag to disable fog of war
    private int lastKnownLevel = -1;
    
    // Real-time game loop
    private Timer gameTimer;
    private static final int GAME_UPDATE_DELAY = 100; // Update every 100ms (10 FPS for game logic)

    public GamePanel(GameState gameState, GameController controller) {
        this.gameState = gameState;
        this.controller = controller;
        // Don't initialize TileRenderer here - wait for map to be fully generated
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
        setupGameLoop();
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
        
        // Ensure TileRenderer is initialized after map generation
        ensureTileRendererInitialized();
        
        // Check if level has changed and reinitialize camera if needed
        int currentLevel = gameState.getCurrentLevel();
        if (currentLevel != lastKnownLevel) {
            reinitializeCameraForNewLevel(player);
            uiManager.refreshStatusPanel();
            lastKnownLevel = currentLevel;
        }
        
        // Efficient batch rendering of all visible tiles and fog in one pass
        FogOfWar fogOfWar = gameState.getFogOfWar();
        
        // Initialize TileRenderer if needed
        if (tileRenderer == null) {
            tileRenderer = new TileRenderer();
        }
        
        // Use optimized batch rendering - handles both graphics and fallback internally
        tileRenderer.renderVisibleArea(g, map, camera, fogOfWar, debugNoFog, getWidth(), getHeight());

        // Render enemies (after tiles, before player) - only visible ones through fog of war
        Graphics2D g2d = (Graphics2D) g;
        int tileSize = getTileSize(); // Get tile size for enemy and player rendering
        EnemyRenderer.renderEnemies(g2d, gameState.getCurrentEnemies(), tileSize, 
                                   camera.getX() * tileSize, camera.getY() * tileSize, gameState.getFogOfWar());

        // Draw player
        int playerScreenX = (player.getX() - camera.getX()) * tileSize;
        int playerScreenY = (player.getY() - camera.getY()) * tileSize;
        
        // Normal player drawing
        g.setColor(StyleConfig.getColor("playerBody", Color.CYAN));
        g.fillOval(playerScreenX, playerScreenY, tileSize, tileSize);
        // Add a border to make it more visible
        g.setColor(StyleConfig.getColor("playerBorder", Color.WHITE));
        g.drawOval(playerScreenX, playerScreenY, tileSize, tileSize);

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
    
    /**
     * Ensures TileRenderer is initialized only when map generation is complete.
     * This prevents rendering issues during multi-pass map generation.
     */
    private void ensureTileRendererInitialized() {
        if (tileRenderer == null) {
            GameMap currentMap = gameState.getCurrentMap();
            
            // Only initialize if map is fully generated (has valid entrance/exit)
            if (currentMap != null && 
                currentMap.getEntranceX() >= 0 && currentMap.getEntranceY() >= 0 && 
                currentMap.getExitX() >= 0 && currentMap.getExitY() >= 0) {
                
                tileRenderer = new TileRenderer();
                System.out.println("TileRenderer initialized after map generation completed");
            }
        }
    }
    

    
    public int getTileSize() {
        // Return default tile size if TileRenderer isn't ready yet (during map generation)
        if (tileRenderer == null) {
            return 32; // Default fallback tile size
        }
        return tileRenderer.getTileSize();
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
    
    /**
     * Sets up the real-time game loop timer that continuously updates enemies.
     * This replaces the old turn-based system with smooth real-time gameplay.
     */
    private void setupGameLoop() {
        gameTimer = new Timer(GAME_UPDATE_DELAY, _ -> updateGameLogic());
        gameTimer.start();
    }
    
    /**
     * Updates the game logic in real-time.
     * Called periodically by the game timer to update enemies, animations, etc.
     */
    private void updateGameLogic() {
        if (gameState != null) {
            // Update enemies continuously in real-time
            gameState.updateEnemies();
            
            // Update camera (smooth following)
            AbstractPlayer player = gameState.getPlayer();
            if (player != null) {
                camera.centerOn(player.getX(), player.getY());
            }
            
            // Refresh status panel to show real-time HP/MP changes
            uiManager.refreshStatusPanel();
            
            // Trigger repaint to show updates
            repaint();
        }
    }
    
    /**
     * Stops the game loop timer. Should be called when disposing the panel.
     */
    public void stopGameLoop() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }
}
