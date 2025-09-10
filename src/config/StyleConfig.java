package config;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Configuration utility for loading and accessing styling settings (colors, fonts, etc.)
 * Follows the same pattern as Config.java but specifically for styling.xml
 */
public class StyleConfig {
    private static final Map<String, String> colorValues = new HashMap<>();
    private static final Map<String, Color> colorCache = new HashMap<>();
    private static final Map<String, FontInfo> fontValues = new HashMap<>();
    private static final Map<String, Font> fontCache = new HashMap<>();
    
    /**
     * Inner class to hold font information
     */
    private static class FontInfo {
        final String family;
        final int size;
        final int style;
        
        FontInfo(String family, int size, int style) {
            this.family = family;
            this.size = size;
            this.style = style;
        }
    }
    
    /**
     * Loads styling configuration from an XML file.
     * 
     * @param stylingPath Path to styling XML file
     */
    public static void loadStyling(String stylingPath) {
        try {
            File file = new File(stylingPath);
            if (!file.exists()) {
                System.err.println("Styling file not found: " + stylingPath);
                return;
            }
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();
            
            // Load colors
            NodeList colorList = document.getElementsByTagName("color");
            for (int i = 0; i < colorList.getLength(); i++) {
                Element colorElement = (Element) colorList.item(i);
                String name = colorElement.getAttribute("name");
                String value = colorElement.getTextContent().trim();
                colorValues.put(name, value);
            }
            
            // Load fonts
            NodeList fontList = document.getElementsByTagName("font");
            for (int i = 0; i < fontList.getLength(); i++) {
                Element fontElement = (Element) fontList.item(i);
                String name = fontElement.getAttribute("name");
                String family = fontElement.getAttribute("family");
                int size = Integer.parseInt(fontElement.getAttribute("size"));
                String styleStr = fontElement.getAttribute("style").toLowerCase();
                
                int style = Font.PLAIN;
                if ("bold".equals(styleStr)) {
                    style = Font.BOLD;
                } else if ("italic".equals(styleStr)) {
                    style = Font.ITALIC;
                } else if ("bold-italic".equals(styleStr) || "bolditalic".equals(styleStr)) {
                    style = Font.BOLD | Font.ITALIC;
                }
                
                fontValues.put(name, new FontInfo(family, size, style));
            }
            
            System.out.println("Loaded " + colorValues.size() + " color definitions and " + 
                             fontValues.size() + " font definitions from styling config");
            
        } catch (Exception e) {
            System.err.println("Error loading styling configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets a color by name, with caching for performance.
     * 
     * @param colorName The color name from styling.xml
     * @return The Color object, or a default color if not found
     */
    public static Color getColor(String colorName) {
        return getColor(colorName, Color.MAGENTA); // Default to magenta for missing colors
    }
    
    /**
     * Gets a color by name with a fallback default.
     * 
     * @param colorName The color name from styling.xml
     * @param defaultColor The color to return if the named color is not found
     * @return The Color object
     */
    public static Color getColor(String colorName, Color defaultColor) {
        // Check cache first
        if (colorCache.containsKey(colorName)) {
            return colorCache.get(colorName);
        }
        
        // Get hex value from config
        String hexValue = colorValues.get(colorName);
        if (hexValue == null) {
            System.err.println("Color not found in styling config: " + colorName);
            colorCache.put(colorName, defaultColor);
            return defaultColor;
        }
        
        try {
            Color color = parseHexColor(hexValue);
            colorCache.put(colorName, color);
            return color;
        } catch (Exception e) {
            System.err.println("Invalid color format for '" + colorName + "': " + hexValue);
            colorCache.put(colorName, defaultColor);
            return defaultColor;
        }
    }
    
    /**
     * Parses a hex color string (with or without alpha) into a Color object.
     * Supports formats: #RGB, #RRGGBB, #RRGGBBAA
     * 
     * @param hexColor The hex color string
     * @return The Color object
     * @throws IllegalArgumentException if the format is invalid
     */
    private static Color parseHexColor(String hexColor) {
        if (!hexColor.startsWith("#")) {
            throw new IllegalArgumentException("Color must start with #");
        }
        
        String hex = hexColor.substring(1);
        
        switch (hex.length()) {
            case 6: // #RRGGBB
                return new Color(Integer.parseInt(hex, 16));
            case 8: // #RRGGBBAA
                long value = Long.parseLong(hex, 16);
                int r = (int) ((value >> 24) & 0xFF);
                int g = (int) ((value >> 16) & 0xFF);
                int b = (int) ((value >> 8) & 0xFF);
                int a = (int) (value & 0xFF);
                return new Color(r, g, b, a);
            default:
                throw new IllegalArgumentException("Invalid hex color format: " + hexColor);
        }
    }
    
    /**
     * Gets a font by name, with caching for performance.
     * 
     * @param fontName The font name from styling.xml
     * @return The Font object, or a default font if not found
     */
    public static Font getFont(String fontName) {
        return getFont(fontName, new Font("SansSerif", Font.PLAIN, 12)); // Default font
    }
    
    /**
     * Gets a font by name with a fallback default.
     * 
     * @param fontName The font name from styling.xml
     * @param defaultFont The font to return if the named font is not found
     * @return The Font object
     */
    public static Font getFont(String fontName, Font defaultFont) {
        // Check cache first
        if (fontCache.containsKey(fontName)) {
            return fontCache.get(fontName);
        }
        
        // Get font info from config
        FontInfo fontInfo = fontValues.get(fontName);
        if (fontInfo == null) {
            System.err.println("Font not found in styling config: " + fontName);
            fontCache.put(fontName, defaultFont);
            return defaultFont;
        }
        
        try {
            Font font = new Font(fontInfo.family, fontInfo.style, fontInfo.size);
            fontCache.put(fontName, font);
            return font;
        } catch (Exception e) {
            System.err.println("Error creating font '" + fontName + "': " + e.getMessage());
            fontCache.put(fontName, defaultFont);
            return defaultFont;
        }
    }
    
    /**
     * Clears the font cache. Useful if styling needs to be reloaded.
     */
    public static void clearFontCache() {
        fontCache.clear();
    }
    
    /**
     * Clears all caches. Useful if styling needs to be reloaded.
     */
    public static void clearAllCaches() {
        colorCache.clear();
        fontCache.clear();
    }
    
    /**
     * Gets all available color names for debugging.
     * 
     * @return Array of color names
     */
    public static String[] getAvailableColorNames() {
        return colorValues.keySet().toArray(new String[0]);
    }
}
