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
 * Configuration utility for loading and accessing game settings.
 */
public class Config {
    private static Map<String, String> settings = new HashMap<>();
    
    /**
     * Loads configuration from XML files.
     * 
     * @param settingsPath Path to settings XML file
     * @param assetsPath Path to assets XML file
     */
    public static void loadConfigs(String settingsPath, String assetsPath) {
        loadSettings(settingsPath);
        loadAssets(assetsPath);
    }
    
    /**
     * Loads settings from an XML file.
     * 
     * @param filePath Path to the settings XML file
     */
    private static void loadSettings(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("Settings file not found: " + filePath);
                return;
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
                
                // If setting has no text content, serialize the element's attributes
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
            
            System.out.println("Loaded " + settings.size() + " settings");
        } catch (Exception e) {
            System.err.println("Error loading settings: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads asset definitions from an XML file.
     * 
     * @param filePath Path to the assets XML file
     */
    private static void loadAssets(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.err.println("Assets file not found: " + filePath);
                return;
            }
            
            // TODO: Implement asset loading
            
        } catch (Exception e) {
            System.err.println("Error loading assets: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Gets a setting by name.
     * 
     * @param name The setting name
     * @return The setting value, or null if not found
     */
    public static String getSetting(String name) {
        return settings.get(name);
    }
    
    /**
     * Gets a setting as an integer.
     * 
     * @param name The setting name
     * @param defaultValue The default value to return if the setting is not found or not a valid integer
     * @return The setting value as an integer
     */
    public static int getIntSetting(String name, int defaultValue) {
        String value = settings.get(name);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Gets a setting as a boolean.
     * 
     * @param name The setting name
     * @param defaultValue The default value to return if the setting is not found
     * @return The setting value as a boolean
     */
    public static boolean getBoolSetting(String name, boolean defaultValue) {
        String value = settings.get(name);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
}
