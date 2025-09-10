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

    public Camera(int viewWidth, int viewHeight) {
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        this.smoothing = 0.2f;  // Default smoothing factor NOTE THIS NEEDS TO BE MOVED TO CONFIG
    }

    public void setMapSize(int mapWidth, int mapHeight) { // both in tiles
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    public void setViewSize(int viewWidth, int viewHeight) { // both in tiles
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

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

    public void snapToTarget(int targetX, int targetY) {
        x = targetX - viewWidth / 2;
        y = targetY - viewHeight / 2;
        
        // Constrain to map boundaries
        x = Math.max(0, Math.min(x, mapWidth - viewWidth));
        y = Math.max(0, Math.min(y, mapHeight - viewHeight));
    }

    public void snapToTarget() {
        // Constrain to map boundaries
        x = Math.max(0, Math.min(x, mapWidth - viewWidth));
        y = Math.max(0, Math.min(y, mapHeight - viewHeight));
    }

    public void setSmoothing(float smoothing) {
        this.smoothing = Math.max(0, Math.min(1, smoothing));
    }

    public float getSmoothing() {
        return smoothing;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public int getViewWidth() {
        return viewWidth;
    }
    
    public int getViewHeight() {
        return viewHeight;
    }
}
