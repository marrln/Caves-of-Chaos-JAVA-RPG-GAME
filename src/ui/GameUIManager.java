package ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Manages the relationship between game UI panels and enforces proper boundaries.
 * This class decouples panels from each other while maintaining awareness of
 * the overall layout.
 */
public class GameUIManager {
    private final GamePanel gamePanel;
    private final StatusPanel statusPanel;
    private final LogPanel logPanel;
    private final int TILE_SIZE;
    
    // Visible game area in tiles
    private int visibleMapWidth;
    private int visibleMapHeight;
    
    public GameUIManager(GamePanel gamePanel, StatusPanel statusPanel, LogPanel logPanel) {
        this.gamePanel = gamePanel;
        this.statusPanel = statusPanel;
        this.logPanel = logPanel;
        this.TILE_SIZE = gamePanel.getTileSize();
        
        // Calculate initial visible area
        calculateVisibleArea();
        
        // Apply the visible area to the game panel
        gamePanel.setVisibleArea(visibleMapWidth, visibleMapHeight);
        
        // Force initial camera centering on player
        gamePanel.centerCameraOnPlayer();
        
        // Listen for component resize events
        setupResizeListeners();
    }
    
    /**
     * Calculates the visible game area in tiles, taking into account
     * the size of the log and status panels.
     */
    private void calculateVisibleArea() {
        // Get the current dimensions of the game panel
        Dimension gamePanelSize = gamePanel.getSize();
        
        // Calculate visible area in tiles
        visibleMapWidth = (gamePanelSize.width) / TILE_SIZE + 1;
        visibleMapHeight = (gamePanelSize.height) / TILE_SIZE + 1;
        
    }
    
    /**
     * Sets up listeners to recalculate visible area when components are resized.
     */
    private void setupResizeListeners() {
        ComponentAdapter resizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                calculateVisibleArea();
                gamePanel.setVisibleArea(visibleMapWidth, visibleMapHeight);
                
                // Force camera to recenter on player with new dimensions
                gamePanel.centerCameraOnPlayer();
            }
        };
        
        // Add listeners to all relevant components
        gamePanel.addComponentListener(resizeListener);
        
        // Also listen to the parent container
        Component parent = gamePanel.getParent();
        if (parent != null) {
            parent.addComponentListener(resizeListener);
        }
    }
    
    /**
     * Refreshes the status panel to update dynamic information like level display.
     */
    public void refreshStatusPanel() {
        statusPanel.repaint();
    }
    
    /**
     * Validates if a position is within the visible game area.
     * This checks if the position is within the camera's current view.
     * 
     * @param x The x coordinate to check
     * @param y The y coordinate to check
     * @return true if the position is within the camera's current view
     */
    public boolean isPositionVisible(int x, int y) {
    // Get camera position from game panel
        int cameraX = gamePanel.getCamera().getX();
        int cameraY = gamePanel.getCamera().getY();
        int viewWidth = gamePanel.getCamera().getViewWidth();
        int viewHeight = gamePanel.getCamera().getViewHeight();
        
        // Check if position is within camera view
        return x >= cameraX && x < cameraX + viewWidth && 
                y >= cameraY && y < cameraY + viewHeight;
    }
    
    /**
     * Returns the current visible map width in tiles.
     */
    public int getVisibleMapWidth() {
        return visibleMapWidth;
    }
    
    /**
     * Returns the current visible map height in tiles.
     */
    public int getVisibleMapHeight() {
        return visibleMapHeight;
    }
    
    /**
     * Forces the game panel to recenter the camera on the player.
     * This should be called after game initialization or level changes.
     */
    public void centerCameraOnPlayer() {
        gamePanel.centerCameraOnPlayer();
    }
    
    /**
     * Adds a message to the log panel.
     * 
     * @param message The message to display
     */
    public void addLogMessage(String message) {
        logPanel.addMessage(message);
    }
    
    /**
     * Gets the log panel for direct access.
     * 
     * @return The log panel
     */
    public LogPanel getLogPanel() {
        return logPanel;
    }
}
