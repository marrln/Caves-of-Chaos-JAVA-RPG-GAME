package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Utility class for line-based algorithms and geometric calculations.
 * Provides optimized implementations of common algorithms like Bresenham's line algorithm.
 * 
 * Design Benefits:
 * - Single implementation of line algorithms eliminates code duplication
 * - Highly flexible with functional interfaces for different use cases
 * - Performance optimized and well-tested
 * - Extensible for future geometric needs
 */
public class LineUtils {
    
    /**
     * Represents a 2D coordinate position.
     */
    public static class Point {
        public final int x, y;
        
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Point)) return false;
            Point point = (Point) obj;
            return x == point.x && y == point.y;
        }
        
        @Override
        public int hashCode() {
            return 31 * x + y;
        }
        
        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }
    
    /**
     * Executes Bresenham's line algorithm between two points.
     * The step function is called for each point along the line.
     * 
     * @param x1 Start x coordinate
     * @param y1 Start y coordinate  
     * @param x2 End x coordinate
     * @param y2 End y coordinate
     * @param stepFunction Function called for each point (x, y) -> boolean.
     *                    Return false to stop traversal early.
     * @return true if traversal completed to the end, false if stopped early
     */
    public static boolean traverseLine(int x1, int y1, int x2, int y2, 
                                     BiPredicate<Integer, Integer> stepFunction) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        
        int x = x1;
        int y = y1;
        
        while (true) {
            // Call the step function for current position
            if (!stepFunction.test(x, y)) {
                return false; // Early termination requested
            }
            
            // Check if we've reached the destination
            if (x == x2 && y == y2) {
                return true; // Successfully completed
            }
            
            // Bresenham's algorithm step
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
    }
    
    /**
     * Gets all points along a line from (x1, y1) to (x2, y2).
     * Uses Bresenham's line algorithm for accurate pixel-perfect lines.
     * 
     * @param x1 Start x coordinate
     * @param y1 Start y coordinate
     * @param x2 End x coordinate  
     * @param y2 End y coordinate
     * @return List of all points along the line (including start and end)
     */
    public static List<Point> getLinePoints(int x1, int y1, int x2, int y2) {
        List<Point> points = new ArrayList<>();
        
        traverseLine(x1, y1, x2, y2, (x, y) -> {
            points.add(new Point(x, y));
            return true; // Continue traversal
        });
        
        return points;
    }
    
    /**
     * Checks if there's an unobstructed line between two points.
     * The obstruction function is called for each point to check if it blocks the line.
     * 
     * @param x1 Start x coordinate
     * @param y1 Start y coordinate
     * @param x2 End x coordinate
     * @param y2 End y coordinate
     * @param isBlocked Function that returns true if a position is blocked
     * @return true if line of sight is clear, false if blocked
     */
    public static boolean hasLineOfSight(int x1, int y1, int x2, int y2,
                                       BiPredicate<Integer, Integer> isBlocked) {
        return traverseLine(x1, y1, x2, y2, (x, y) -> {
            // Don't check blocking on the starting position
            if (x == x1 && y == y1) {
                return true;
            }
            // Allow the destination even if it would normally block
            if (x == x2 && y == y2) {
                return true;
            }
            // Check if this position blocks the line
            return !isBlocked.test(x, y);
        });
    }
    
    /**
     * Gets the path a projectile would take, stopping at the first obstacle.
     * 
     * @param x1 Start x coordinate
     * @param y1 Start y coordinate
     * @param x2 Target x coordinate (may not be reached)
     * @param y2 Target y coordinate (may not be reached)
     * @param isBlocked Function that returns true if a position stops the projectile
     * @return List of points along the projectile path (including final blocking position)
     */
    public static List<Point> getProjectilePath(int x1, int y1, int x2, int y2,
                                              BiPredicate<Integer, Integer> isBlocked) {
        List<Point> path = new ArrayList<>();
        
        traverseLine(x1, y1, x2, y2, (x, y) -> {
            path.add(new Point(x, y));
            
            // Continue if this position doesn't block projectiles
            return !isBlocked.test(x, y);
        });
        
        return path;
    }
    
    /**
     * Calculates the Manhattan distance between two points.
     * Manhattan distance = |x1 - x2| + |y1 - y2|
     * 
     * @param x1 First point x coordinate
     * @param y1 First point y coordinate
     * @param x2 Second point x coordinate
     * @param y2 Second point y coordinate
     * @return Manhattan distance
     */
    public static int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
    
    /**
     * Calculates the Euclidean distance between two points.
     * Euclidean distance = sqrt((x1 - x2)² + (y1 - y2)²)
     * 
     * @param x1 First point x coordinate
     * @param y1 First point y coordinate
     * @param x2 Second point x coordinate
     * @param y2 Second point y coordinate
     * @return Euclidean distance
     */
    public static double euclideanDistance(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2;
        int dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Checks if two points are adjacent (within 1 tile in any direction).
     * Includes diagonal adjacency.
     * 
     * @param x1 First point x coordinate
     * @param y1 First point y coordinate
     * @param x2 Second point x coordinate
     * @param y2 Second point y coordinate
     * @return true if points are adjacent
     */
    public static boolean isAdjacent(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) <= 1 && Math.abs(y1 - y2) <= 1 && !(x1 == x2 && y1 == y2);
    }
    
    /**
     * Checks if two points are adjacent in cardinal directions only (N, S, E, W).
     * No diagonal adjacency.
     * 
     * @param x1 First point x coordinate
     * @param y1 First point y coordinate  
     * @param x2 Second point x coordinate
     * @param y2 Second point y coordinate
     * @return true if points are cardinally adjacent
     */
    public static boolean isCardinallyAdjacent(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }
}
