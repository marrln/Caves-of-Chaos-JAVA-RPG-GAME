package graphics;

import config.Config;
import core.CombatState;
import enemies.AbstractEnemy;
import enemies.Enemy;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import map.FogOfWar;

public class EnemyRenderer {

    // === Config flag ===
    private static final boolean USE_GRAPHICS = Config.getBoolSetting("use_graphics");

    // Enemy visuals
    private static final int ENEMY_CIRCLE_SIZE = 28;
    private static final int HEALTH_BAR_WIDTH = 32;
    private static final int HEALTH_BAR_HEIGHT = 4;
    private static final int HEALTH_BAR_OFFSET_Y = -12;
    private static final int NAME_OFFSET_Y = -30;

    // Enemy colors (fallback)
    private static final Color DEFAULT_ENEMY_COLOR = Color.RED;
    private static final Color BOSS_COLOR = new Color(150, 0, 150);
    private static final Color ELITE_COLOR = new Color(200, 100, 0);
    private static final Color BASIC_COLOR = Color.RED;

    // Health bar colors
    private static final Color HEALTH_BAR_BACKGROUND = Color.DARK_GRAY;
    private static final Color HEALTH_BAR_FULL = Color.GREEN;
    private static final Color HEALTH_BAR_MEDIUM = Color.YELLOW;
    private static final Color HEALTH_BAR_LOW = Color.RED;

    // === Sprite management ===
    private static final SpriteSheetLoader sheetLoader = new SpriteSheetLoader();
    private static final Map<String, BufferedImage[]> spriteCache = new HashMap<>();

    public static void renderEnemies(Graphics2D g2d, List<Enemy> enemies,
                                     int tileSize, int cameraX, int cameraY, FogOfWar fogOfWar, double scale) {
        if (enemies == null || enemies.isEmpty()) return;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && (fogOfWar == null || fogOfWar.isVisible(enemy.getX(), enemy.getY()))) {
                renderEnemy(g2d, enemy, tileSize, cameraX, cameraY, fogOfWar, scale);
            }
        }
    }

    private static void renderEnemy(Graphics2D g2d, Enemy enemy,
                                    int tileSize, int cameraX, int cameraY, FogOfWar fogOfWar, double scale) {
        int scaledTile = (int)(tileSize * scale);
        int screenX = (enemy.getX() * tileSize) - cameraX + (tileSize / 2);
        int screenY = (enemy.getY() * tileSize) - cameraY + (tileSize / 2);

        float visibility = fogOfWar != null ? fogOfWar.getVisibilityStrength(enemy.getX(), enemy.getY()) : 1.0f;

        if (USE_GRAPHICS && enemy instanceof AbstractEnemy ae) {
            renderEnemySprite(g2d, screenX, screenY, ae, scaledTile, visibility);
        } else {
            renderEnemyCircle(g2d, screenX, screenY, getEnemyColor(enemy), enemy, visibility);
        }

        if (visibility > 0.3f) {
            renderHealthBar(g2d, screenX, screenY, enemy, visibility);
            if (visibility > 0.6f) renderEnemyName(g2d, screenX, screenY, enemy, visibility);
        }
    }

    // === SPRITE RENDERING WITH SCALING AND MIRRORING ===
    private static void renderEnemySprite(Graphics2D g2d, int x, int y,
                                          AbstractEnemy enemy, int scaledTile, float visibility) {
        CombatState combatState = enemy.getCombatState();
        CombatState.State state = combatState.getCurrentState();

        // Build asset key dynamically
        String enemyName = enemy.getName().toLowerCase().replace(" ", "_");
        String assetKey;
        switch (state) {
            case ATTACKING -> {
                int attackType = combatState.getAttackType();
                assetKey = enemyName + "_attack" + String.format("%02d", attackType);
            }
            case MOVING -> assetKey = enemyName + "_walk";
            case HURT -> assetKey = enemyName + "_hurt";
            case DYING -> assetKey = enemyName + "_death";
            default -> assetKey = enemyName + "_idle";
        }

        BufferedImage[] frames = spriteCache.computeIfAbsent(assetKey, key -> sheetLoader.loadFrames(key));
        if (frames.length == 0) {
            renderEnemyCircle(g2d, x, y, DEFAULT_ENEMY_COLOR, enemy, visibility);
            System.out.println("Missing enemy sprite for asset ID: " + assetKey);
            return;
        }

        // --- SCALE FRAMES ---
        String scaledKey = assetKey + "_scaled_" + scaledTile;
        BufferedImage[] scaledFrames = spriteCache.computeIfAbsent(scaledKey, key -> {
            BufferedImage[] scaled = new BufferedImage[frames.length];
            for (int i = 0; i < frames.length; i++) {
                BufferedImage buf = new BufferedImage(scaledTile, scaledTile, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = buf.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(frames[i], 0, 0, scaledTile, scaledTile, null);
                g.dispose();
                scaled[i] = buf;
            }
            return scaled;
        });

        long now = System.currentTimeMillis();
        int frameIndex = (int) ((now / 120) % scaledFrames.length); // 8 fps
        BufferedImage frame = scaledFrames[frameIndex];

        Composite original = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, visibility));

        int drawX = x - scaledTile / 2;
        int drawY = y - scaledTile / 2;

        // Flip horizontally if enemy is facing left (via Positionable)
        if (enemy.isFacingLeft()) {
            g2d.drawImage(frame, drawX + scaledTile, drawY, -scaledTile, scaledTile, null);
        } else {
            g2d.drawImage(frame, drawX, drawY, scaledTile, scaledTile, null);
        }

        g2d.setComposite(original);
    }

    // --- Remaining methods unchanged ---
    private static void renderEnemyCircle(Graphics2D g2d, int x, int y,
                                          Color color, Enemy enemy, float visibility) {
        Composite original = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, visibility));

        int cx = x - (ENEMY_CIRCLE_SIZE / 2);
        int cy = y - (ENEMY_CIRCLE_SIZE / 2);
        Ellipse2D.Double circle = new Ellipse2D.Double(cx, cy, ENEMY_CIRCLE_SIZE, ENEMY_CIRCLE_SIZE);

        g2d.setColor(color);
        g2d.fill(circle);
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(circle);

        if (enemy instanceof AbstractEnemy ae) renderCombatStateIndicator(g2d, x, y, ae, visibility);
        g2d.setComposite(original);
    }

    private static void renderCombatStateIndicator(Graphics2D g2d, int x, int y, AbstractEnemy enemy, float visibility) {
        int alpha;
        switch (enemy.getCombatState().getCurrentState()) {
            case ATTACKING -> {
                alpha = (int) (100 * visibility);
                g2d.setColor(new Color(255, 0, 0, alpha));
                g2d.setStroke(new BasicStroke(3f));
                int ringSize = ENEMY_CIRCLE_SIZE + 6;
                g2d.drawOval(x - ringSize / 2, y - ringSize / 2, ringSize, ringSize);
            }
            case HURT -> {
                alpha = (int) (150 * visibility);
                g2d.setColor(new Color(255, 255, 0, alpha));
                g2d.fillOval(x - ENEMY_CIRCLE_SIZE / 2, y - ENEMY_CIRCLE_SIZE / 2, ENEMY_CIRCLE_SIZE, ENEMY_CIRCLE_SIZE);
            }
            case DYING -> {
                alpha = (int) (180 * visibility);
                g2d.setColor(new Color(128, 128, 128, alpha));
                g2d.fillOval(x - ENEMY_CIRCLE_SIZE / 2, y - ENEMY_CIRCLE_SIZE / 2, ENEMY_CIRCLE_SIZE, ENEMY_CIRCLE_SIZE);
            }
            default -> {}
        }
    }

    private static void renderHealthBar(Graphics2D g2d, int x, int y, Enemy enemy, float visibility) {
        Composite original = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, visibility));

        int barX = x - (HEALTH_BAR_WIDTH / 2);
        int barY = y + HEALTH_BAR_OFFSET_Y;

        g2d.setColor(HEALTH_BAR_BACKGROUND);
        g2d.fillRect(barX, barY, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);

        double percent = (double) enemy.getHp() / enemy.getMaxHp();
        int healthWidth = (int) (HEALTH_BAR_WIDTH * percent);

        g2d.setColor(getHealthBarColor(percent));
        g2d.fillRect(barX, barY, healthWidth, HEALTH_BAR_HEIGHT);

        g2d.setColor(Color.BLACK);
        g2d.drawRect(barX, barY, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);

        g2d.setComposite(original);
    }

    private static void renderEnemyName(Graphics2D g2d, int x, int y, Enemy enemy, float visibility) {
        Composite original = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, visibility));

        Font font = new Font("Arial", Font.BOLD, 10);
        g2d.setFont(font);

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(enemy.getName());
        int textX = x - (textWidth / 2);
        int textY = y + NAME_OFFSET_Y;

        g2d.setColor(Color.BLACK);
        g2d.drawString(enemy.getName(), textX + 1, textY + 1);
        g2d.setColor(Color.WHITE);
        g2d.drawString(enemy.getName(), textX, textY);

        g2d.setComposite(original);
    }

    private static Color getEnemyColor(Enemy enemy) {
        if (!(enemy instanceof AbstractEnemy ae)) return DEFAULT_ENEMY_COLOR;

        return switch (ae.getType()) {
            case MEDUSA_OF_CHAOS -> BOSS_COLOR;
            case ELITE_ORC, GREATSWORD_SKELETON, ORC_RIDER, WEREBEAR -> ELITE_COLOR;
            case ARMORED_ORC, ARMORED_SKELETON -> new Color(180, 50, 50);
            case WEREWOLF -> new Color(139, 69, 19);
            case SLIME -> new Color(0, 200, 0);
            default -> BASIC_COLOR;
        };
    }

    private static Color getHealthBarColor(double healthPercent) {
        if (healthPercent > 0.6) return HEALTH_BAR_FULL;
        if (healthPercent > 0.3) return HEALTH_BAR_MEDIUM;
        return HEALTH_BAR_LOW;
    }
}
