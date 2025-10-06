package utils;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for loading and registering custom fonts.
 * Ensures consistent typography across platforms.
 */
public class FontLoader {

    private static final String FONTS_DIRECTORY = "assets/fonts/";
    private static final Map<String, Font> loadedFonts = new HashMap<>();
    private static boolean initialized = false;

    public static int loadCustomFonts() {
        if (initialized) return loadedFonts.size();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontFiles = {
            "Cinze.ttf",
            "EBGaramond.ttf",
            "EBGaramond-Italic.ttf"
        };

        for (String fontFile : fontFiles) {
            Font font = loadFont(FONTS_DIRECTORY + fontFile);
            if (font != null) {
                ge.registerFont(font);
                loadedFonts.put(font.getFontName(), font);
            }
        }

        initialized = true;
        return loadedFonts.size();
    }

    private static Font loadFont(String path) {
        try (InputStream fontStream = FontLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (fontStream != null) return Font.createFont(Font.TRUETYPE_FONT, fontStream);
        } catch (FontFormatException | IOException ignored) {}

        File file = new File(path);
        if (!file.exists()) file = new File("bin/" + path);

        try {
            if (file.exists()) return Font.createFont(Font.TRUETYPE_FONT, file);
        } catch (FontFormatException | IOException ignored) {}

        return null;
    }

    public static boolean isFontAvailable(String fontName) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (String font : ge.getAvailableFontFamilyNames()) {
            if (font.equalsIgnoreCase(fontName)) return true;
        }
        return false;
    }

    public static String[] getLoadedFontNames() {
        return loadedFonts.keySet().toArray(String[]::new);
    }
}
