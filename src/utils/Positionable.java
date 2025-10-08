package utils;

/**
 * Base interface for entities with position and facing direction.
 * Provides access to position and direction for collision/pathfinding systems.
 */
public interface Positionable {
    int getX();
    int getY();
    int getFacingDirection();  // 0=N, 1=E, 2=S, 3=W
    void setFacingDirection(int dir);
    
    // Collision properties
    default int getCollisionRadius() { return 0; }
    default boolean isSolid() { return true; }
}
