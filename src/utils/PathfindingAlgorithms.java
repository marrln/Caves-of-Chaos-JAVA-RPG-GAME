package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pathfinding and line-of-sight algorithms.
 * Uses collision callbacks and geometry helpers for calculations.
 */
public class PathfindingAlgorithms {

    // ===== LINE OF SIGHT =====
    
    /**
     * Bresenham's line algorithm for line-of-sight checks.
     * @param blockCheck Callback that returns true if a tile blocks vision
     */
    public static boolean hasLineOfSight(int x1, int y1, int x2, int y2, 
                                         java.util.function.BiPredicate<Integer, Integer> blockCheck) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int x = x1;
        int y = y1;

        while (true) {
            // Don't check start/end tiles
            if (!((x == x1 && y == y1) || (x == x2 && y == y2))) {
                if (blockCheck.test(x, y)) return false;
            }

            if (x == x2 && y == y2) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
        return true;
    }

    // ===== PROJECTILE PATH =====
    
    /**
     * Calculate projectile path until it hits an obstacle.
     * @param blockCheck Callback that returns true if a tile blocks the projectile
     */
    public static List<GeometryHelpers.Position> getProjectilePath(int x1, int y1, int x2, int y2,
                                                                     java.util.function.BiPredicate<Integer, Integer> blockCheck) {
        List<GeometryHelpers.Position> path = new ArrayList<>();
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int x = x1;
        int y = y1;

        while (true) {
            path.add(new GeometryHelpers.Position(x, y));

            if (x == x2 && y == y2) break;
            if (blockCheck.test(x, y)) break;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
        }
        return path;
    }

    // ===== SMART MOVEMENT =====
    
    /**
     * Find next move toward target with obstacle avoidance.
     * Tries primary axis first, then secondary axis as fallback.
     * @param canMove Callback that returns true if entity can move to position
     */
    public static GeometryHelpers.Position findSmartMoveToward(int currentX, int currentY, 
                                                                int targetX, int targetY,
                                                                java.util.function.BiPredicate<Integer, Integer> canMove) {
        int dx = targetX - currentX;
        int dy = targetY - currentY;

        // Try horizontal movement first if it's the larger component
        if (Math.abs(dx) > Math.abs(dy)) {
            int newX = currentX + (dx > 0 ? 1 : -1);
            if (canMove.test(newX, currentY)) {
                return new GeometryHelpers.Position(newX, currentY);
            }
            // Fallback to vertical
            if (dy != 0) {
                int newY = currentY + (dy > 0 ? 1 : -1);
                if (canMove.test(currentX, newY)) {
                    return new GeometryHelpers.Position(currentX, newY);
                }
            }
        } 
        // Try vertical movement first
        else if (dy != 0) {
            int newY = currentY + (dy > 0 ? 1 : -1);
            if (canMove.test(currentX, newY)) {
                return new GeometryHelpers.Position(currentX, newY);
            }
            // Fallback to horizontal
            if (dx != 0) {
                int newX = currentX + (dx > 0 ? 1 : -1);
                if (canMove.test(newX, currentY)) {
                    return new GeometryHelpers.Position(newX, currentY);
                }
            }
        }

        return null; // No valid move found
    }

    // ===== RANDOM MOVEMENT =====
    
    /**
     * Find random valid move in any direction.
     * @param canMove Callback that returns true if entity can move to position
     */
    public static GeometryHelpers.Position findRandomMove(int currentX, int currentY, 
                                                           int maxAttempts, Random random,
                                                           java.util.function.BiPredicate<Integer, Integer> canMove) {
        for (int attempts = 0; attempts < maxAttempts; attempts++) {
            int direction = random.nextInt(4);
            GeometryHelpers.Position offset = GeometryHelpers.getDirectionOffset(direction);
            int newX = currentX + offset.x;
            int newY = currentY + offset.y;
            if (canMove.test(newX, newY)) {
                return new GeometryHelpers.Position(newX, newY);
            }
        }
        return null; // No valid move found
    }
}
