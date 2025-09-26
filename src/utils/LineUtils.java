package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public class LineUtils {

    public static class Point {
        public final int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Point other)) return false;
            return x == other.x && y == other.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ")";
        }
    }

    public static boolean traverseLine(int x1, int y1, int x2, int y2,
                                       BiPredicate<Integer, Integer> stepFunction) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        int x = x1, y = y1;

        while (true) {
            if (!stepFunction.test(x, y)) return false;
            if (x == x2 && y == y2) return true;

            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx) { err += dx; y += sy; }
        }
    }

    public static List<Point> getLinePoints(int x1, int y1, int x2, int y2) {
        List<Point> points = new ArrayList<>();
        traverseLine(x1, y1, x2, y2, (x, y) -> { points.add(new Point(x, y)); return true; });
        return points;
    }

    public static boolean hasLineOfSight(int x1, int y1, int x2, int y2,
                                         BiPredicate<Integer, Integer> isBlocked) {
        return traverseLine(x1, y1, x2, y2, (x, y) -> {
            if ((x == x1 && y == y1) || (x == x2 && y == y2)) return true;
            return !isBlocked.test(x, y);
        });
    }

    public static List<Point> getProjectilePath(int x1, int y1, int x2, int y2,
                                                BiPredicate<Integer, Integer> isBlocked) {
        List<Point> path = new ArrayList<>();
        traverseLine(x1, y1, x2, y2, (x, y) -> {
            path.add(new Point(x, y));
            return !isBlocked.test(x, y);
        });
        return path;
    }

    public static int manhattanDistance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public static double euclideanDistance(int x1, int y1, int x2, int y2) {
        int dx = x1 - x2, dy = y1 - y2;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public static boolean isAdjacent(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) <= 1 && Math.abs(y1 - y2) <= 1 && !(x1 == x2 && y1 == y2);
    }

    public static boolean isCardinallyAdjacent(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2), dy = Math.abs(y1 - y2);
        return (dx == 1 && dy == 0) || (dx == 0 && dy == 1);
    }
}
