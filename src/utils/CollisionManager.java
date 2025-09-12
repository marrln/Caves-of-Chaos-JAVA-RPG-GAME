package utils;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import map.GameMap;

public class CollisionManager {

    private final GameMap gameMap;
    private List<? extends Positionable> entities;

    public interface Positionable {
        int getX();
        int getY();
        default int getCollisionRadius() { return 0; }
        default boolean isSolid() { return true; }
    }

    public CollisionManager(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public void updateEntities(List<? extends Positionable> entities) {
        this.entities = entities;
    }

    public boolean isWallBlocked(int x, int y) {
        if (gameMap == null) return false;
        if (x < 0 || y < 0 || x >= gameMap.getWidth() || y >= gameMap.getHeight()) return true;
        var tile = gameMap.getTile(x, y);
        return tile != null && tile.isBlocked();
    }

    public boolean isEntityOccupied(int x, int y, Positionable exclude) {
        if (entities == null) return false;
        return entities.stream()
            .filter(e -> e != exclude && e.isSolid())
            .anyMatch(e -> e.getX() == x && e.getY() == y);
    }

    public boolean isEntityOccupied(int x, int y) {
        return isEntityOccupied(x, y, null);
    }

    public boolean canMoveTo(Positionable entity, int newX, int newY) {
        return !isWallBlocked(newX, newY) && !isEntityOccupied(newX, newY, entity);
    }

    public boolean isAccessible(int x, int y) {
        return !isWallBlocked(x, y) && !isEntityOccupied(x, y);
    }

    public Positionable getEntityAt(int x, int y, Predicate<Positionable> filter) {
        if (entities == null) return null;
        return entities.stream()
            .filter(e -> e.getX() == x && e.getY() == y)
            .filter(filter != null ? filter : e -> true)
            .findFirst()
            .orElse(null);
    }

    public Positionable getEntityAt(int x, int y) {
        return getEntityAt(x, y, null);
    }

    public List<Positionable> getEntitiesInRadius(int cx, int cy, int radius, Predicate<Positionable> filter) {
        if (entities == null) return List.of();
        var stream = entities.stream()
            .filter(e -> {
                int dx = e.getX() - cx, dy = e.getY() - cy;
                return Math.sqrt(dx * dx + dy * dy) <= radius;
            });
        if (filter != null) stream = stream.filter(filter);
        return stream.collect(Collectors.toList());
    }

    public boolean hasLineOfSight(int x1, int y1, int x2, int y2, boolean checkEntities) {
        return LineUtils.hasLineOfSight(x1, y1, x2, y2, (x, y) ->
            isWallBlocked(x, y) || (checkEntities && isEntityOccupied(x, y))
        );
    }

    public List<Position> getProjectilePath(int x1, int y1, int x2, int y2, boolean piercing) {
        List<LineUtils.Point> linePoints = LineUtils.getProjectilePath(x1, y1, x2, y2, (x, y) ->
            isWallBlocked(x, y) || (!piercing && isEntityOccupied(x, y))
        );
        
        // Convert LineUtils.Point to CollisionManager.Position
        return linePoints.stream()
            .map(p -> new Position(p.x, p.y))
            .collect(Collectors.toList());
    }

    // ========== COMMON MOVEMENT UTILITIES ==========
    
    /**
     * Converts a direction integer to coordinate offset.
     * 0=North, 1=East, 2=South, 3=West
     * 
     * @param direction Direction code (0-3)
     * @return Position offset, or (0,0) for invalid direction
     */
    public static Position getDirectionOffset(int direction) {
        return switch (direction) {
            case 0 -> new Position(0, -1); // North
            case 1 -> new Position(1, 0);  // East  
            case 2 -> new Position(0, 1);  // South
            case 3 -> new Position(-1, 0); // West
            default -> new Position(0, 0); // Invalid/No movement
        };
    }
    
    /**
     * Attempts to move an entity in a specific direction with collision checking.
     * 
     * @param entity The entity to move
     * @param direction Direction code (0=N, 1=E, 2=S, 3=W)
     * @return true if movement was successful, false if blocked
     */
    public boolean tryMoveEntityInDirection(Positionable entity, int direction) {
        Position offset = getDirectionOffset(direction);
        if (offset.x == 0 && offset.y == 0) {
            return false; // Invalid direction
        }
        
        int newX = entity.getX() + offset.x;
        int newY = entity.getY() + offset.y;
        
        return canMoveTo(entity, newX, newY);
    }
    
    /**
     * Smart pathfinding that tries to move toward a target.
     * Attempts horizontal movement first if it's the larger distance,
     * then falls back to vertical movement if blocked.
     * 
     * @param entity The entity to move
     * @param targetX Target x coordinate
     * @param targetY Target y coordinate
     * @return Position of successful move, or null if no valid move found
     */
    public Position findSmartMoveToward(Positionable entity, int targetX, int targetY) {
        int currentX = entity.getX();
        int currentY = entity.getY();
        int dx = targetX - currentX;
        int dy = targetY - currentY;
        
        // Try to move on the axis with the larger distance first
        if (Math.abs(dx) > Math.abs(dy)) {
            // Try horizontal movement first
            int newX = currentX + (dx > 0 ? 1 : -1);
            if (canMoveTo(entity, newX, currentY)) {
                return new Position(newX, currentY);
            }
            // If horizontal blocked, try vertical
            if (dy != 0) {
                int newY = currentY + (dy > 0 ? 1 : -1);
                if (canMoveTo(entity, currentX, newY)) {
                    return new Position(currentX, newY);
                }
            }
        } else if (dy != 0) {
            // Try vertical movement first
            int newY = currentY + (dy > 0 ? 1 : -1);
            if (canMoveTo(entity, currentX, newY)) {
                return new Position(currentX, newY);
            }
            // If vertical blocked, try horizontal
            if (dx != 0) {
                int newX = currentX + (dx > 0 ? 1 : -1);
                if (canMoveTo(entity, newX, currentY)) {
                    return new Position(newX, currentY);
                }
            }
        }
        
        return null; // No valid move found
    }
    
    /**
     * Attempts random movement with retry logic.
     * Tries up to maxAttempts random directions.
     * 
     * @param entity The entity to move
     * @param maxAttempts Maximum number of random directions to try
     * @param random Random number generator to use
     * @return Position of successful move, or null if no valid move found
     */
    public Position findRandomMove(Positionable entity, int maxAttempts, java.util.Random random) {
        int currentX = entity.getX();
        int currentY = entity.getY();
        
        for (int attempts = 0; attempts < maxAttempts; attempts++) {
            int direction = random.nextInt(4);
            Position offset = getDirectionOffset(direction);
            
            int newX = currentX + offset.x;
            int newY = currentY + offset.y;
            
            if (canMoveTo(entity, newX, newY)) {
                return new Position(newX, newY);
            }
        }
        
        return null; // No valid move found after all attempts
    }

    public static class Position {
        public final int x, y;
        public Position(int x, int y) { this.x = x; this.y = y; }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Position p)) return false;
            return x == p.x && y == p.y;
        }
        @Override public int hashCode() { return 31 * x + y; }
        @Override public String toString() { return "(" + x + ", " + y + ")"; }
    }
}
