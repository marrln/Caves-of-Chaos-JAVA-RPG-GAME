package audio;

import graphics.AssetManager;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.sound.sampled.*;

/**
 * Manages all sound effects in the game.
 * 
 * Design Philosophy:
 * - Singleton pattern for centralized SFX control
 * - Lazy loading: sounds are loaded on first use to reduce startup time
 * - Fail-safe: missing audio system doesn't crash the game
 * - Self-documenting API: each method clearly states what sound it plays
 * 
 * Usage: SFXManager.getInstance().playPlayerHurt();
 */
public class SFXManager {

    private static SFXManager instance;
    private static final Random RANDOM = new Random();
    
    // Cache of loaded sound clips to avoid reloading
    private final Map<String, Clip> soundCache = new HashMap<>();
    
    private boolean isSfxEnabled = true;
    private float volumeLevel = 0.7f; // 0.0 to 1.0
    
    private SFXManager() {
        // Check if audio system is available
        try {
            AudioSystem.getMixerInfo();
        } catch (Exception e) {
            System.err.println("Warning: Audio system not available - SFX disabled");
            isSfxEnabled = false;
        }
    }
    
    public static synchronized SFXManager getInstance() {
        if (instance == null) {
            instance = new SFXManager();
        }
        return instance;
    }
    
    // ====== CORE PLAYBACK ======
    
    /**
     * Loads and plays a sound effect.
     * Sounds are cached after first load for performance.
     * 
     * @param soundId The asset ID from assets.xml
     */
    private void playSound(String soundId) {
        if (!isSfxEnabled) return;
        
        try {
            Clip clip = getOrLoadClip(soundId);
            if (clip == null) return;
            
            // CRITICAL: Stop the clip if it's already playing to prevent overlap
            if (clip.isRunning()) {
                clip.stop();
            }
            
            // Reset clip to beginning and play
            clip.setFramePosition(0);
            
            // Apply volume
            setClipVolume(clip, volumeLevel);
            
            clip.start();
            
        } catch (Exception e) {
            System.err.println("Failed to play sound: " + soundId + " - " + e.getMessage());
        }
    }
    
    /**
     * Retrieves a clip from cache or loads it if not already loaded.
     * Uses lazy initialization to reduce memory footprint.
     */
    private Clip getOrLoadClip(String soundId) {
        // Return cached clip if available
        if (soundCache.containsKey(soundId)) {
            return soundCache.get(soundId);
        }
        
        // Load new clip
        try {
            AssetManager am = AssetManager.getInstance();
            String filePath = am.getSoundAssetPath(soundId);
            
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(filePath));
            AudioFormat format = audioStream.getFormat();
            
            // Convert to PCM format if necessary (same as MusicManager approach)
            AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                format.getSampleRate(),
                16,
                format.getChannels(),
                format.getChannels() * 2,
                format.getSampleRate(),
                false
            );
            
            AudioInputStream convertedStream = AudioSystem.isConversionSupported(targetFormat, format)
                ? AudioSystem.getAudioInputStream(targetFormat, audioStream)
                : audioStream;
            
            Clip clip = AudioSystem.getClip();
            clip.open(convertedStream);
            
            // Cache for future use
            soundCache.put(soundId, clip);
            
            return clip;
            
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            System.err.println("Failed to load sound: " + soundId + " - " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Sets the volume of a clip using the MASTER_GAIN control.
     * Volume range: 0.0 (silent) to 1.0 (full volume)
     */
    private void setClipVolume(Clip clip, float volume) {
        if (clip == null) return;
        
        try {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            
            // Convert linear volume (0.0-1.0) to decibel gain
            // Use logarithmic scale for more natural volume perception
            float gain = min + (max - min) * volume;
            gainControl.setValue(gain);
            
        } catch (IllegalArgumentException e) {
            // Control not supported, play at default volume
        }
    }
    
    /**
     * Plays a random sound from a set of similar sounds.
     * Adds variety to repetitive actions like footsteps or enemy hurt sounds.
     */
    private void playRandomSound(String... soundIds) {
        if (soundIds.length == 0) return;
        String randomId = soundIds[RANDOM.nextInt(soundIds.length)];
        playSound(randomId);
    }
    
    // ====== PLAYER SOUNDS ======
    
    /**
     * Plays the Duelist's successful melee hit sound.
     * Call this when a Duelist's melee attack connects with an enemy.
     */
    public void playDuelistHit() {
        playSound("duelist_hit");
    }
    
    /**
     * Plays the Duelist's missed attack sound.
     * Call this when a Duelist's attack misses or hits nothing.
     */
    public void playDuelistMiss() {
        playSound("duelist_miss");
    }
    
    /**
     * Plays the player hurt sound.
     * Call this when the player takes damage from any source.
     */
    public void playPlayerHurt() {
        playSound("player_hurt");
    }
    
    /**
     * Plays the player death sound.
     * Call this when the player's HP reaches 0.
     */
    public void playPlayerDeath() {
        playSound("player_death");
    }
    
    /**
     * Plays a random player footstep sound.
     * Call this during the player's walk animation for immersion.
     */
    public void playPlayerWalk() {
        playRandomSound("player_walk_1", "player_walk_2", "player_walk_3");
    }
    
    // ====== SPELL SOUNDS ======
    
    /**
     * Plays the spell casting sound.
     * Call this when the Wizard casts fire or ice spells.
     */
    public void playSpellCast() {
        playSound("spell_sound");
    }
    
    // ====== ENEMY SOUNDS ======
    
    /**
     * Plays a generic enemy attack sound.
     * Use this for basic enemies like slimes and basic orcs.
     */
    public void playEnemyAttackGeneric() {
        playSound("enemy_attack_generic");
    }
    
    /**
     * Plays a sword-based enemy attack sound (variant 1).
     * Use this for sword-wielding enemies like skeletons.
     */
    public void playEnemyAttackSword1() {
        playSound("enemy_attack_sword_1");
    }
    
    /**
     * Plays a sword-based enemy attack sound (variant 2).
     * Use this for heavy sword enemies like elite orcs or greatsword skeletons.
     */
    public void playEnemyAttackSword2() {
        playSound("enemy_attack_sword_2");
    }
    
    /**
     * Plays a random sword attack sound for variety.
     * Good default for most armed enemies.
     */
    public void playEnemyAttackSword() {
        playRandomSound("enemy_attack_sword_1", "enemy_attack_sword_2");
    }
    
    /**
     * Plays a random enemy hurt sound.
     * Call this when an enemy takes damage.
     */
    public void playEnemyHurt() {
        playRandomSound("enemy_hurt_1", "enemy_hurt_2", "enemy_hurt_3");
    }
    
    /**
     * Plays the enemy death sound.
     * Call this when an enemy's HP reaches 0.
     */
    public void playEnemyDeath() {
        playSound("enemy_death");
    }
    
    /**
     * Plays a random enemy footstep sound.
     * Call this during enemy walk animations.
     */
    public void playEnemyWalk() {
        playRandomSound("enemy_walk_1", "enemy_walk_2", "enemy_walk_3");
    }
    
    // ====== ITEM SOUNDS ======
    
    /**
     * Plays a random item pickup sound.
     * Call this when the player picks up any item from the ground.
     */
    public void playItemPickup() {
        playRandomSound("item_pickup_1", "item_pickup_2");
    }
    
    // ====== VOLUME & SETTINGS ======
    
    /**
     * Sets the master volume for all SFX.
     * @param volume 0.0 (silent) to 1.0 (full volume)
     */
    public void setVolume(float volume) {
        this.volumeLevel = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    public float getVolume() {
        return volumeLevel;
    }
    
    public void setSfxEnabled(boolean enabled) {
        this.isSfxEnabled = enabled;
    }
    
    public void toggleSfx() {
        this.isSfxEnabled = !this.isSfxEnabled;
    }
    
    public boolean isSfxEnabled() {
        return isSfxEnabled;
    }
    
    // ====== CLEANUP ======
    
    /**
     * Stops all playing sounds and releases audio resources.
     * Call this when shutting down the game.
     */
    public void cleanup() {
        for (Clip clip : soundCache.values()) {
            if (clip != null) {
                if (clip.isRunning()) {
                    clip.stop();
                }
                clip.close();
            }
        }
        soundCache.clear();
    }
}
