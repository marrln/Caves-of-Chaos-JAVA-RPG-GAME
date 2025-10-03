package graphics;

import config.Config;
import core.CombatState;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import map.FogOfWar;
import player.AbstractPlayer;

public class PlayerRenderer {

    private static final boolean USE_GRAPHICS = Config.getBoolSetting("use_graphics");
    private static final SpriteSheetLoader sheetLoader = new SpriteSheetLoader();

    // Cache scaled sprite frames: assetId → BufferedImage[]
    private static final Map<String, BufferedImage[]> spriteCache = new HashMap<>();

    // Animation tracking
    private static String lastAssetId = "";
    private static int frameIndex = 0;
    private static long lastFrameTime = 0;
    private static final int FRAME_DURATION = 150; // ms per frame

    private static BufferedImage[] healFrames = null;
    private static BufferedImage[] manaFrames = null;
    private static final String HEAL_EFFECT_ID = "health_potion_effect";
    private static final String MANA_EFFECT_ID = "mana_potion_effect";

    public static void renderPlayer(Graphics2D g2d,
                                    AbstractPlayer player,
                                    int tileSize,
                                    int cameraOffsetX,
                                    int cameraOffsetY,
                                    FogOfWar fog,
                                    double scale) {
        if (player == null) return;

        int scaledTile = (int)(tileSize * scale);

        int screenX = (player.getX() * tileSize) - cameraOffsetX;
        int screenY = (player.getY() * tileSize) - cameraOffsetY;

        if (!USE_GRAPHICS) {
            // fallback circle
            g2d.setColor(Color.CYAN);
            g2d.fillOval(screenX, screenY, scaledTile, scaledTile);
            g2d.setColor(Color.WHITE);
            g2d.drawOval(screenX, screenY, scaledTile, scaledTile);
            return;
        }

        // Build assetId dynamically based on class and state
        String assetId = buildAssetId(player);
        if (assetId == null) return;

        // Load and scale frames if needed
        BufferedImage[] frames = spriteCache.computeIfAbsent(assetId, id -> {
            BufferedImage[] originalFrames = sheetLoader.loadFrames(id);
            BufferedImage[] scaledFrames = new BufferedImage[originalFrames.length];
            for (int i = 0; i < originalFrames.length; i++) {
                scaledFrames[i] = new BufferedImage(scaledTile, scaledTile, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = scaledFrames[i].createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(originalFrames[i], 0, 0, scaledTile, scaledTile, null);
                g.dispose();
            }
            return scaledFrames;
        });

        if (frames.length == 0) return;

        // Reset frame index if animation changed
        if (!assetId.equals(lastAssetId)) {
            frameIndex = 0;
            lastFrameTime = System.currentTimeMillis();
            lastAssetId = assetId;
        }

        // Advance animation frame
        long now = System.currentTimeMillis();
        if (now - lastFrameTime > FRAME_DURATION) {
            frameIndex = (frameIndex + 1) % frames.length;
            lastFrameTime = now;
        }

        BufferedImage frame = frames[frameIndex];

        int drawX = screenX + (tileSize - scaledTile) / 2;
        int drawY = screenY + (tileSize - scaledTile) / 2;

        // Flip horizontally if player is facing left (via Positionable)
        if (player.isFacingLeft()) {
            g2d.drawImage(frame, drawX + scaledTile, drawY, -scaledTile, scaledTile, null);
        } else {
            g2d.drawImage(frame, drawX, drawY, scaledTile, scaledTile, null);
        }

        renderHealingEffect(g2d, player, drawX, drawY, scaledTile);
        renderManaEffect(g2d, player, drawX, drawY, scaledTile);
    }

    private static String buildAssetId(AbstractPlayer player) {
        String className = player.getClass().getSimpleName().toLowerCase();
        CombatState combat = player.getCombatState();
        CombatState.State state = combat.getCurrentState();

        return switch (state) {
            case IDLE -> className + "_idle";
            case MOVING -> className + "_walk";
            case ATTACKING -> String.format("%s_attack%02d", className, combat.getAttackType());
            case HURT -> className + "_hurt";
            case DYING -> className + "_death";
            default -> className + "_idle";
        };
    }

    private static void loadEffectFrames(String effectId, int scaledTile) {
        SpriteSheetLoader loader = new SpriteSheetLoader();
        BufferedImage[] originalFrames = loader.loadFrames(effectId);
        BufferedImage[] scaledFrames = new BufferedImage[originalFrames.length];
        for (int i = 0; i < originalFrames.length; i++) {
            scaledFrames[i] = new BufferedImage(scaledTile, scaledTile, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = scaledFrames[i].createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalFrames[i], 0, 0, scaledTile, scaledTile, null);
            g.dispose();
        }

        if (effectId.equals(HEAL_EFFECT_ID)) healFrames = scaledFrames;
        else if (effectId.equals(MANA_EFFECT_ID)) manaFrames = scaledFrames;
    }

    private static void renderHealingEffect(Graphics2D g2d, AbstractPlayer player, int drawX, int drawY, int scaledTile) {
        if (!player.isHealingEffectActive()) return;

        if (healFrames == null) loadEffectFrames(HEAL_EFFECT_ID, scaledTile);
        if (healFrames.length == 0) return;

        int healFrameIndex = (int) ((System.currentTimeMillis() / 100) % healFrames.length);
        g2d.drawImage(healFrames[healFrameIndex], drawX, drawY, scaledTile, scaledTile, null);
    }

    private static void renderManaEffect(Graphics2D g2d, AbstractPlayer player, int drawX, int drawY, int scaledTile) {
        if (!player.isManaEffectActive()) return;

        if (manaFrames == null) loadEffectFrames(MANA_EFFECT_ID, scaledTile);
        if (manaFrames.length == 0) return;

        int manaFrameIndex = (int) ((System.currentTimeMillis() / 100) % manaFrames.length);
        g2d.drawImage(manaFrames[manaFrameIndex], drawX, drawY, scaledTile, scaledTile, null);
    }

}
