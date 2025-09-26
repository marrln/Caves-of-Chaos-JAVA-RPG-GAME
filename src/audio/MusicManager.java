package audio;

import enemies.AbstractEnemy;
import enemies.Enemy;
import graphics.AssetManager;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.sound.sampled.*;

/**
 * Music manager for background playback.
 * Handles exploration and combat tracks with instant transitions.
 */
public class MusicManager {

    // ====== SINGLETON ======
    private static MusicManager instance;

    public static synchronized MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    // ====== ASSET IDs ======
    private static final String EXPLORATION_TRACK_ID = "regular_music";
    private static final String COMBAT_TRACK_ID = "battle_music";

    // ====== MUSIC CLIPS ======
    private Clip explorationClip;
    private Clip combatClip;
    private Clip currentClip;

    // ====== SETTINGS ======
    private boolean isMusicEnabled = true;

    // ====== COMBAT STATE TRACKING ======
    private boolean wasCombatMusicPlaying = false;

    private MusicManager() {
        initializeMusic();
    }

    // ====== INITIALIZATION ======
    private void initializeMusic() {
        try {
            AudioSystem.getMixerInfo(); // Verify audio system available
        } catch (Exception e) {
            System.err.println("Warning: Audio system not available - music disabled");
            isMusicEnabled = false;
            return;
        }

        AssetManager assetManager = AssetManager.getInstance();

        String explorationPath = assetManager.getMusicAssetPath(EXPLORATION_TRACK_ID);
        String combatPath = assetManager.getMusicAssetPath(COMBAT_TRACK_ID);

        explorationClip = loadClip(explorationPath);
        combatClip = loadClip(combatPath);
    }

    private Clip loadClip(String filePath) {

        try {
            AudioInputStream originalStream = AudioSystem.getAudioInputStream(new File(filePath));
            AudioFormat originalFormat = originalStream.getFormat();

            AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                originalFormat.getSampleRate(),
                16,
                originalFormat.getChannels(),
                originalFormat.getChannels() * 2,
                originalFormat.getSampleRate(),
                false
            );

            AudioInputStream audioStream = AudioSystem.isConversionSupported(targetFormat, originalFormat)
                ? AudioSystem.getAudioInputStream(targetFormat, originalStream)
                : originalStream;

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            return clip;

        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            return null; // Failed to load clip
        }
    }

    // ====== MUSIC CONTROL ======
    public void startExplorationMusic() {
        if (!isMusicEnabled) return;
        playClip(explorationClip);
    }

    public void startCombatMusic() {
        if (!isMusicEnabled) return;
        playClip(combatClip);
    }

    public void endCombatMusic() {
        if (!isMusicEnabled) return;
        playClip(explorationClip);
    }

    private void playClip(Clip clip) {
        if (clip == null) return;

        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
        }

        currentClip = clip;
        currentClip.setFramePosition(0);
        currentClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stopMusic() {
        if (currentClip != null && currentClip.isRunning()) {
            currentClip.stop();
        }
    }

    // ====== SETTINGS & STATE ======
    public void setMusicEnabled(boolean enabled) {
        isMusicEnabled = enabled;
        if (!enabled) stopMusic();
    }

    public boolean isMusicEnabled() {
        return isMusicEnabled;
    }

    // ====== CLEANUP ======
    public void cleanup() {
        stopMusic();
        if (explorationClip != null) explorationClip.close();
        if (combatClip != null) combatClip.close();
    }

    // ====== COMBAT STATE HANDLING ======

    public void updateForCombatState(List<Enemy> enemies) {
        boolean anyNoticed = false;
        for (Enemy enemy : enemies) {
            if (enemy instanceof AbstractEnemy ae && ae.hasNoticedPlayer() && !enemy.isDead()) {
                anyNoticed = true;
                break;
            }
        }
        if (anyNoticed && !wasCombatMusicPlaying) {
            wasCombatMusicPlaying = true;
            startCombatMusic();
        } else if (!anyNoticed && wasCombatMusicPlaying) {
            wasCombatMusicPlaying = false;
            endCombatMusic();
        }
    }
}
