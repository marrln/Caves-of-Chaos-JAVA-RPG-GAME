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

/** Loads and provides colors and fonts from styling configuration. */
public class StyleConfig {

    private static final Map<String, String> colorValues = new HashMap<>();
    private static final Map<String, Color> colorCache = new HashMap<>();
    private static final Map<String, FontInfo> fontValues = new HashMap<>();
    private static final Map<String, Font> fontCache = new HashMap<>();

    private static class FontInfo {
        final String family;
        final int size, style;
        FontInfo(String family, int size, int style) { this.family = family; this.size = size; this.style = style; }
    }

    public static void loadStyling(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) throw new RuntimeException("Styling file not found: " + path);

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList colors = doc.getElementsByTagName("color");
            for (int i = 0; i < colors.getLength(); i++) {
                Element e = (Element) colors.item(i);
                colorValues.put(e.getAttribute("name"), e.getTextContent().trim());
            }

            NodeList fonts = doc.getElementsByTagName("font");
            for (int i = 0; i < fonts.getLength(); i++) {
                Element e = (Element) fonts.item(i);
                fontValues.put(e.getAttribute("name"),
                        new FontInfo(e.getAttribute("family"),
                                     Integer.parseInt(e.getAttribute("size")),
                                     parseFontStyle(e.getAttribute("style"))));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error loading styling configuration: " + ex.getMessage(), ex);
        }
    }

    private static int parseFontStyle(String s) {
        return switch (s.toLowerCase()) {
            case "bold" -> Font.BOLD;
            case "italic" -> Font.ITALIC;
            case "bold-italic", "bolditalic" -> Font.BOLD | Font.ITALIC;
            default -> Font.PLAIN;
        };
    }

    public static Color getColor(String name, Color defaultColor) {
        if (colorCache.containsKey(name)) return colorCache.get(name);
        String hex = colorValues.get(name);
        if (hex == null) return cacheColor(name, defaultColor);

        try { return cacheColor(name, parseHexColor(hex)); }
        catch (Exception e) { return cacheColor(name, defaultColor); }
    }
    
    public static Color getColor(String name) { return getColor(name, Color.MAGENTA); }
    private static Color cacheColor(String name, Color color) { colorCache.put(name, color); return color; }

    private static Color parseHexColor(String hex) {
        if (!hex.startsWith("#")) throw new IllegalArgumentException("Color must start with #");
        hex = hex.substring(1);
        if (hex.length() == 6) return new Color(Integer.parseInt(hex, 16));
        if (hex.length() == 8) {
            long v = Long.parseLong(hex, 16);
            return new Color((int)((v>>24)&0xFF), (int)((v>>16)&0xFF), (int)((v>>8)&0xFF), (int)(v&0xFF));
        }
        throw new IllegalArgumentException("Invalid hex color: " + hex);
    }

    public static Font getFont(String name, Font defaultFont) {
        if (fontCache.containsKey(name)) return fontCache.get(name);
        FontInfo info = fontValues.get(name);
        if (info == null) return cacheFont(name, defaultFont);

        try {
            String[] families = info.family.split(",");
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] available = ge.getAvailableFontFamilyNames();

            for (String f : families) {
                String trimmed = f.trim();
                for (String af : available) if (af.equalsIgnoreCase(trimmed))
                    return cacheFont(name, new Font(trimmed, info.style, info.size));
            }
            return cacheFont(name, new Font(families[0].trim(), info.style, info.size));
        } catch (Exception e) {
            return cacheFont(name, defaultFont);
        }
    }

    public static Font getFont(String name) { return getFont(name, new Font("SansSerif", Font.PLAIN, 12)); }
    private static Font cacheFont(String name, Font font) { fontCache.put(name, font); return font; }
}
