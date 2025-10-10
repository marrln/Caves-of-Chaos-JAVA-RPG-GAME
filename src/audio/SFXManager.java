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

    public void playDuelistHit() {
        playSound("duelist_hit");
    }
    
    public void playDuelistMiss() {
        playSound("duelist_miss");
    }
    
    public void playPlayerHurt() {
        playSound("player_hurt");
    }

    public void playPlayerDeath() {
        playSound("player_death");
    }

    public void playPlayerWalk() {
        playRandomSound("player_walk_1", "player_walk_2", "player_walk_3");
    }
    
    // ====== SPELL SOUNDS ======

    public void playSpellCast() {
        playSound("spell_sound");
    }
    
    // ====== ENEMY SOUNDS ======

    public void playEnemyAttackGeneric() {
        playSound("enemy_attack_generic");
    }
    
    public void playEnemyAttackSword1() {
        playSound("enemy_attack_sword_1");
    }

    public void playEnemyAttackSword2() {
        playSound("enemy_attack_sword_2");
    }

    public void playEnemyAttackSword() {
        playRandomSound("enemy_attack_sword_1", "enemy_attack_sword_2");
    }
    
    public void playEnemyHurt() {
        playRandomSound("enemy_hurt_1", "enemy_hurt_2", "enemy_hurt_3");
    }

    public void playEnemyDeath() {
        playSound("enemy_death");
    }

    public void playEnemyWalk() {
        playRandomSound("enemy_walk_1", "enemy_walk_2", "enemy_walk_3");
    }
    
    // ====== ITEM SOUNDS ======

    public void playItemPickup() {
        playRandomSound("item_pickup_1", "item_pickup_2");
    }
    
    // ====== VOLUME & SETTINGS ======
    
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
