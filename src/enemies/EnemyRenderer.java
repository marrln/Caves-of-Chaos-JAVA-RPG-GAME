package enemies;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;

/**
 * Handles rendering of enemies in the game.
 * Provides simple placeholder rendering until animation system is implemented.
 */
public class EnemyRenderer {
    
    // Visual constants
    private static final int ENEMY_CIRCLE_SIZE = 28; // Increased from 20 to 28
    private static final int HEALTH_BAR_WIDTH = 32;  // Increased proportionally
    private static final int HEALTH_BAR_HEIGHT = 4;
    private static final int HEALTH_BAR_OFFSET_Y = -12; // Moved further up
    private static final int NAME_OFFSET_Y = -30;       // Moved further up
    
    // Colors for different enemy types
    private static final Color DEFAULT_ENEMY_COLOR = Color.RED;
    private static final Color BOSS_COLOR = new Color(150, 0, 150); // Purple for boss
    private static final Color ELITE_COLOR = new Color(200, 100, 0); // Orange for elite enemies
    private static final Color BASIC_COLOR = Color.RED;
    
    // Health bar colors
    private static final Color HEALTH_BAR_BACKGROUND = Color.DARK_GRAY;
    private static final Color HEALTH_BAR_FULL = Color.GREEN;
    private static final Color HEALTH_BAR_MEDIUM = Color.YELLOW;
    private static final Color HEALTH_BAR_LOW = Color.RED;
    
    /**
     * Renders all enemies in the provided list, respecting fog of war visibility.
     * 
     * @param g2d The graphics context
     * @param enemies List of enemies to render
     * @param tileSize The size of each tile in pixels
     * @param cameraX Camera offset X
     * @param cameraY Camera offset Y
     * @param fogOfWar The fog of war system (null to render all enemies)
     */
    public static void renderEnemies(Graphics2D g2d, List<Enemy> enemies, int tileSize, int cameraX, int cameraY, map.FogOfWar fogOfWar) {
        if (enemies == null || enemies.isEmpty()) {
            return;
        }
        
        // Enable anti-aliasing for smoother rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        for (Enemy enemy : enemies) {
            if (!enemy.isDead()) {
                // Only render enemies that are visible through fog of war
                if (fogOfWar == null || fogOfWar.isVisible(enemy.getX(), enemy.getY())) {
                    renderEnemy(g2d, enemy, tileSize, cameraX, cameraY, fogOfWar);
                }
            }
        }
    }

    /**
     * Legacy method for backward compatibility - renders all enemies without fog of war checks.
     * 
     * @param g2d The graphics context
     * @param enemies List of enemies to render
     * @param tileSize The size of each tile in pixels
     * @param cameraX Camera offset X
     * @param cameraY Camera offset Y
     * @deprecated Use renderEnemies with FogOfWar parameter instead
     */
    @Deprecated
    public static void renderEnemies(Graphics2D g2d, List<Enemy> enemies, int tileSize, int cameraX, int cameraY) {
        renderEnemies(g2d, enemies, tileSize, cameraX, cameraY, null);
    }
    
    /**
     * Renders a single enemy with optional fog of war effects.
     * 
     * @param g2d The graphics context
     * @param enemy The enemy to render
     * @param tileSize The size of each tile in pixels
     * @param cameraX Camera offset X
     * @param cameraY Camera offset Y
     * @param fogOfWar The fog of war system (null to ignore fog effects)
     */
    private static void renderEnemy(Graphics2D g2d, Enemy enemy, int tileSize, int cameraX, int cameraY, map.FogOfWar fogOfWar) {
        // Calculate screen position
        int screenX = (enemy.getX() * tileSize) - cameraX + (tileSize / 2);
        int screenY = (enemy.getY() * tileSize) - cameraY + (tileSize / 2);
        
        // Get enemy color based on type
        Color enemyColor = getEnemyColor(enemy);
        
        // Apply fog of war transparency if needed
        float visibility = 1.0f;
        if (fogOfWar != null) {
            visibility = fogOfWar.getVisibilityStrength(enemy.getX(), enemy.getY());
        }
        
        // Render enemy circle with proper transparency
        renderEnemyCircle(g2d, screenX, screenY, enemyColor, enemy, visibility);
        
        // Render health bar (only if enemy is reasonably visible)
        if (visibility > 0.3f) {
            renderHealthBar(g2d, screenX, screenY, enemy, visibility);
            
            // Render name (only if enemy is clearly visible)
            if (visibility > 0.6f) {
                renderEnemyName(g2d, screenX, screenY, enemy, visibility);
            }
        }
    }
    
    /**
     * Renders the enemy as a colored circle with fog of war transparency.
     */
    private static void renderEnemyCircle(Graphics2D g2d, int x, int y, Color color, Enemy enemy, float visibility) {
        // Save original composite for restoration
        Composite originalComposite = g2d.getComposite();
        
        // Apply visibility transparency
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, visibility));
        
        // Create circle centered on the position
        int circleX = x - (ENEMY_CIRCLE_SIZE / 2);
        int circleY = y - (ENEMY_CIRCLE_SIZE / 2);
        
        Ellipse2D.Double circle = new Ellipse2D.Double(circleX, circleY, ENEMY_CIRCLE_SIZE, ENEMY_CIRCLE_SIZE);
        
        // Fill the circle
        g2d.setColor(color);
        g2d.fill(circle);
        
        // Add border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.draw(circle);
        
        // Add combat state indicator (if attacking or hurt)
        if (enemy instanceof AbstractEnemy abstractEnemy) {
            renderCombatStateIndicator(g2d, x, y, abstractEnemy, visibility);
        }
        
        // Restore original composite
        g2d.setComposite(originalComposite);
    }

    /**
     * Legacy method for backward compatibility.
     */
    private static void renderEnemyCircle(Graphics2D g2d, int x, int y, Color color, Enemy enemy) {
        renderEnemyCircle(g2d, x, y, color, enemy, 1.0f);
    }
    
    /**
     * Renders a visual indicator for the enemy's combat state with fog visibility.
     */
    private static void renderCombatStateIndicator(Graphics2D g2d, int x, int y, AbstractEnemy enemy, float visibility) {
        // This will be visible during combat states
        switch (enemy.getCombatState().getCurrentState()) {
            case ATTACKING -> {
                // Red pulsing ring for attacking (respects visibility)
                int alpha = (int) (100 * visibility);
                g2d.setColor(new Color(255, 0, 0, alpha));
                g2d.setStroke(new BasicStroke(3f));
                int ringSize = ENEMY_CIRCLE_SIZE + 6;
                g2d.drawOval(x - ringSize/2, y - ringSize/2, ringSize, ringSize);
            }
            case HURT -> {
                // Yellow flash for hurt (respects visibility)
                int alpha = (int) (150 * visibility);
                g2d.setColor(new Color(255, 255, 0, alpha));
                g2d.fillOval(x - ENEMY_CIRCLE_SIZE/2, y - ENEMY_CIRCLE_SIZE/2, ENEMY_CIRCLE_SIZE, ENEMY_CIRCLE_SIZE);
            }
            case DYING -> {
                // Gray overlay for dying (respects visibility)
                int alpha = (int) (180 * visibility);
                g2d.setColor(new Color(128, 128, 128, alpha));
                g2d.fillOval(x - ENEMY_CIRCLE_SIZE/2, y - ENEMY_CIRCLE_SIZE/2, ENEMY_CIRCLE_SIZE, ENEMY_CIRCLE_SIZE);
            }
            case IDLE, MOVING -> {
                // No special indicator for idle or moving states
            }
        }
    }

    /**
     * Legacy method for backward compatibility.
     */
    private static void renderCombatStateIndicator(Graphics2D g2d, int x, int y, AbstractEnemy enemy) {
        renderCombatStateIndicator(g2d, x, y, enemy, 1.0f);
    }
    
    /**
     * Renders the health bar above the enemy with fog visibility.
     */
    private static void renderHealthBar(Graphics2D g2d, int x, int y, Enemy enemy, float visibility) {
        // Save original composite
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, visibility));
        
        int barX = x - (HEALTH_BAR_WIDTH / 2);
        int barY = y + HEALTH_BAR_OFFSET_Y;
        
        // Background
        g2d.setColor(HEALTH_BAR_BACKGROUND);
        g2d.fillRect(barX, barY, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        
        // Health fill
        double healthPercent = (double) enemy.getHp() / enemy.getMaxHp();
        int healthWidth = (int) (HEALTH_BAR_WIDTH * healthPercent);
        
        // Choose color based on health percentage
        Color healthColor = getHealthBarColor(healthPercent);
        g2d.setColor(healthColor);
        g2d.fillRect(barX, barY, healthWidth, HEALTH_BAR_HEIGHT);
        
        // Border
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1f));
        g2d.drawRect(barX, barY, HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        
        // Restore original composite
        g2d.setComposite(originalComposite);
    }

    /**
     * Legacy method for backward compatibility.
     */
    private static void renderHealthBar(Graphics2D g2d, int x, int y, Enemy enemy) {
        renderHealthBar(g2d, x, y, enemy, 1.0f);
    }
    
    /**
     * Renders the enemy name above the health bar with fog visibility.
     */
    private static void renderEnemyName(Graphics2D g2d, int x, int y, Enemy enemy, float visibility) {
        String name = enemy.getName();
        
        // Save original composite
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, visibility));
        
        // Set font
        Font font = new Font("Arial", Font.BOLD, 10);
        g2d.setFont(font);
        
        // Calculate text position (centered)
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(name);
        int textX = x - (textWidth / 2);
        int textY = y + NAME_OFFSET_Y;
        
        // Render text with outline for better visibility
        g2d.setColor(Color.BLACK);
        g2d.drawString(name, textX + 1, textY + 1); // Shadow
        g2d.setColor(Color.WHITE);
        g2d.drawString(name, textX, textY);
        
        // Restore original composite
        g2d.setComposite(originalComposite);
    }

    /**
     * Legacy method for backward compatibility.
     */
    private static void renderEnemyName(Graphics2D g2d, int x, int y, Enemy enemy) {
        renderEnemyName(g2d, x, y, enemy, 1.0f);
    }
    
    /**
     * Gets the appropriate color for an enemy based on its type.
     */
    private static Color getEnemyColor(Enemy enemy) {
        if (!(enemy instanceof AbstractEnemy)) {
            return DEFAULT_ENEMY_COLOR;
        }
        
        AbstractEnemy abstractEnemy = (AbstractEnemy) enemy;
        EnemyType type = abstractEnemy.getType();
        
        return switch (type) {
            case MEDUSA_OF_CHAOS -> BOSS_COLOR;
            case ELITE_ORC, GREATSWORD_SKELETON, ORC_RIDER, WEREBEAR -> ELITE_COLOR;
            case ARMORED_ORC, ARMORED_SKELETON -> new Color(180, 50, 50); // Dark red for armored
            case WEREWOLF -> new Color(139, 69, 19); // Brown for werewolf
            case SLIME -> new Color(0, 200, 0); // Green for slime
            default -> BASIC_COLOR;
        };
    }
    
    /**
     * Gets the appropriate health bar color based on health percentage.
     */
    private static Color getHealthBarColor(double healthPercent) {
        if (healthPercent > 0.6) {
            return HEALTH_BAR_FULL;
        } else if (healthPercent > 0.3) {
            return HEALTH_BAR_MEDIUM;
        } else {
            return HEALTH_BAR_LOW;
        }
    }
    
    /**
     * Renders debugging information for enemies (optional).
     */
    public static void renderEnemyDebugInfo(Graphics2D g2d, List<Enemy> enemies, int tileSize, int cameraX, int cameraY) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 8));
        g2d.setColor(Color.CYAN);
        
        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && enemy instanceof AbstractEnemy) {
                AbstractEnemy ae = (AbstractEnemy) enemy;
                int screenX = (enemy.getX() * tileSize) - cameraX;
                int screenY = (enemy.getY() * tileSize) - cameraY;
                
                // Show notice radius
                if (ae.hasNoticedPlayer) {
                    g2d.drawOval(
                        screenX - ae.stats.noticeRadius * tileSize,
                        screenY - ae.stats.noticeRadius * tileSize,
                        ae.stats.noticeRadius * tileSize * 2,
                        ae.stats.noticeRadius * tileSize * 2
                    );
                }
                
                // Show combat state
                String stateText = ae.getCombatState().getCurrentState().toString();
                g2d.drawString(stateText, screenX, screenY + 35);
            }
        }
    }
}
