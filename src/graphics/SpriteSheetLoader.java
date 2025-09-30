package graphics;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpriteSheetLoader {
    private static final int DEFAULT_FRAME_WIDTH = 100;
    private static final int DEFAULT_FRAME_HEIGHT = 100;
    private final AssetManager assetManager = AssetManager.getInstance();

    // cache: key → frames (works for raw + scaled)
    private final Map<String, BufferedImage[]> frameCache = new ConcurrentHashMap<>();

    /**
     * Load frames from a sprite sheet.
     * @param assetId asset key (like "orc_attack01")
     * @return array of unscaled frames
     */
    public BufferedImage[] loadFrames(String assetId) {
        return frameCache.computeIfAbsent(assetId, this::loadUncached);
    }

    /**
     * Load frames and return scaled copies.
     * Frames are cached by (assetId + tileSize).
     */
    public BufferedImage[] loadFrames(String assetId, int tileSize) {
        String scaledKey = assetId + "_scaled_" + tileSize;
        return frameCache.computeIfAbsent(scaledKey, key -> {
            BufferedImage[] rawFrames = loadFrames(assetId);
            BufferedImage[] scaled = new BufferedImage[rawFrames.length];
            for (int i = 0; i < rawFrames.length; i++) {
                scaled[i] = scaleFrame(rawFrames[i], tileSize, tileSize);
            }
            return scaled;
        });
    }

    // === Internals ===
    private BufferedImage[] loadUncached(String assetId) {
        BufferedImage sheet = assetManager.loadImage(assetId);
        if (sheet == null) return new BufferedImage[0];

        Integer frameCount = assetManager.getFrameCount(assetId);

        List<BufferedImage> frames = (frameCount != null && frameCount > 0)
                ? sliceByCount(sheet, assetId, frameCount)
                : sliceByDefault(sheet, assetId);

        return frames.toArray(BufferedImage[]::new);
    }

    private List<BufferedImage> sliceByCount(BufferedImage sheet, String assetId, int frameCount) {
        List<BufferedImage> frames = new ArrayList<>();
        int frameWidth = sheet.getWidth() / frameCount;
        int frameHeight = sheet.getHeight();

        for (int i = 0; i < frameCount; i++) {
            try {
                int x = i * frameWidth;
                int w = (i == frameCount - 1) ? sheet.getWidth() - x : frameWidth;
                frames.add(sheet.getSubimage(x, 0, w, frameHeight));
            } catch (Exception e) {
                logSliceError(assetId, i, e);
            }
        }
        return frames;
    }

    private List<BufferedImage> sliceByDefault(BufferedImage sheet, String assetId) {
        List<BufferedImage> frames = new ArrayList<>();
        int frameWidth = DEFAULT_FRAME_WIDTH;
        int frameHeight = DEFAULT_FRAME_HEIGHT;

        int columns = sheet.getWidth() / frameWidth;
        int rows = sheet.getHeight() / frameHeight;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                try {
                    frames.add(sheet.getSubimage(
                            x * frameWidth, y * frameHeight, frameWidth, frameHeight));
                } catch (Exception e) {
                    logSliceError(assetId, x + y * columns, e);
                }
            }
        }
        return frames;
    }

    private BufferedImage scaleFrame(BufferedImage src, int width, int height) {
        BufferedImage buf = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buf.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, width, height, null);
        g.dispose();
        return buf;
    }

    private void logSliceError(String assetId, int index, Exception e) {
        System.err.printf("Error slicing frame %d from %s: %s%n", index, assetId, e.getMessage());
    }

}
