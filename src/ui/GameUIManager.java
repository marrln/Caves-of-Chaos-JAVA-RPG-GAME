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
        
        calculateVisibleArea();
        gamePanel.setVisibleArea(visibleMapWidth, visibleMapHeight);  // Apply the visible area to the game panel     
        gamePanel.centerCameraOnPlayer();                             // Force initial camera centering on player
        setupResizeListeners();                                       // Listen for component resize events
    }

    private void calculateVisibleArea() {
        Dimension gamePanelSize = gamePanel.getSize();
        
        // Calculate visible area in tiles
        visibleMapWidth = (gamePanelSize.width) / TILE_SIZE + 1;
        visibleMapHeight = (gamePanelSize.height) / TILE_SIZE + 1;
        
    }

    private void setupResizeListeners() {
        ComponentAdapter resizeListener = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                calculateVisibleArea();
                gamePanel.setVisibleArea(visibleMapWidth, visibleMapHeight);
                gamePanel.centerCameraOnPlayer(); // Force camera to recenter on player with new dimensions
            }
        };

        gamePanel.addComponentListener(resizeListener);
        Component parent = gamePanel.getParent();
        if (parent != null) {
            parent.addComponentListener(resizeListener);
        }
    }
    
    public void refreshStatusPanel() { statusPanel.refresh(); }

    public boolean isPositionVisible(int x, int y) {
    // Get camera position from game panel
        int cameraX = gamePanel.getCamera().getX();
        int cameraY = gamePanel.getCamera().getY();
        int viewWidth = gamePanel.getCamera().getViewWidth();
        int viewHeight = gamePanel.getCamera().getViewHeight();
        
        // Check if position is within camera view
        return x >= cameraX && x < cameraX + viewWidth && y >= cameraY && y < cameraY + viewHeight;
    }
    
    public int getVisibleMapWidth() { return visibleMapWidth; }
    public int getVisibleMapHeight() { return visibleMapHeight; }
    public void centerCameraOnPlayer() { gamePanel.centerCameraOnPlayer(); }
    public void addMessage(String message) { logPanel.addMessage(message); }
    public LogPanel getLogPanel() { return logPanel; }
}
