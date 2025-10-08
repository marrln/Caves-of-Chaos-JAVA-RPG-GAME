package utils;

import java.util.ArrayList;
import java.util.List;
import map.GameMap;

/**
 * Manages entity and tile collision detection.
 * Handles movement validation and entity queries.
 */
public class CollisionSystem {

    private final GameMap gameMap;
    private List<? extends Positionable> entities;

    public CollisionSystem(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public void updateEntities(List<? extends Positionable> entities) {
        this.entities = entities;
    }

    // ===== TILE COLLISION =====
    
    public boolean isWallBlocked(int x, int y) {
        if (gameMap == null) return false;
        if (x < 0 || y < 0 || x >= gameMap.getWidth() || y >= gameMap.getHeight()) return true;
        var tile = gameMap.getTile(x, y);
        return tile != null && tile.isBlocked();
    }

    // ===== ENTITY COLLISION =====
    
    public boolean isEntityOccupied(int x, int y, Positionable exclude) {
        if (entities == null) return false;
        for (Positionable e : entities) {
            if (e != exclude && e.isSolid() && e.getX() == x && e.getY() == y) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isEntityOccupied(int x, int y) {
        return isEntityOccupied(x, y, null);
    }

    // ===== COMBINED CHECKS =====
    
    public boolean isAccessible(int x, int y) {
        return !isWallBlocked(x, y) && !isEntityOccupied(x, y);
    }
    
    public boolean canMoveTo(Positionable entity, int newX, int newY) {
        return !isWallBlocked(newX, newY) && !isEntityOccupied(newX, newY, entity);
    }

    // ===== ENTITY QUERIES =====
    
    public Positionable getEntityAt(int x, int y) {
        if (entities == null) return null;
        for (Positionable e : entities) {
            if (e.getX() == x && e.getY() == y) return e;
        }
        return null;
    }
    
    public List<Positionable> getEntitiesInRadius(int cx, int cy, double radius) {
        List<Positionable> result = new ArrayList<>();
        if (entities == null) return result;

        for (Positionable e : entities) {
            double dist = GeometryHelpers.getEuclideanDistance(cx, cy, e.getX(), e.getY());
            if (dist <= radius) {
                result.add(e);
            }
        }
        return result;
    }

    // ===== MOVEMENT HELPERS =====
    
    public boolean tryMoveEntityInDirection(Positionable entity, int direction) {
        GeometryHelpers.Position offset = GeometryHelpers.getDirectionOffset(direction);
        if (offset.x == 0 && offset.y == 0) return false;

        int newX = entity.getX() + offset.x;
        int newY = entity.getY() + offset.y;
        boolean moved = canMoveTo(entity, newX, newY);
        if (moved) {
            entity.setFacingDirection(direction);
        }
        return moved;
    }
}
