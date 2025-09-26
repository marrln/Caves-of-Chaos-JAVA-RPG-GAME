package graphics;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SpriteSheetLoader {
    private static final int DEFAULT_FRAME_WIDTH = 100;
    private static final int DEFAULT_FRAME_HEIGHT = 100;

    private final AssetManager assetManager = AssetManager.getInstance();

    public BufferedImage[] loadFrames(String assetId) {
        BufferedImage sheet = assetManager.loadImage(assetId);
        if (sheet == null) return new BufferedImage[0];

        Integer frameCount = assetManager.getFrameCount(assetId);
        int frameWidth = DEFAULT_FRAME_WIDTH;
        int frameHeight = DEFAULT_FRAME_HEIGHT;

        if (frameCount == null) {
            frameCount = sheet.getWidth() / frameWidth;
        }

        List<BufferedImage> frames = new ArrayList<>(frameCount);
        for (int i = 0; i < frameCount; i++) {
            try {
                frames.add(sheet.getSubimage(
                    i * frameWidth,
                    0,
                    frameWidth,
                    frameHeight
                ));
            } catch (Exception e) {
                System.err.println("Error slicing frame " + i + " from " + assetId + ": " + e.getMessage());
            }
        }

        return frames.toArray(BufferedImage[]::new);
    }
}
