package config;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Utility for loading and accessing styling settings (colors, fonts, textures, effects, typography).
 */
public class StyleConfig {

    private static final Map<String, String> colorValues = new HashMap<>();
    private static final Map<String, Color> colorCache = new HashMap<>();
    private static final Map<String, FontInfo> fontValues = new HashMap<>();
    private static final Map<String, Font> fontCache = new HashMap<>();

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

        public static void loadStyling(String stylingPath) {
        try {
            File file = new File(stylingPath);
            if (!file.exists()) throw new RuntimeException("Styling file not found: " + stylingPath);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();

            NodeList colorList = document.getElementsByTagName("color");
            for (int i = 0; i < colorList.getLength(); i++) {
                Element colorElement = (Element) colorList.item(i);
                colorValues.put(colorElement.getAttribute("name"), colorElement.getTextContent().trim());
            }

            NodeList fontList = document.getElementsByTagName("font");
            for (int i = 0; i < fontList.getLength(); i++) {
                Element fontElement = (Element) fontList.item(i);
                String name = fontElement.getAttribute("name");
                String family = fontElement.getAttribute("family");
                int size = Integer.parseInt(fontElement.getAttribute("size"));
                int style = parseFontStyle(fontElement.getAttribute("style"));
                fontValues.put(name, new FontInfo(family, size, style));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error loading styling configuration: " + e.getMessage(), e);
        }
    }

    private static int parseFontStyle(String styleStr) {
        return switch (styleStr.toLowerCase()) {
            case "bold" -> Font.BOLD;
            case "italic" -> Font.ITALIC;
            case "bold-italic", "bolditalic" -> Font.BOLD | Font.ITALIC;
            default -> Font.PLAIN;
        };
    }
    public static class EffectInfo {
        public final String name;
        public final String type;
        public final Map<String, String> parameters;

        public EffectInfo(String name, String type, Map<String, String> parameters) {
            this.name = name;
            this.type = type;
            this.parameters = parameters;
        }

        public float getFloatParam(String key, float defaultValue) {
            String value = parameters.get(key);
            if (value == null) return defaultValue;
            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        public int getIntParam(String key, int defaultValue) {
            String value = parameters.get(key);
            if (value == null) return defaultValue;
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        public String getStringParam(String key, String defaultValue) {
            return parameters.getOrDefault(key, defaultValue);
        }
    }

    public static Color getColor(String name) {
        return getColor(name, Color.MAGENTA);
    }

    public static Color getColor(String name, Color defaultColor) {
        if (colorCache.containsKey(name)) return colorCache.get(name);

        String hex = colorValues.get(name);
        if (hex == null) return cacheColor(name, defaultColor);

        try {
            Color color = parseHexColor(hex);
            return cacheColor(name, color);
        } catch (Exception e) {
            return cacheColor(name, defaultColor);
        }
    }

    private static Color cacheColor(String name, Color color) {
        colorCache.put(name, color);
        return color;
    }

    private static Color parseHexColor(String hexColor) {
        if (!hexColor.startsWith("#")) throw new IllegalArgumentException("Color must start with #");
        String hex = hexColor.substring(1);

        switch (hex.length()) {
            case 6 -> {
                return new Color(Integer.parseInt(hex, 16));
            }
            case 8 -> {
                long value = Long.parseLong(hex, 16);
                int r = (int) ((value >> 24) & 0xFF);
                int g = (int) ((value >> 16) & 0xFF);
                int b = (int) ((value >> 8) & 0xFF);
                int a = (int) (value & 0xFF);
                return new Color(r, g, b, a);
            }
            default -> throw new IllegalArgumentException("Invalid hex color: " + hexColor);
        }
    }

    public static Font getFont(String name) {
        return getFont(name, new Font("SansSerif", Font.PLAIN, 12));
    }

    public static Font getFont(String name, Font defaultFont) {
        if (fontCache.containsKey(name)) return fontCache.get(name);

        FontInfo info = fontValues.get(name);
        if (info == null) return cacheFont(name, defaultFont);

        try {
            // Parse comma-separated font families (e.g., "Cinzel, Trajan Pro, Serif")
            String[] fontFamilies = info.family.split(",");
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] availableFonts = ge.getAvailableFontFamilyNames();
            
            // Try each font family in order until one is found
            for (String family : fontFamilies) {
                String trimmedFamily = family.trim();
                
                // Check if this font family is available
                for (String availableFont : availableFonts) {
                    if (availableFont.equalsIgnoreCase(trimmedFamily)) {
                        Font font = new Font(trimmedFamily, info.style, info.size);
                        return cacheFont(name, font);
                    }
                }
            }
            
            // If no fonts from the list are available, use the first one as fallback
            // (Java will substitute with a default font)
            Font font = new Font(fontFamilies[0].trim(), info.style, info.size);
            return cacheFont(name, font);
        } catch (Exception e) {
            return cacheFont(name, defaultFont);
        }
    }

    private static Font cacheFont(String name, Font font) {
        fontCache.put(name, font);
        return font;
    }

    public static void clearCaches() {
        colorCache.clear();
        fontCache.clear();
    }

    public static String[] getAvailableColorNames() {
        return colorValues.keySet().toArray(String[]::new);
    }

}
