package config;

/**
 * Configuration class for animation frame counts and timing.
 * This will be used to calculate animation durations until the actual
 * animation system is implemented.
 */
public class AnimationConfig {
    
    /**
     * Frame counts for different animation types.
     * These will later be read from the enhanced assets.xml.
     */
    
    // Player animations (frames)
    public static final int PLAYER_ATTACK_FRAMES = 6;
    public static final int PLAYER_HURT_FRAMES = 3;
    public static final int PLAYER_DEATH_FRAMES = 8;
    public static final int PLAYER_WALK_FRAMES = 4;
    public static final int PLAYER_IDLE_FRAMES = 4;
    
    // Enemy animations (frames)
    public static final int ENEMY_ATTACK_FRAMES = 5;
    public static final int ENEMY_HURT_FRAMES = 2;
    public static final int ENEMY_DEATH_FRAMES = 6;
    public static final int ENEMY_WALK_FRAMES = 4;
    public static final int ENEMY_IDLE_FRAMES = 4;
    
    // Animation timing
    public static final int FRAME_DURATION_MS = 100; // 100ms per frame (10 FPS)
    
    /**
     * Gets the duration in milliseconds for a player animation.
     * 
     * @param animationType The animation type ("attack", "hurt", "death", "walk", "idle")
     * @return The duration in milliseconds
     */
    public static long getPlayerAnimationDuration(String animationType) {
        int frames = switch (animationType.toLowerCase()) {
            case "attack" -> PLAYER_ATTACK_FRAMES;
            case "hurt" -> PLAYER_HURT_FRAMES;
            case "death" -> PLAYER_DEATH_FRAMES;
            case "walk" -> PLAYER_WALK_FRAMES;
            case "idle" -> PLAYER_IDLE_FRAMES;
            default -> 4; // Default frame count
        };
        
        return frames * FRAME_DURATION_MS;
    }
    
    /**
     * Gets the duration in milliseconds for an enemy animation.
     * 
     * @param animationType The animation type ("attack", "hurt", "death", "walk", "idle")
     * @return The duration in milliseconds
     */
    public static long getEnemyAnimationDuration(String animationType) {
        int frames = switch (animationType.toLowerCase()) {
            case "attack" -> ENEMY_ATTACK_FRAMES;
            case "hurt" -> ENEMY_HURT_FRAMES;
            case "death" -> ENEMY_DEATH_FRAMES;
            case "walk" -> ENEMY_WALK_FRAMES;
            case "idle" -> ENEMY_IDLE_FRAMES;
            default -> 4; // Default frame count
        };
        
        return frames * FRAME_DURATION_MS;
    }
    
    /**
     * Gets the frame at which damage should be dealt during an attack animation.
     * This is typically around 60% through the animation.
     * 
     * @param totalFrames The total number of frames in the attack animation
     * @return The frame number when damage should be dealt
     */
    public static int getDamageFrame(int totalFrames) {
        return (int) Math.ceil(totalFrames * 0.6);
    }
    
    /**
     * Gets the percentage through an animation when damage should be dealt.
     * 
     * @return The percentage (0-100)
     */
    public static int getDamageTimingPercent() {
        return 60; // 60% through the animation
    }
}
