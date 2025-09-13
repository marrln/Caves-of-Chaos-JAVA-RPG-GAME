package graphics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Manages loading and caching of game assets from assets.xml.
 * Provides efficient access to tile, enemy, player, and item graphics.
 */
public class AssetManager {
    private static AssetManager instance;
    private final Map<String, String> assetPaths;
    private final Map<String, BufferedImage> imageCache;
    private final Set<String> missingAssets; // Track missing assets to avoid spam logging
    
    private AssetManager() {
        assetPaths = new HashMap<>();
        imageCache = new HashMap<>();
        missingAssets = new HashSet<>();
        loadAssetPaths();
    }
    
    /**
     * Gets the singleton instance of AssetManager.
     */
    public static synchronized AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }
    
    /**
     * Loads asset paths from assets.xml file.
     */
    private void loadAssetPaths() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // Try to load from src/config/assets.xml
            File assetsFile = new File("src/config/assets.xml");
            if (!assetsFile.exists()) {
                System.err.println("Assets file not found: " + assetsFile.getAbsolutePath());
                return;
            }
            
            Document doc = builder.parse(assetsFile);
            doc.getDocumentElement().normalize();
            
            // Load tile assets
            loadTileAssets(doc);
            // Load other asset types as needed
            loadEnemyAssets(doc);
            loadPlayerAssets(doc);
            loadItemAssets(doc);
            
            System.out.println("Loaded " + assetPaths.size() + " asset paths from assets.xml");
            
        } catch (Exception e) {
            System.err.println("Error loading assets.xml: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads tile asset paths from the XML document.
     */
    private void loadTileAssets(Document doc) {
        NodeList tileNodes = doc.getElementsByTagName("tiles");
        if (tileNodes.getLength() > 0) {
            Element tilesElement = (Element) tileNodes.item(0);
            NodeList spriteNodes = tilesElement.getElementsByTagName("sprite");
            
            for (int i = 0; i < spriteNodes.getLength(); i++) {
                Element sprite = (Element) spriteNodes.item(i);
                String id = sprite.getAttribute("id");
                String path = sprite.getAttribute("path");
                
                // Add "src/" prefix to paths that don't already have it
                if (!path.startsWith("src/")) {
                    path = "src/" + path;
                }
                
                assetPaths.put(id, path);
            }
        }
    }
    
    /**
     * Loads enemy asset paths from the XML document.
     */
    private void loadEnemyAssets(Document doc) {
        NodeList enemyNodes = doc.getElementsByTagName("enemies");
        if (enemyNodes.getLength() > 0) {
            Element enemiesElement = (Element) enemyNodes.item(0);
            NodeList spriteNodes = enemiesElement.getElementsByTagName("sprite");
            
            for (int i = 0; i < spriteNodes.getLength(); i++) {
                Element sprite = (Element) spriteNodes.item(i);
                String id = sprite.getAttribute("id");
                String path = sprite.getAttribute("path");
                
                // Ensure path has "src/" prefix
                if (!path.startsWith("src/")) {
                    path = "src/" + path;
                }
                
                assetPaths.put(id, path);
            }
        }
    }
    
    /**
     * Loads player asset paths from the XML document.
     */
    private void loadPlayerAssets(Document doc) {
        NodeList playerNodes = doc.getElementsByTagName("player");
        if (playerNodes.getLength() > 0) {
            Element playerElement = (Element) playerNodes.item(0);
            NodeList spriteNodes = playerElement.getElementsByTagName("sprite");
            
            for (int i = 0; i < spriteNodes.getLength(); i++) {
                Element sprite = (Element) spriteNodes.item(i);
                String id = sprite.getAttribute("id");
                String path = sprite.getAttribute("path");
                
                // Ensure path has "src/" prefix
                if (!path.startsWith("src/")) {
                    path = "src/" + path;
                }
                
                assetPaths.put(id, path);
            }
        }
    }
    
    /**
     * Loads item asset paths from the XML document.
     */
    private void loadItemAssets(Document doc) {
        NodeList itemNodes = doc.getElementsByTagName("items");
        if (itemNodes.getLength() > 0) {
            Element itemsElement = (Element) itemNodes.item(0);
            // Load consumables
            NodeList consumableNodes = itemsElement.getElementsByTagName("consumables");
            if (consumableNodes.getLength() > 0) {
                Element consumablesElement = (Element) consumableNodes.item(0);
                NodeList spriteNodes = consumablesElement.getElementsByTagName("sprite");
                
                for (int i = 0; i < spriteNodes.getLength(); i++) {
                    Element sprite = (Element) spriteNodes.item(i);
                    String id = sprite.getAttribute("id");
                    String path = sprite.getAttribute("path");
                    
                    if (!path.startsWith("src/")) {
                        path = "src/" + path;
                    }
                    
                    assetPaths.put(id, path);
                }
            }
            
            // Load projectiles
            NodeList projectileNodes = itemsElement.getElementsByTagName("projectiles");
            if (projectileNodes.getLength() > 0) {
                Element projectilesElement = (Element) projectileNodes.item(0);
                NodeList spriteNodes = projectilesElement.getElementsByTagName("sprite");
                
                for (int i = 0; i < spriteNodes.getLength(); i++) {
                    Element sprite = (Element) spriteNodes.item(i);
                    String id = sprite.getAttribute("id");
                    String path = sprite.getAttribute("path");
                    
                    if (!path.startsWith("src/")) {
                        path = "src/" + path;
                    }
                    
                    assetPaths.put(id, path);
                }
            }
        }
    }
    
    /**
     * Gets the asset path for the given asset ID.
     */
    public String getAssetPath(String assetId) {
        return assetPaths.get(assetId);
    }
    
    /**
     * Checks if an asset exists for the given ID.
     */
    public boolean hasAsset(String assetId) {
        return assetPaths.containsKey(assetId);
    }
    
    /**
     * Gets asset path with validation and error reporting for music assets.
     * This method handles all loading concerns including missing files and validates file existence.
     */
    public String getMusicAssetPath(String assetId) {
        String path = assetPaths.get(assetId);
        
        if (path == null) {
            System.err.println("Warning: Music asset '" + assetId + "' not found in assets.xml configuration");
            return null;
        }
        
        File musicFile = new File(path);
        if (!musicFile.exists()) {
            System.err.println("Warning: Music file not found at configured path: " + path);
            return null;
        }
        
        return path;
    }
    
    /**
     * Loads and caches an image for the given asset ID.
     * Returns null if the asset doesn't exist or fails to load.
     */
    public BufferedImage loadImage(String assetId) {
        // Check cache first
        if (imageCache.containsKey(assetId)) {
            return imageCache.get(assetId);
        }
        
        // Check if we already know this asset is missing
        if (missingAssets.contains(assetId)) {
            return null;
        }
        
        String path = assetPaths.get(assetId);
        if (path == null) {
            // Asset not defined in XML, add to missing set to avoid repeated lookups
            missingAssets.add(assetId);
            return null;
        }
        
        try {
            File imageFile = new File(path);
            if (!imageFile.exists()) {
                System.err.println("Asset file not found: " + path + " (ID: " + assetId + ")");
                missingAssets.add(assetId);
                return null;
            }
            
            BufferedImage image = ImageIO.read(imageFile);
            if (image != null) {
                // Cache the loaded image
                imageCache.put(assetId, image);
                System.out.println("Loaded asset: " + assetId + " from " + path);
            } else {
                System.err.println("Failed to load image from: " + path + " (ID: " + assetId + ")");
                missingAssets.add(assetId);
            }
            
            return image;
            
        } catch (Exception e) {
            System.err.println("Error loading asset " + assetId + " from " + path + ": " + e.getMessage());
            missingAssets.add(assetId);
            return null;
        }
    }
    
    /**
     * Preloads all tile assets for better performance.
     */
    public void preloadTileAssets() {
        String[] tileAssets = {"floor", "wall", "stairs_up", "stairs_down", "spawn_point", "floor_with_item"};
        for (String assetId : tileAssets) {
            loadImage(assetId);
        }
    }
    
    /**
     * Clears the image cache to free memory.
     */
    public void clearCache() {
        imageCache.clear();
        System.out.println("Asset cache cleared");
    }
    
    /**
     * Gets cache statistics for debugging.
     */
    public String getCacheStats() {
        return String.format("AssetManager: %d paths loaded, %d images cached, %d missing assets", 
                           assetPaths.size(), imageCache.size(), missingAssets.size());
    }
}
