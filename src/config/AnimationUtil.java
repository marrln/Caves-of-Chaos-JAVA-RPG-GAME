package config;

import graphics.AssetManager;

public class AnimationUtil {
    
    private static final int FRAME_DURATION_MS = 100;
    private static final int DAMAGE_TIMING_PERCENT = 60;
    private static final int DEFAULT_FRAME_COUNT = 4;
    
    private static final AssetManager assetManager = AssetManager.getInstance();
    
    public static int getPlayerAnimationDuration(String animationType, String playerName, int attackNumber) {
        // Normalize player name to lowercase for sprite ID
        String playerClass = playerName.toLowerCase();
        
        // Map generic animation types to specific sprite IDs
        String specificAnimation = switch (animationType.toLowerCase()) {
            case "attack" -> {
                // Use the specific attack number (attack01, attack02, etc.)
                if (attackNumber < 1) attackNumber = 1; // Safety: default to attack01
                yield "attack" + String.format("%02d", attackNumber);
            }
            case "hurt", "death", "walk", "idle" -> animationType.toLowerCase();
            default -> {
                System.err.println("Warning: Unknown player animation type '" + animationType + "', using default");
                yield "idle";
            }
        };
        
        String spriteId = playerClass + "_" + specificAnimation;
        return getFrameCount(spriteId) * FRAME_DURATION_MS;
    }
    

    public static int getPlayerAnimationDuration(String animationType, String playerName) {
        return getPlayerAnimationDuration(animationType, playerName, 0);
    }
    

    public static int getEnemyAnimationDuration(String animationType, String enemyTypePrefix, int attackNumber) {
        // Map generic animation types to specific sprite IDs
        String specificAnimation = switch (animationType.toLowerCase()) {
            case "attack" -> {
                // Use the specific attack number (attack01, attack02, attack03, etc.)
                if (attackNumber < 1) attackNumber = 1; // Safety: default to attack01
                yield "attack" + String.format("%02d", attackNumber);
            }
            case "hurt", "death", "walk", "idle" -> animationType.toLowerCase();
            default -> {
                System.err.println("Warning: Unknown enemy animation type '" + animationType + "', using default");
                yield "idle";
            }
        };
        
        String spriteId = enemyTypePrefix + "_" + specificAnimation;
        return getFrameCount(spriteId) * FRAME_DURATION_MS;
    }
    
    public static int getEnemyAnimationDuration(String animationType, String enemyTypePrefix) {
        return getEnemyAnimationDuration(animationType, enemyTypePrefix, 0);
    }
    
    public static int getDamageTimingPercent() {
        return DAMAGE_TIMING_PERCENT;
    }

    private static int getFrameCount(String spriteId) {
        Integer frames = assetManager.getFrameCount(spriteId);
        if (frames != null && frames > 0) {
            return frames;
        }
        
        // Fallback: use default frame count and log warning
        System.err.println("Warning: No frame count found for sprite '" + spriteId + 
                          "', using default (" + DEFAULT_FRAME_COUNT + " frames)");
        return DEFAULT_FRAME_COUNT;
    }
}
