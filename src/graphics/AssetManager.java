package graphics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

/**
 * Singleton manager for loading and caching game assets.
 * Provides access to tile, enemy, player, item, and music assets.
 */
public class AssetManager {
    private static AssetManager instance;

    // Asset metadata
    private static class AssetInfo {
        final String path;
        final Integer frameCount; // null = auto-calculate

        AssetInfo(String path, Integer frameCount) {
            this.path = path;
            this.frameCount = frameCount;
        }
    }

    private final Map<String, AssetInfo> assetInfos = new HashMap<>();
    private final Map<String, BufferedImage> imageCache = new HashMap<>();
    private final Set<String> missingAssets = new HashSet<>();

    private AssetManager() {
        loadAssetPaths();
    }

    public static synchronized AssetManager getInstance() {
        if (instance == null) instance = new AssetManager();
        return instance;
    }

    private void loadAssetPaths() {
        try {
            File assetsFile = new File("bin/config/assets.xml");
            if (!assetsFile.exists()) assetsFile = new File("src/config/assets.xml");
            if (!assetsFile.exists()) {
                System.err.println("Assets file not found in bin/config or src/config");
                return;
            }

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(assetsFile);
            doc.getDocumentElement().normalize();

            loadSprites(doc, "tiles");
            loadSprites(doc, "enemies");
            loadSprites(doc, "player");
            loadSprites(doc, "items", "consumables");
            loadSprites(doc, "items", "projectiles");
            loadMusic(doc);

            System.out.println("Loaded " + assetInfos.size() + " assets from assets.xml");
        } catch (Exception e) {
            System.err.println("Error loading assets.xml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSprites(Document doc, String parentTag) {
        NodeList nodes = doc.getElementsByTagName(parentTag);
        if (nodes.getLength() == 0) return;

        Element parent = (Element) nodes.item(0);
        NodeList spriteNodes = parent.getElementsByTagName("sprite");

        for (int i = 0; i < spriteNodes.getLength(); i++) {
            parseSpriteElement((Element) spriteNodes.item(i));
        }
    }

    private void loadSprites(Document doc, String parentTag, String childTag) {
        NodeList parentNodes = doc.getElementsByTagName(parentTag);
        if (parentNodes.getLength() == 0) return;

        Element parent = (Element) parentNodes.item(0);
        NodeList childNodes = parent.getElementsByTagName(childTag);
        if (childNodes.getLength() == 0) return;

        Element child = (Element) childNodes.item(0);
        NodeList spriteNodes = child.getElementsByTagName("sprite");

        for (int i = 0; i < spriteNodes.getLength(); i++) {
            parseSpriteElement((Element) spriteNodes.item(i));
        }
    }

    private void parseSpriteElement(Element sprite) {
        String id = sprite.getAttribute("id");
        String path = sprite.getAttribute("path");
        if (!path.startsWith("src/")) path = "src/" + path;

        // Optional frames attribute (e.g., Medusa of Chaos)
        String framesAttr = sprite.getAttribute("frames");
        Integer frameCount = framesAttr.isEmpty() ? null : Integer.parseInt(framesAttr);

        assetInfos.put(id, new AssetInfo(path, frameCount));
    }

    private void loadMusic(Document doc) {
        NodeList nodes = doc.getElementsByTagName("music");
        if (nodes.getLength() == 0) return;

        Element musicElement = (Element) nodes.item(0);
        NodeList trackNodes = musicElement.getElementsByTagName("track");

        for (int i = 0; i < trackNodes.getLength(); i++) {
            Element track = (Element) trackNodes.item(i);
            String id = track.getAttribute("id");
            String path = track.getAttribute("path");

            if (!path.startsWith("src/") && !path.startsWith("bin/")) {
                path = new File("bin/config/assets.xml").exists() ? "bin/" + path : "src/" + path;
            }
            assetInfos.put(id, new AssetInfo(path, null));
        }
    }

    public String getAssetPath(String assetId) {
        AssetInfo info = assetInfos.get(assetId);
        return info != null ? info.path : null;
    }

    public Integer getFrameCount(String assetId) {
        AssetInfo info = assetInfos.get(assetId);
        return info != null ? info.frameCount : null;
    }

    public boolean hasAsset(String assetId) {
        return assetInfos.containsKey(assetId);
    }

    public BufferedImage loadImage(String assetId) {
        if (imageCache.containsKey(assetId)) return imageCache.get(assetId);
        if (missingAssets.contains(assetId)) return null;

        AssetInfo info = assetInfos.get(assetId);
        if (info == null) {
            missingAssets.add(assetId);
            return null;
        }

        try {
            File file = new File(info.path);
            if (!file.exists()) {
                System.err.println("Missing asset: " + info.path + " (ID: " + assetId + ")");
                missingAssets.add(assetId);
                return null;
            }

            BufferedImage image = ImageIO.read(file);
            if (image != null) {
                imageCache.put(assetId, image);
                return image;
            } else {
                System.err.println("Failed to load image: " + info.path + " (ID: " + assetId + ")");
            }
        } catch (Exception e) {
            System.err.println("Error loading asset " + assetId + " (" + info.path + "): " + e.getMessage());
        }

        missingAssets.add(assetId);
        return null;
    }

    // For compatibility with MusicManager
    public String getMusicAssetPath(String trackId) {
        AssetInfo info = assetInfos.get(trackId);
        if (info != null) return info.path;
        System.err.println("Music asset not found: " + trackId);
        return null;
    }

    // For compatibility with TileRenderer
    public void preloadTileAssets() {
        // Preload only assets that look like tiles (e.g., start with "tile_")
        for (String id : assetInfos.keySet()) {
            if (id.startsWith("tile_")) {
                loadImage(id);
            }
        }
        System.out.println("Preloaded tile assets");
    }


    public void clearCache() {
        imageCache.clear();
        System.out.println("Asset cache cleared");
    }

    public String getCacheStats() {
        return String.format(
            "AssetManager: %d paths, %d cached, %d missing",
            assetInfos.size(), imageCache.size(), missingAssets.size()
        );
    }
}
