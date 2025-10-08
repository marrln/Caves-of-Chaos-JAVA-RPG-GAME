package graphics;

import config.Config;
import core.Projectile;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import map.FogOfWar;

public class ProjectileRenderer {
	private static final boolean USE_GRAPHICS = Config.getBoolSetting("use_graphics");
	private static final SpriteSheetLoader sheetLoader = new SpriteSheetLoader();
	private static final Map<String, BufferedImage[]> spriteCache = new HashMap<>();
	private static final int FRAME_DURATION = 120; // ms per frame
	private static final Map<String, Integer> frameIndices = new HashMap<>();
	private static final Map<String, Long> lastFrameTimes = new HashMap<>();

	public static void renderProjectiles(Graphics2D g2d, List<Projectile> projectiles, int tileSize, int cameraOffsetX, int cameraOffsetY, double scale, FogOfWar fogOfWar) {
		for (Projectile projectile : projectiles) {
			// Check visibility in fog of war - skip if not visible
			float fogAlpha = 1.0f;
			boolean visible = true;
			if (fogOfWar != null) {
				fogAlpha = fogOfWar.getVisibilityStrength((int) projectile.getX(), (int) projectile.getY());
				visible = fogAlpha > 0.05f; // Only skip if fully hidden
			}
			if (!visible) {
				continue;
			}
			
			int screenX = (int) ((projectile.getX() * tileSize) - cameraOffsetX);
			int screenY = (int) ((projectile.getY() * tileSize) - cameraOffsetY);
			int scaledTile = (int)(tileSize * scale);

			if (!USE_GRAPHICS) {
				// Fallback: colored dot with fog alpha
				Composite original = g2d.getComposite();
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fogAlpha));
				
				switch (projectile.getType()) {
					case FIRE_SPELL -> g2d.setColor(Color.RED);
					case ICE_SPELL -> g2d.setColor(Color.CYAN);
					default -> g2d.setColor(Color.YELLOW);
				}
				int dotSize = Math.max(4, tileSize / 4);
				int dotX = screenX + (tileSize - dotSize) / 2;
				int dotY = screenY + (tileSize - dotSize) / 2;
				g2d.fillOval(dotX, dotY, dotSize, dotSize);
				g2d.setColor(Color.WHITE);
				g2d.drawOval(dotX, dotY, dotSize, dotSize);
				
				g2d.setComposite(original);
				continue;
			}

			// Sprite rendering
			String spriteId = projectile.getType().getSpriteId();
			BufferedImage[] frames = spriteCache.computeIfAbsent(spriteId, id -> {
				BufferedImage[] originalFrames = sheetLoader.loadFrames(id);
				BufferedImage[] scaledFrames = new BufferedImage[originalFrames.length];
				for (int i = 0; i < originalFrames.length; i++) {
					scaledFrames[i] = new BufferedImage(scaledTile, scaledTile, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = scaledFrames[i].createGraphics();
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					g.drawImage(originalFrames[i], 0, 0, scaledTile, scaledTile, null);
					g.dispose();
				}
				return scaledFrames;
			});
			if (frames.length == 0) continue;

			// Animation frame management per projectile type
			int frameIdx = frameIndices.getOrDefault(spriteId, 0);
			long now = System.currentTimeMillis();
			long lastTime = lastFrameTimes.getOrDefault(spriteId, 0L);
			if (now - lastTime > FRAME_DURATION) {
				frameIdx = (frameIdx + 1) % frames.length;
				frameIndices.put(spriteId, frameIdx);
				lastFrameTimes.put(spriteId, now);
			}
			BufferedImage frame = frames[frameIdx];

			int drawX = screenX + (tileSize - scaledTile) / 2;
			int drawY = screenY + (tileSize - scaledTile) / 2;
			
			// Apply fog alpha when drawing
			Composite original = g2d.getComposite();
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fogAlpha));
			g2d.drawImage(frame, drawX, drawY, scaledTile, scaledTile, null);
			g2d.setComposite(original);
		}
	}
}
