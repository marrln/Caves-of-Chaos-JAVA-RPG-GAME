package graphics;

import config.Config;
import core.CombatState;
import enemies.AbstractEnemy;
import enemies.Enemy;
import enemies.EnemyType;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.List;
import map.FogOfWar;

public class EnemyRenderer {

    // === Config flag ===
    private static final boolean USE_GRAPHICS = Config.getBoolSetting("use_graphics");

    // Enemy visuals
    private static final int ENEMY_CIRCLE_SIZE = 28;
    private static final int HEALTH_BAR_WIDTH = 32;
    private static final int HEALTH_BAR_HEIGHT = 4;
    private static final int HEALTH_BAR_OFFSET_Y = -25;
    private static final int NAME_OFFSET_Y = -30;

    // Enemy colors (fallback)
    private static final Color DEFAULT_ENEMY_COLOR = Color.RED;
    private static final Color BOSS_COLOR = new Color(150, 0, 150);
    private static final Color ELITE_COLOR = new Color(200, 100, 0);

    // Health bar colors
    private static final Color HEALTH_BAR_BACKGROUND = Color.DARK_GRAY;
    private static final Color HEALTH_BAR_FULL = config.StyleConfig.getColor("statHigh", new Color(0x4EE39A));
    private static final Color HEALTH_BAR_MEDIUM = config.StyleConfig.getColor("statMedium", new Color(0xE39A4E));
    private static final Color HEALTH_BAR_LOW = config.StyleConfig.getColor("statLow", new Color(0xE04E4E));

    // === Sprite management ===
    private static final SpriteSheetLoader sheetLoader = new SpriteSheetLoader();

    // === Public rendering entry point ===
    public static void renderEnemies(Graphics2D g2d, List<Enemy> enemies,
                                     int tileSize, int cameraX, int cameraY,
                                     FogOfWar fogOfWar, double scale) {
        if (enemies == null || enemies.isEmpty()) return;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Enemy enemy : enemies) {
            float fogAlpha = 1.0f;
            boolean visible = true;
            if (fogOfWar != null) {
                fogAlpha = fogOfWar.getVisibilityStrength(enemy.getX(), enemy.getY());
                visible = fogAlpha > 0.05f; // Only skip if fully hidden
            }
            if (visible) {
                renderEnemy(g2d, enemy, tileSize, cameraX, cameraY, fogOfWar, scale, fogAlpha);
            }
        }
    }

    // === Internal rendering per enemy ===
    private static void renderEnemy(Graphics2D g2d, Enemy enemy,
                                    int tileSize, int cameraX, int cameraY,
                                    FogOfWar fogOfWar, double scale, float fogAlpha) {

        int scaledTile = (int) (tileSize * scale);
        int screenX = (enemy.getX() * tileSize) - cameraX + (tileSize / 2);
        int screenY = (enemy.getY() * tileSize) - cameraY + (tileSize / 2);

        float visibility = fogAlpha;

        boolean isMedusa = (enemy instanceof AbstractEnemy ae) && ae.getType() == EnemyType.MEDUSA_OF_CHAOS;
        int medusaYOffset = 0;

        if (isMedusa && USE_GRAPHICS) {
            String assetKey = buildAssetKey((AbstractEnemy) enemy);
            BufferedImage[] frames = sheetLoader.loadFrames(assetKey);
            if (frames.length > 0) {
                BufferedImage frame = frames[0];
                medusaYOffset = Math.round(frame.getHeight() * 0.7f);
            }
        }

        if (USE_GRAPHICS && enemy instanceof AbstractEnemy ae) {
            renderEnemySprite(g2d, screenX, screenY, ae, scaledTile, visibility);
        } else {
            renderEnemyCircle(g2d, screenX, screenY, getEnemyColor(enemy), visibility);
        }

        int overlayY = isMedusa && medusaYOffset > 0 ? screenY - medusaYOffset : screenY;

        if (!enemy.isDead() && visibility > 0.3f) {
            renderHealthBar(g2d, screenX, overlayY, enemy, visibility);
        }
        if (!enemy.isDead() && visibility > 0.6f) {
            renderEnemyName(g2d, screenX, overlayY, enemy, visibility);
        }
    }

    // === Sprite rendering ===
    private static void renderEnemySprite(Graphics2D g2d, int x, int y,
                                          AbstractEnemy enemy, int scaledTile, float visibility) {

        String assetKey = buildAssetKey(enemy);
        BufferedImage[] frames = sheetLoader.loadFrames(assetKey);

        if (frames.length == 0) {
            renderEnemyCircle(g2d, x, y, DEFAULT_ENEMY_COLOR, visibility);
            return;
        }

        boolean isMedusa = enemy.getType() == EnemyType.MEDUSA_OF_CHAOS;
        BufferedImage frame;
        int drawX, drawY, width, height;

        if (isMedusa) {
            frame = selectFrame(frames, enemy.isDead());
            width = Math.round(frame.getWidth() * 0.7f);
            height = Math.round(frame.getHeight() * 0.7f);
            drawX = x - width / 2;
            drawY = y - height + 16;
        } else {
            BufferedImage[] scaledFrames = sheetLoader.loadFrames(assetKey, scaledTile);
            frame = selectFrame(scaledFrames, enemy.isDead());
            width = scaledTile;
            height = scaledTile;
            drawX = x - scaledTile / 2;
            drawY = y - scaledTile / 2;
        }

        drawWithAlpha(g2d, visibility, () -> {
            if (enemy.isFacingLeft()) {
                g2d.drawImage(frame, drawX + width, drawY, -width, height, null);
            } else {
                g2d.drawImage(frame, drawX, drawY, width, height, null);
            }
        });
    }

    // === Helpers ===
    private static String buildAssetKey(AbstractEnemy enemy) {
        CombatState combatState = enemy.getCombatState();
        CombatState.State state = combatState.getCurrentState();
        String enemyName = enemy.getName().toLowerCase().replace(" ", "_");

        if (enemy.isDead() || state == CombatState.State.DYING) {
            return enemyName + "_death";
        }
        return switch (state) {
            case ATTACKING -> enemyName + "_attack" + String.format("%02d", combatState.getAttackType());
            case MOVING -> enemyName + "_walk";
            case HURT -> enemyName + "_hurt";
            default -> enemyName + "_idle";
        };
    }

    private static BufferedImage selectFrame(BufferedImage[] frames, boolean isDead) {
        if (frames.length == 0) return null;
        return isDead
                ? frames[frames.length - 1]
                : frames[(int) ((System.currentTimeMillis() / 120) % frames.length)];
    }

    private static void drawWithAlpha(Graphics2D g2d, float alpha, Runnable drawAction) {
        Composite original = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        drawAction.run();
        g2d.setComposite(original);
    }

    private static void renderEnemyCircle(Graphics2D g2d, int x, int y,
                                          Color color, float visibility) {
        drawWithAlpha(g2d, visibility, () -> {
            int cx = x - (ENEMY_CIRCLE_SIZE / 2);
            int cy = y - (ENEMY_CIRCLE_SIZE / 2);
            Ellipse2D.Double circle = new Ellipse2D.Double(cx, cy, ENEMY_CIRCLE_SIZE, ENEMY_CIRCLE_SIZE);

            g2d.setColor(color);
            g2d.fill(circle);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.draw(circle);
        });
    }

    private static void renderHealthBar(Graphics2D g2d, int x, int y,
                                        Enemy enemy, float visibility) {
        drawWithAlpha(g2d, visibility, () -> {
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
        });
    }

    private static void renderEnemyName(Graphics2D g2d, int x, int y,
                                        Enemy enemy, float visibility) {
        drawWithAlpha(g2d, visibility, () -> {
            Font styledFont = config.StyleConfig.getFont("EbGaramond", new Font("Serif", Font.PLAIN, 12));
            g2d.setFont(styledFont);

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(enemy.getName());
            int textX = x - (textWidth / 2);
            int textY = y + NAME_OFFSET_Y;

            g2d.setColor(Color.BLACK);
            g2d.drawString(enemy.getName(), textX + 1, textY + 1);
            g2d.setColor(Color.WHITE);
            g2d.drawString(enemy.getName(), textX, textY);
        });
    }

    private static Color getEnemyColor(Enemy enemy) {
        if (!(enemy instanceof AbstractEnemy ae)) return DEFAULT_ENEMY_COLOR;

        return switch (ae.getType()) {
            case MEDUSA_OF_CHAOS -> BOSS_COLOR;
            case ELITE_ORC, GREATSWORD_SKELETON, ORC_RIDER, WEREBEAR -> ELITE_COLOR;
            case ARMORED_ORC, ARMORED_SKELETON -> new Color(180, 50, 50);
            case WEREWOLF -> new Color(139, 69, 19);
            case SLIME -> new Color(0, 200, 0);
            default -> DEFAULT_ENEMY_COLOR;
        };
    }

    private static Color getHealthBarColor(double healthPercent) {
        if (healthPercent > 0.6) return HEALTH_BAR_FULL;
        if (healthPercent > 0.3) return HEALTH_BAR_MEDIUM;
        return HEALTH_BAR_LOW;
    }
}
