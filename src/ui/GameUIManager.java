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
        if (gamePanelSize.width == 0 || gamePanelSize.height == 0) {
            // Not yet visible, use preferred size
            gamePanelSize = gamePanel.getPreferredSize();
        }
        
        // Get the dimensions of the log and status panels
        Dimension logPanelSize = logPanel.getPreferredSize();
        Dimension statusPanelSize = statusPanel.getPreferredSize();
        
        // Calculate visible area in tiles
        visibleMapWidth = (gamePanelSize.width - statusPanelSize.width) / TILE_SIZE;
        visibleMapHeight = (gamePanelSize.height - logPanelSize.height) / TILE_SIZE;
        
        // Ensure we have at least some visible area
        visibleMapWidth = Math.max(5, visibleMapWidth);
        visibleMapHeight = Math.max(5, visibleMapHeight);
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
                gamePanel.repaint();
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
     * Validates if a position is within the visible game area.
     * 
     * @param x The x coordinate to check
     * @param y The y coordinate to check
     * @return true if the position is within the visible area
     */
    public boolean isPositionVisible(int x, int y) {
        return x >= 0 && x < visibleMapWidth && y >= 0 && y < visibleMapHeight;
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
}
