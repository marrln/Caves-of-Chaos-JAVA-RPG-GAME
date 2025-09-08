package ui;

/**
 * Camera class to handle viewport scrolling.
 * The camera follows the player and maintains a view onto the map.
 */
public class Camera {
    private int x, y;           // Top-left corner of the camera's view (in tiles)
    private int viewWidth;      // View width in tiles
    private int viewHeight;     // View height in tiles
    private int mapWidth;       // Map width in tiles
    private int mapHeight;      // Map height in tiles
    private float smoothing;    // Camera smoothing factor (0-1, 0 = instant, 1 = no movement)
    
    /**
     * Creates a new camera with the specified view dimensions.
     * 
     * @param viewWidth The width of the view in tiles
     * @param viewHeight The height of the view in tiles
     */
    public Camera(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.smoothing = 0.2f;  // Default smoothing factor
    }
    
    /**
     * Sets the map dimensions to constrain the camera.
     * 
     * @param mapWidth The map width in tiles
     * @param mapHeight The map height in tiles
     */
    public void setMapSize(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }
    
    /**
     * Sets the view size in tiles.
     * 
     * @param viewWidth The width of the view in tiles
     * @param viewHeight The height of the view in tiles
     */
    public void setViewSize(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }
    
    /**
     * Gradually moves the camera to center on the target position.
     * Uses smoothing for a more natural camera movement.
     * 
     * @param targetX The target x position in tiles
     * @param targetY The target y position in tiles
     */
    public void centerOn(int targetX, int targetY) {
        // Calculate desired camera position (centered on target)
        int desiredX = targetX - viewWidth / 2;
        int desiredY = targetY - viewHeight / 2;
        
        // Apply smoothing
        if (smoothing > 0) {
            x += (desiredX - x) * (1 - smoothing);
            y += (desiredY - y) * (1 - smoothing);
        } else {
            x = desiredX;
            y = desiredY;
        }
        
        // Constrain to map boundaries
        x = Math.max(0, Math.min(x, mapWidth - viewWidth));
        y = Math.max(0, Math.min(y, mapHeight - viewHeight));
    }
    
    /**
     * Immediately centers the camera on the target without smoothing.
     * 
     * @param targetX The target x position in tiles
     * @param targetY The target y position in tiles
     */
    public void snapToTarget(int targetX, int targetY) {
        x = targetX - viewWidth / 2;
        y = targetY - viewHeight / 2;
        
        // Constrain to map boundaries
        x = Math.max(0, Math.min(x, mapWidth - viewWidth));
        y = Math.max(0, Math.min(y, mapHeight - viewHeight));
    }
    
    /**
     * Immediately snaps the camera to its current target position.
     * Useful after loading a new level or teleporting.
     */
    public void snapToTarget() {
        // Constrain to map boundaries
        x = Math.max(0, Math.min(x, mapWidth - viewWidth));
        y = Math.max(0, Math.min(y, mapHeight - viewHeight));
    }
    
    /**
     * Sets the camera smoothing factor.
     * 0 = instant movement, 1 = no movement, 0.2-0.3 is a good default.
     * 
     * @param smoothing The smoothing factor (0-1)
     */
    public void setSmoothing(float smoothing) {
        this.smoothing = Math.max(0, Math.min(1, smoothing));
    }
    
    /**
     * Gets the camera's current x position.
     * 
     * @return The camera x position (in tiles)
     */
    public int getX() {
        return x;
    }
    
    /**
     * Gets the camera's current y position.
     * 
     * @return The camera y position (in tiles)
     */
    public int getY() {
        return y;
    }
    
    /**
     * Gets the camera's view width.
     * 
     * @return The view width (in tiles)
     */
    public int getViewWidth() {
        return viewWidth;
    }
    
    /**
     * Gets the camera's view height.
     * 
     * @return The view height (in tiles)
     */
    public int getViewHeight() {
        return viewHeight;
    }
}
