package graphics;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import config.Config;

public class Sprite {
    private BufferedImage spriteSheet;
    private BufferedImage[] frames;
    private final int frameCount;
    private final int frameWidth;
    private final int frameHeight;

    private final int TILE_SIZE = Config.getIntSetting("tile_size");

    public Sprite(String path, int frameCount, int frameWidth, int frameHeight) {
        this.frameCount = frameCount;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        loadSpriteSheet(path);
        loadFrames();
    }

    private void loadSpriteSheet(String path) {
        try {
            spriteSheet = ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load sprite sheet: " + path);
        }
    }

    private void loadFrames() {
        frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            // Extract the original frame
            BufferedImage origFrame = spriteSheet.getSubimage(
                    i * frameWidth,
                    0,
                    frameWidth,
                    frameHeight
            );

            // Scale to tile size
            BufferedImage scaledFrame = new BufferedImage(TILE_SIZE, TILE_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = scaledFrame.createGraphics();
            g2d.drawImage(origFrame, 0, 0, TILE_SIZE, TILE_SIZE, null);
            g2d.dispose();

            frames[i] = scaledFrame;
        }
    }

    public BufferedImage getFrame(int index) {
        return frames[index % frameCount]; // Safe wrap-around
    }

    public int getFrameCount() {
        return frameCount;
    }

    // Example update loop: cycle through frames
    public BufferedImage getAnimationFrame(long time, int frameSpeedMs) {
        int index = (int) ((time / frameSpeedMs) % frameCount);
        return getFrame(index);
    }

    public BufferedImage[] getScaledFrames(int tileSize) {
    BufferedImage[] scaled = new BufferedImage[frameCount];
    for (int i = 0; i < frameCount; i++) {
        BufferedImage frame = getFrame(i);
        BufferedImage scaledFrame = new BufferedImage(tileSize, tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaledFrame.createGraphics();
        g.drawImage(frame, 0, 0, tileSize, tileSize, null);
        g.dispose();
        scaled[i] = scaledFrame;
    }
    return scaled;
}

}
