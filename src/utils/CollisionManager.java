package utils;

import java.util.ArrayList;
import java.util.List;
import map.GameMap;

public class CollisionManager {

    private final GameMap gameMap;
    private List<? extends Positionable> entities;
    public interface Positionable {
        int getX();
        int getY();
        default int getCollisionRadius() { return 0; }
        default boolean isSolid() { return true; }

        // Returns the facing direction in cardinal degrees: 0=N, 1=E, 2=S, 3=W.
        // For rendering, 1=E (right) is default, 3=W (left) means mirrored.
        int getFacingDirection();
        void setFacingDirection(int dir);
        default boolean isFacingLeft() { return getFacingDirection() == 3; }
    }

    // Convert a movement offset (dx, dy) to a facing direction (0=N, 1=E, 2=S, 3=W).
    // Returns -1 if no movement.
    public static int getDirectionFromOffset(int dx, int dy) {
        if (dx == 0 && dy == 0) return -1;
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? 1 : 3;
        } else {
            return dy > 0 ? 2 : 0;
        }
    }

    /**
     * Utility: Returns true if the direction is left (W, 3).
     */
    public static boolean isLeftDirection(int dir) {
        return ((dir % 4) + 4) % 4 == 3;
    }

    public CollisionManager(GameMap gameMap) { this.gameMap = gameMap; }
    public void updateEntities(List<? extends Positionable> entities) { this.entities = entities; }

    public boolean isWallBlocked(int x, int y) {
        if (gameMap == null) return false;
        if (x < 0 || y < 0 || x >= gameMap.getWidth() || y >= gameMap.getHeight()) return true;
        var tile = gameMap.getTile(x, y);
        return tile != null && tile.isBlocked();
    }

    public boolean isEntityOccupied(int x, int y, Positionable exclude) {
        if (entities == null) return false;
        for (Positionable e : entities) {
            if (e != exclude && e.isSolid() && e.getX() == x && e.getY() == y) {
                return true;
            }
        }
        return false;
    }

    public boolean isEntityOccupied(int x, int y) { return isEntityOccupied(x, y, null); }
    public boolean isAccessible(int x, int y) { return !isWallBlocked(x, y) && !isEntityOccupied(x, y); }

    public boolean canMoveTo(Positionable entity, int newX, int newY) {
        return !isWallBlocked(newX, newY) && !isEntityOccupied(newX, newY, entity);
    }

    public Positionable getEntityAt(int x, int y) {
        if (entities == null) return null;
        for (Positionable e : entities) {
            if (e.getX() == x && e.getY() == y) return e;
        }
        return null;
    }

    public List<Positionable> getEntitiesInRadius(int cx, int cy, int radius) {
        List<Positionable> result = new ArrayList<>();
        if (entities == null) return result;

        for (Positionable e : entities) {
            int dx = e.getX() - cx;
            int dy = e.getY() - cy;
            if (Math.sqrt(dx * dx + dy * dy) <= radius) {
                result.add(e);
            }
        }
        return result;
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

        List<Position> path = new ArrayList<>();
        for (LineUtils.Point p : linePoints) {
            path.add(new Position(p.x, p.y));
        }
        return path;
    }

    // ========== COMMON MOVEMENT UTILITIES ==========

    public static Position getDirectionOffset(int direction) {
        return switch (direction) {
            case 0 -> new Position(0, -1);   // North
            case 1 -> new Position(1, 0);  // East
            case 2 -> new Position(0, 1);  // South
            case 3 -> new Position(-1, 0);   // West
            default -> new Position(0, 0); // No movement
        };
    }

    public boolean tryMoveEntityInDirection(Positionable entity, int direction) {
        Position offset = getDirectionOffset(direction);
        if (offset.x == 0 && offset.y == 0) return false;

        int newX = entity.getX() + offset.x;
        int newY = entity.getY() + offset.y;
        boolean moved = canMoveTo(entity, newX, newY);
        if (moved) {
            entity.setFacingDirection(direction);
        }
        return moved;
    }

    public Position findSmartMoveToward(Positionable entity, int targetX, int targetY) {
        int currentX = entity.getX();
        int currentY = entity.getY();
        int dx = targetX - currentX;
        int dy = targetY - currentY;

        if (Math.abs(dx) > Math.abs(dy)) {
            int newX = currentX + (dx > 0 ? 1 : -1);
            if (canMoveTo(entity, newX, currentY)) return new Position(newX, currentY);

            if (dy != 0) {
                int newY = currentY + (dy > 0 ? 1 : -1);
                if (canMoveTo(entity, currentX, newY)) return new Position(currentX, newY);
            }
        } else if (dy != 0) {
            int newY = currentY + (dy > 0 ? 1 : -1);
            if (canMoveTo(entity, currentX, newY)) return new Position(currentX, newY);

            if (dx != 0) {
                int newX = currentX + (dx > 0 ? 1 : -1);
                if (canMoveTo(entity, newX, currentY)) return new Position(newX, currentY);
            }
        }

        return null;
    }

    public Position findRandomMove(Positionable entity, int maxAttempts, java.util.Random random) {
        int currentX = entity.getX();
        int currentY = entity.getY();

        for (int attempts = 0; attempts < maxAttempts; attempts++) {
            int direction = random.nextInt(4);
            Position offset = getDirectionOffset(direction);
            int newX = currentX + offset.x;
            int newY = currentY + offset.y;
            if (canMoveTo(entity, newX, newY)) return new Position(newX, newY);
        }

        return null;
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
