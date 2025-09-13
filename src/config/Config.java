package config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Utility for loading and accessing game settings from XML.
 */
public class Config {

    private static final Map<String, String> settings = new HashMap<>();

    /** Loads configuration from XML files. Assets are handled separately by AssetManager. */
    public static void loadConfigs(String settingsPath, String assetsPath) {
        loadSettings(settingsPath);
    }

    private static void loadSettings(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new RuntimeException("Settings file not found: " + filePath);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();

            NodeList settingList = document.getElementsByTagName("setting");
            for (int i = 0; i < settingList.getLength(); i++) {
                Element setting = (Element) settingList.item(i);
                String name = setting.getAttribute("name");
                String value = setting.getTextContent().trim();

                if (value.isEmpty()) {
                    StringBuilder attributes = new StringBuilder();
                    for (int j = 0; j < setting.getAttributes().getLength(); j++) {
                        String attrName = setting.getAttributes().item(j).getNodeName();
                        String attrValue = setting.getAttributes().item(j).getNodeValue();
                        if (!"name".equals(attrName)) {
                            attributes.append(attrName).append("=\"").append(attrValue).append("\" ");
                        }
                    }
                    value = attributes.toString();
                }

                settings.put(name, value);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error loading settings: " + e.getMessage(), e);
        }
    }

    public static String getSetting(String name) {
        return settings.get(name);
    }

    public static int getIntSetting(String name) {
        return Integer.parseInt(settings.get(name));
    }

    public static boolean getBoolSetting(String name) {
        return Boolean.parseBoolean(settings.get(name));
    }

    public static double getDoubleSetting(String name) {
        return Double.parseDouble(settings.get(name));
    }
}
