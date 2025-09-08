package ui;

import core.Config;
import core.GameController;
import core.GameMap;
import core.GameState;
import core.Tile;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import player.AbstractPlayer;

/**
 * Panel responsible for rendering the game map and handling player input.
 */
public class GamePanel extends JPanel {
    private GameState gameState;
    private final GameController controller;
    private static final int TILE_SIZE = 32;
    private Camera camera;
    private int viewWidth;
    private int viewHeight;
    private int visionRadius;
    // Keep track of visible game area in tiles
    private int visibleMapWidth;
    private int visibleMapHeight;
    // Flag for sprite rendering
    private boolean useSprites = false;
    // Reference to the GameUIManager
    private GameUIManager uiManager;

    /**
     * Creates a new GamePanel with the given game state and controller.
     *
     * @param gameState The current game state
     * @param controller The game controller for processing game actions
     */
    public GamePanel(GameState gameState, GameController controller) {
        this.gameState = gameState;
        this.controller = controller;
        setBackground(Color.BLACK);
        setFocusable(true);
        
        // Default visible area - will be updated by the GameUIManager
        this.visibleMapWidth = 20;
        this.visibleMapHeight = 15;
        
        // Set preferred size from settings.xml if available
        int panelW = -1, panelH = -1;
        try {
            String wStr = Config.getSetting("gamePanelWidth");
            String hStr = Config.getSetting("gamePanelHeight");
            if (wStr != null) panelW = Integer.parseInt(wStr.trim());
            if (hStr != null) panelH = Integer.parseInt(hStr.trim());
        } catch (NumberFormatException | NullPointerException e) {
            // Ignore errors and use default panel size
        }
        if (panelW > 0 && panelH > 0) {
            setPreferredSize(new Dimension(panelW, panelH));
        }

        // Always read view size from config, but clamp to map size if map is smaller
        viewWidth = 20;
        viewHeight = 15;
        visionRadius = 5;
        try {
            String widthStr = Config.getSetting("defaultMapView");
            if (widthStr != null && widthStr.contains("width")) {
                int wIdx = widthStr.indexOf("width=\"");
                int hIdx = widthStr.indexOf("height=\"");
                if (wIdx != -1 && hIdx != -1) {
                    int wEnd = widthStr.indexOf('"', wIdx + 7 + 1);
                    int hEnd = widthStr.indexOf('"', hIdx + 8 + 1);
                    viewWidth = Integer.parseInt(widthStr.substring(wIdx + 7, wEnd));
                    viewHeight = Integer.parseInt(widthStr.substring(hIdx + 8, hEnd));
                }
            }
            String vrStr = Config.getSetting("visionRadius");
            if (vrStr != null) visionRadius = Integer.parseInt(vrStr.trim());
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            // Ignore parsing errors and use default values
        }

        // Initialize camera with the view size
        GameMap map = gameState.getCurrentMap();
        AbstractPlayer player = gameState.getPlayer();
        camera = new Camera(viewWidth, viewHeight);
        camera.setMapSize(map.getWidth(), map.getHeight());
        
        // Center camera on player
        camera.centerOn(player.getX(), player.getY());
        camera.snapToTarget();
        
        // Ensure player is within valid map bounds
        int px = player.getX();
        int py = player.getY();
        int minX = 0;
        int maxX = map.getWidth() - 1;
        int minY = 0;
        int maxY = map.getHeight() - 1;
        
        if (px < minX || py < minY || px > maxX || py > maxY) {
            px = Math.max(minX, Math.min(maxX, px));
            py = Math.max(minY, Math.min(maxY, py));
            player.setPosition(px, py);
        }

        // Setup key listener for player movement and actions
        setupKeyListener();
    }
    
    /**
     * Sets up the key listener for player movement and actions.
     */
    private void setupKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int dx = 0, dy = 0;
                boolean shouldUpdate = false;
                
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W, KeyEvent.VK_UP -> { 
                        dy = -1; 
                    }
                    case KeyEvent.VK_S, KeyEvent.VK_DOWN -> { 
                        dy = 1; 
                    }
                    case KeyEvent.VK_A, KeyEvent.VK_LEFT -> { 
                        dx = -1; 
                    }
                    case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> { 
                        dx = 1; 
                    }
                    case KeyEvent.VK_R -> { 
                        controller.rest(); 
                        shouldUpdate = true; 
                    }
                    case KeyEvent.VK_E -> { 
                        controller.attack(1); 
                        shouldUpdate = true; 
                    }
                    case KeyEvent.VK_Q -> { 
                        controller.attack(2); 
                        shouldUpdate = true; 
                    }
                    case KeyEvent.VK_1 -> { 
                        controller.useItem(1); 
                        shouldUpdate = true; 
                    }
                    case KeyEvent.VK_2 -> { 
                        controller.useItem(2); 
                        shouldUpdate = true; 
                    }
                    case KeyEvent.VK_3 -> { 
                        controller.useItem(3); 
                        shouldUpdate = true; 
                    }
                    case KeyEvent.VK_4 -> { 
                        controller.useItem(4); 
                        shouldUpdate = true; 
                    }
                    case KeyEvent.VK_5 -> { 
                        controller.useItem(5); 
                        shouldUpdate = true; 
                    }
                    case KeyEvent.VK_6 -> { 
                        controller.useItem(6); 
                        shouldUpdate = true; 
                    }
                    case KeyEvent.VK_7 -> { 
                        controller.useItem(7); 
                        shouldUpdate = true; 
                    }
                    case KeyEvent.VK_8 -> { 
                        controller.useItem(8); 
                        shouldUpdate = true; 
                    }
                    case KeyEvent.VK_9 -> { 
                        controller.useItem(9); 
                        shouldUpdate = true; 
                    }
                    default -> { return; }
                }
                
                // If movement keys were pressed
                if (dx != 0 || dy != 0) {
                    // Get the player's intended position
                    AbstractPlayer player = gameState.getPlayer();
                    int newX = player.getX() + dx;
                    int newY = player.getY() + dy;
                    
                    // Check if the position is within the visible UI boundaries
                    boolean isVisible = (uiManager == null) || 
                                        uiManager.isPositionVisible(newX, newY);
                    
                    // Only try to move if within visible area
                    if (isVisible) {
                        shouldUpdate = controller.movePlayer(dx, dy);
                    }
                }
                
                if (shouldUpdate) {
                    // Update camera to follow player
                    AbstractPlayer player = gameState.getPlayer();
                    GameMap map = gameState.getCurrentMap();
                    
                    camera.setMapSize(map.getWidth(), map.getHeight());
                    camera.centerOn(player.getX(), player.getY());
                    
                    // Refresh the view
                    repaint();
                }
            }
        });
    }
    
    /**
     * Sets the UI manager for this panel.
     * This should be called after all panels are created.
     * 
     * @param uiManager The UI manager to use
     */
    public void setUIManager(GameUIManager uiManager) {
        this.uiManager = uiManager;
    }

    /**
     * Sets the visible map area in tiles.
     * This is called by the GameUIManager when the window is resized.
     * 
     * @param width The visible width in tiles
     * @param height The visible height in tiles
     */
    public void setVisibleArea(int width, int height) {
        this.visibleMapWidth = width;
        this.visibleMapHeight = height;
        
        // Update the camera view size to match the visible area
        if (camera != null) {
            camera.setViewSize(width, height);
        }
    }
    
    /**
     * Returns the tile size used for rendering.
     * 
     * @return The tile size in pixels
     */
    public int getTileSize() {
        return TILE_SIZE;
    }
    
    /**
     * Returns the current visible map width in tiles.
     * 
     * @return The visible map width
     */
    public int getVisibleMapWidth() {
        return visibleMapWidth;
    }
    
    /**
     * Returns the current visible map height in tiles.
     * 
     * @return The visible map height
     */
    public int getVisibleMapHeight() {
        return visibleMapHeight;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Don't render if game state is not initialized
        if (gameState == null) return;
        
        AbstractPlayer player = gameState.getPlayer();
        GameMap map = gameState.getCurrentMap();
        int mapW = map.getWidth();
        int mapH = map.getHeight();
        
        // Render tiles in the viewport
        for (int vx = 0; vx < camera.getViewWidth(); vx++) {
            for (int vy = 0; vy < camera.getViewHeight(); vy++) {
                int mapX = vx + camera.getX();
                int mapY = vy + camera.getY();
                int screenX = vx * TILE_SIZE;
                int screenY = vy * TILE_SIZE;
                
                // Check if this tile would be under a UI panel
                boolean isUnderUIPanel = (uiManager != null && !uiManager.isPositionVisible(mapX, mapY));
                
                // Handle out of bounds tiles or tiles under UI panels
                if (mapX < 0 || mapY < 0 || mapX >= mapW || mapY >= mapH || isUnderUIPanel) {
                    g.setColor(Color.BLACK);
                    g.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                    continue;
                }
                
                Tile tile = map.getTile(mapX, mapY);
                
                // Draw tile based on fog state
                switch (tile.getFogState()) {
                    case UNDISCOVERED -> {
                        // Make undiscovered tiles dark navy blue instead of pure black
                        // for a more subtle fog effect
                        g.setColor(new Color(15, 15, 30));
                        g.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                    }
                    case DISCOVERED, VISIBLE -> {
                        if (mapX == map.getEntranceX() && mapY == map.getEntranceY()) {
                            g.setColor(Color.PINK);
                        } else {
                            switch (tile.getType()) {
                                case Tile.WALL -> g.setColor(Color.DARK_GRAY);
                                case Tile.FLOOR -> g.setColor(Color.LIGHT_GRAY);
                                case Tile.STAIRS_DOWN -> g.setColor(Color.YELLOW);
                                case Tile.STAIRS_UP -> g.setColor(Color.ORANGE);
                            }
                        }
                        g.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                        
                        // Apply fog effect based on distance from player
                        if (tile.getFogState() == Tile.FogState.DISCOVERED) {
                            // Apply a more subtle fog overlay
                            Graphics2D g2 = (Graphics2D) g;
                            Composite oldComp = g2.getComposite();
                            
                            // More subtle fog - 0.3f opacity for discovered tiles
                            float alpha = 0.3f;
                            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                            g2.setColor(new Color(10, 10, 40)); // Dark blue tint
                            g2.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                            g2.setComposite(oldComp);
                        } else if (tile.getFogState() == Tile.FogState.VISIBLE) {
                            // For visible tiles near the edge of vision, add a subtle fog gradient
                            int playerMapX = player.getX();
                            int playerMapY = player.getY();
                            double distance = Math.sqrt(Math.pow(mapX - playerMapX, 2) + Math.pow(mapY - playerMapY, 2));
                            
                            // Calculate normalized distance (0 = player, 1 = edge of vision)
                            double normalizedDistance = distance / visionRadius;
                            
                            // Only apply fog effect at the edges of vision (normalizedDistance > 0.75)
                            if (normalizedDistance > 0.75) {
                                // Calculate fog opacity based on distance (more distant = more fog)
                                float fogOpacity = (float)((normalizedDistance - 0.75) / 0.25) * 0.2f;
                                
                                // Apply subtle fog at the edges of vision
                                Graphics2D g2 = (Graphics2D) g;
                                Composite oldComp = g2.getComposite();
                                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fogOpacity));
                                g2.setColor(new Color(10, 10, 40)); // Dark blue tint
                                g2.fillRect(screenX, screenY, TILE_SIZE, TILE_SIZE);
                                g2.setComposite(oldComp);
                            }
                        }
                    }
                }
            }
        }

        // Draw a visual separator line for UI panels if needed
        if (uiManager != null) {
            g.setColor(Color.DARK_GRAY);
            int panelStartY = getHeight() - ((mapH - uiManager.getVisibleMapHeight()) * TILE_SIZE);
            g.fillRect(0, panelStartY - 2, getWidth(), 2);
        }

        // Draw player
        int playerViewX = player.getX() - camera.getX();
        int playerViewY = player.getY() - camera.getY();
        
        // Only draw player if within view area and not under UI panels
        boolean playerVisible = playerViewX >= 0 && playerViewX < camera.getViewWidth() && 
                               playerViewY >= 0 && playerViewY < camera.getViewHeight();
                               
        if (uiManager != null) {
            playerVisible = playerVisible && uiManager.isPositionVisible(player.getX(), player.getY());
        }
        
        if (playerVisible) {
            int playerScreenX = playerViewX * TILE_SIZE;
            int playerScreenY = playerViewY * TILE_SIZE;
            
            if (!useSprites) {
                // Fallback: Draw player as cyan circle
                g.setColor(Color.CYAN);
                g.fillOval(playerScreenX, playerScreenY, TILE_SIZE, TILE_SIZE);
                // TODO: Draw enemies as red circles, items as green squares, etc.
            } else {
                // TODO: Sprite-based rendering for player, enemies, items
            }
        }
    }
}
