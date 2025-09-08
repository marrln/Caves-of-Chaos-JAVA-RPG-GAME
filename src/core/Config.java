package core;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;

public class Config {
    private static Document settingsDoc;
    private static Document assetsDoc;

    public static void loadConfigs(String settingsPath, String assetsPath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            settingsDoc = dBuilder.parse(new File(settingsPath));
            settingsDoc.getDocumentElement().normalize();
            assetsDoc = dBuilder.parse(new File(assetsPath));
            assetsDoc.getDocumentElement().normalize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config: " + e.getMessage(), e);
        }
    }

    public static String getSetting(String tagName) {
        NodeList nList = settingsDoc.getElementsByTagName(tagName);
        if (nList.getLength() > 0) {
            return nList.item(0).getTextContent();
        }
        return null;
    }

    public static Map<String, String> getAnimationFrameDurations() {
        Map<String, String> map = new HashMap<>();
        NodeList nList = settingsDoc.getElementsByTagName("animationFrameDurations");
        if (nList.getLength() > 0) {
            Node node = nList.item(0);
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    map.put(child.getNodeName(), child.getTextContent());
                }
            }
        }
        return map;
    }

    public static String getAssetPath(String... tags) {
        Node node = assetsDoc.getDocumentElement();
        for (String tag : tags) {
            NodeList nList = ((Element) node).getElementsByTagName(tag);
            if (nList.getLength() > 0) {
                node = nList.item(0);
            } else {
                return null;
            }
        }
        return node.getTextContent();
    }
}
