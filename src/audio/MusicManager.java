package audio;

import enemies.AbstractEnemy;
import enemies.Enemy;
import graphics.AssetManager;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.sound.sampled.*;

public class MusicManager {

    private static MusicManager instance;
    private static final String EXPLORATION_TRACK_ID = "regular_music";
    private static final String COMBAT_TRACK_ID = "battle_music";

    private Clip explorationClip, combatClip, currentClip;
    private boolean isMusicEnabled = true, wasCombatMusicPlaying = false;

    private MusicManager() { initializeMusic(); }

    public static synchronized MusicManager getInstance() {
        return instance == null ? (instance = new MusicManager()) : instance;
    }

    private void initializeMusic() {
        try { AudioSystem.getMixerInfo(); }
        catch (Exception e) {
            System.err.println("Warning: Audio system not available - music disabled");
            isMusicEnabled = false; return;
        }

        AssetManager am = AssetManager.getInstance();
        explorationClip = loadClip(am.getMusicAssetPath(EXPLORATION_TRACK_ID));
        combatClip = loadClip(am.getMusicAssetPath(COMBAT_TRACK_ID));
    }

    private Clip loadClip(String filePath) {
        try (AudioInputStream original = AudioSystem.getAudioInputStream(new File(filePath))) {
            AudioFormat f = original.getFormat();
            AudioFormat target = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, f.getSampleRate(), 16, f.getChannels(),
                                                 f.getChannels() * 2, f.getSampleRate(), false);
            AudioInputStream stream = AudioSystem.isConversionSupported(target, f)
                    ? AudioSystem.getAudioInputStream(target, original) : original;
            Clip c = AudioSystem.getClip(); c.open(stream); return c;
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            return null;
        }
    }

    public void startExplorationMusic() { if (isMusicEnabled) playClip(explorationClip); }
    public void startCombatMusic() { if (isMusicEnabled) playClip(combatClip); }
    public void endCombatMusic() { if (isMusicEnabled) playClip(explorationClip); }

    private void playClip(Clip clip) {
        if (clip == null) return;
        if (currentClip != null && currentClip.isRunning()) currentClip.stop();
        currentClip = clip; currentClip.setFramePosition(0); currentClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stopMusic() { if (currentClip != null && currentClip.isRunning()) currentClip.stop(); }

    public void setMusicEnabled(boolean enabled) {
        isMusicEnabled = enabled;
        if (!enabled) stopMusic();
        else if (wasCombatMusicPlaying) startCombatMusic(); else startExplorationMusic();
    }

    public void toggleMusic() { setMusicEnabled(!isMusicEnabled); }
    public boolean isMusicEnabled() { return isMusicEnabled; }

    public void cleanup() {
        stopMusic();
        if (explorationClip != null) explorationClip.close();
        if (combatClip != null) combatClip.close();
    }

    public void updateForCombatState(List<Enemy> enemies) {
        boolean anyNoticed = enemies.stream()
                .anyMatch(e -> e instanceof AbstractEnemy ae && ae.hasNoticedPlayer() && !e.isDead());
        if (anyNoticed && !wasCombatMusicPlaying) { wasCombatMusicPlaying = true; startCombatMusic(); }
        else if (!anyNoticed && wasCombatMusicPlaying) { wasCombatMusicPlaying = false; endCombatMusic(); }
    }
}
