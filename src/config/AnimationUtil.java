package config;

import graphics.AssetManager;

public class AnimationUtil {

    private static final int FRAME_DURATION_MS = 100;
    private static final int DAMAGE_TIMING_PERCENT = 60;
    private static final int DEFAULT_FRAME_COUNT = 4;
    private static final AssetManager assetManager = AssetManager.getInstance();

    public static int getPlayerAnimationDuration(String animationType, String playerName, int attackNumber) {
        String playerClass = playerName.toLowerCase();
        String specificAnimation = switch (animationType.toLowerCase()) {
            case "attack" -> {
                if (attackNumber < 1) attackNumber = 1;
                yield "attack" + String.format("%02d", attackNumber);
            }
            case "hurt", "death", "walk", "idle" -> animationType.toLowerCase();
            default -> {
                System.err.println("Warning: Unknown player animation type '" + animationType + "', using default");
                yield "idle";
            }
        };
        return getFrameCount(playerClass + "_" + specificAnimation) * FRAME_DURATION_MS;
    }

    public static int getPlayerAnimationDuration(String animationType, String playerName) {
        return getPlayerAnimationDuration(animationType, playerName, 0);
    }

    public static int getEnemyAnimationDuration(String animationType, String enemyTypePrefix, int attackNumber) {
        String specificAnimation = switch (animationType.toLowerCase()) {
            case "attack" -> {
                if (attackNumber < 1) attackNumber = 1;
                yield "attack" + String.format("%02d", attackNumber);
            }
            case "hurt", "death", "walk", "idle" -> animationType.toLowerCase();
            default -> {
                System.err.println("Warning: Unknown enemy animation type '" + animationType + "', using default");
                yield "idle";
            }
        };
        return getFrameCount(enemyTypePrefix + "_" + specificAnimation) * FRAME_DURATION_MS;
    }

    public static int getEnemyAnimationDuration(String animationType, String enemyTypePrefix) {
        return getEnemyAnimationDuration(animationType, enemyTypePrefix, 0);
    }

    public static int getDamageTimingPercent() { return DAMAGE_TIMING_PERCENT; }

    private static int getFrameCount(String spriteId) {
        Integer frames = assetManager.getFrameCount(spriteId);
        if (frames != null && frames > 0) return frames;
        System.err.println("Warning: No frame count found for sprite '" + spriteId + "', using default (" + DEFAULT_FRAME_COUNT + " frames)");
        return DEFAULT_FRAME_COUNT;
    }
}
