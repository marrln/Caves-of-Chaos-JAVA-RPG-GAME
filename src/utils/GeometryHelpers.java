package utils;

/**
 * Geometric and mathematical utilities for spatial calculations.
 * Handles distances, adjacency, direction conversions, clamping, and interpolation.
 * Supports both integer (grid-based) and double (smooth movement) operations.
 */
public class GeometryHelpers {

    // ===== POSITION DATA STRUCTURE =====
    
    public static class Position {
        public final int x, y;
        
        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Position p)) return false;
            return x == p.x && y == p.y;
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

    // ===== DISTANCE CALCULATIONS =====
    
    public static int getManhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1);
    }
    
    public static double getEuclideanDistance(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    // Overload for double precision (smooth movement, projectiles)
    public static double getEuclideanDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    // Squared distance for performance (avoids sqrt when comparing distances)
    public static double getEuclideanDistanceSquared(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    // ===== ADJACENCY CHECKS =====
    
    public static boolean isAdjacent(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        return (dx <= 1 && dy <= 1) && !(dx == 0 && dy == 0);
    }
    
    public static boolean isCardinallyAdjacent(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }

    // ===== DIRECTION CONVERSIONS =====
    
    // Convert movement offset (dx, dy) to facing direction (0=N, 1=E, 2=S, 3=W)
    // Returns -1 if no movement
    public static int getDirectionFromOffset(int dx, int dy) {
        if (dx == 0 && dy == 0) return -1;
        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? 1 : 3; // E or W
        } else {
            return dy > 0 ? 2 : 0; // S or N
        }
    }
    
    // Convert direction to offset vector
    public static Position getDirectionOffset(int direction) {
        return switch (direction) {
            case 0 -> new Position(0, -1);  // North
            case 1 -> new Position(1, 0);   // East
            case 2 -> new Position(0, 1);   // South
            case 3 -> new Position(-1, 0);  // West
            default -> new Position(0, 0);  // No movement
        };
    }
    
    // Check if direction is left-facing (West = 3)
    public static boolean isLeftDirection(int dir) {
        return ((dir % 4) + 4) % 4 == 3;
    }

    // ===== CLAMPING & BOUNDS =====
    
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    // ===== INTERPOLATION & SMOOTH MOVEMENT =====
    
    // Linear interpolation between two values (t: 0.0 = a, 1.0 = b)
    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
    
    // Normalize a 2D vector, returning [dx, dy]. Returns zero vector if input is zero length.
    public static double[] normalize(double dx, double dy) {
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < 0.0001) return new double[]{0.0, 0.0};
        return new double[]{dx / dist, dy / dist};
    }
    
    // Move from (x1, y1) toward (x2, y2) by specified distance. Stops at target if exceeded.
    public static double[] moveTowards(double x1, double y1, double x2, double y2, double distance) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double currentDist = Math.sqrt(dx * dx + dy * dy);
        
        if (currentDist <= distance) {
            return new double[]{x2, y2};
        }
        
        double ratio = distance / currentDist;
        return new double[]{x1 + dx * ratio, y1 + dy * ratio};
    }
}
