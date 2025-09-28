package ui;

import config.StyleConfig;
import core.GameState;
import items.Inventory;
import items.Item;
import items.Potion;
import items.Weapon;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * Scrollable status panel with compact item spacing and refresh support.
 */
public class StatusPanel extends JPanel {
    private GameState gameState;
    private final JPanel contentPanel;
    private final JScrollPane scrollPane;

    private static final int SECTION_SPACING = 10; // Between sections
    private static final int ITEM_SPACING = 0;     // Between items in the same section

    public StatusPanel() {
        setLayout(new BorderLayout());
        setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
        setPreferredSize(new Dimension(250, 600));
        setBorder(BorderFactory.createLineBorder(StyleConfig.getColor("panelBorder", Color.DARK_GRAY), 1));

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(getBackground());

        scrollPane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        refresh(); // Rebuild content immediately
    }

    /**
     * Refreshes the panel based on the current gameState.
     * Call this whenever game stats, inventory, or cooldowns change.
     */
    public void refresh() {
        if (gameState == null) return;

        contentPanel.removeAll();

        // CHARACTER section
        addSectionTitle("PLAYER INFO");
        String name = gameState.getPlayer().getName();
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        
        addText("Name: " + gameState.getPlayer().getClass().getSimpleName() + name);
        addColoredText("HP: " + gameState.getPlayer().getHp() + "/" + gameState.getPlayer().getMaxHp(), getHpColor());
        if (gameState.getPlayer().getMaxMp() > 0) {
            addText("MP: " + gameState.getPlayer().getMp() + "/" + gameState.getPlayer().getMaxMp());
        }
        addText("Level: " + gameState.getPlayer().getLevel());
        addText("EXP: " + gameState.getPlayer().getExp());

        // Cooldowns
        boolean isWizard = gameState.getPlayer().getClass().getSimpleName().equals("Wizard");
        String attack1 = isWizard ? "Fire Spell:" : "Quick Strike:";
        String attack2 = isWizard ? "Ice Spell:" : "Power Attack:";
        addText("");
        addColoredText(attack1 + " " + gameState.getPlayer().getCooldownDisplay(1),
                "Ready".equals(gameState.getPlayer().getCooldownDisplay(1)) ? Color.GREEN : Color.RED);
        addColoredText(attack2 + " " + gameState.getPlayer().getCooldownDisplay(2),
                "Ready".equals(gameState.getPlayer().getCooldownDisplay(2)) ? Color.GREEN : Color.RED);
        
        addSectionTitle("Cave Info");
        addText(gameState.getLevelDisplayString());
        addText("You can sense the presence of " + gameState.getCurrentEnemies().size() + " enemies.");
        if (!gameState.canGoToNextLevel()) {
            addColoredText("You have reached the deepest\nparts of the Caves of Chaos!", StyleConfig.getColor("panelHighlight", Color.RED));
        }

        // INVENTORY section
        addSectionTitle("INVENTORY");
        Inventory inventory = gameState.getPlayer().getInventory();
        for (int i = 0; i < 9; i++) {
            addItemSlot(i + 1, inventory.getItem(i + 1));
        }

        // EQUIPMENT section
        addSectionTitle("EQUIPMENT");
        Weapon weapon = gameState.getPlayer().getEquippedWeapon();
        if (weapon != null) {
            addText("Weapon: " + weapon.getName());
            addText("  +" + weapon.getDamageBonus() + " damage");
        } else {
            addText("Weapon: None");
        }

        // CONTROLS section
        addSectionTitle("CONTROLS");
        addText("WASD: Move");
        addText("SPACE: Pick up");
        addText("Q/E: Attack");
        addText("R: Rest");
        addText("1-9: Use Item");

        contentPanel.revalidate();
        contentPanel.repaint();
        scrollPane.getVerticalScrollBar().setValue(0); // Scroll to top
    }

    // --------------------- Helper Methods ---------------------

    public void addSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(StyleConfig.getFont("statusTitle", new Font("SansSerif", Font.BOLD, 16)));
        label.setForeground(StyleConfig.getColor("panelText", Color.WHITE));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(SECTION_SPACING, 0, ITEM_SPACING, 0));
        contentPanel.add(label);
    }

    public void addText(String text) {
        JLabel label = new JLabel(text);
        label.setFont(StyleConfig.getFont("statusNormal", new Font("SansSerif", Font.PLAIN, 14)));
        label.setForeground(StyleConfig.getColor("panelText", Color.WHITE));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(ITEM_SPACING, 0, ITEM_SPACING, 0));
        contentPanel.add(label);
    }

    public void addColoredText(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(StyleConfig.getFont("statusNormal", new Font("SansSerif", Font.PLAIN, 14)));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(ITEM_SPACING, 0, ITEM_SPACING, 0));
        contentPanel.add(label);
    }

    public void addItemSlot(int slot, Item item) {
        JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0)); // hgap=2, vgap=0
        itemPanel.setBackground(getBackground());
        itemPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Slot number and item name
        String text = "[" + slot + "] " + (item != null ? item.getDisplayName() : "--");
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(StyleConfig.getFont("statusNormal", new Font("SansSerif", Font.PLAIN, 14)));
        textLabel.setForeground(StyleConfig.getColor("panelText", Color.WHITE));
        itemPanel.add(textLabel);

        // Potion icon after text
        if (item instanceof Potion potion) {
            Icon icon = getPotionIcon(potion);
            if (icon != null) {
                JLabel iconLabel = new JLabel(icon);
                itemPanel.add(iconLabel);
            }
        }

        // Add small spacing below each slot
        itemPanel.setBorder(BorderFactory.createEmptyBorder(ITEM_SPACING, 0, ITEM_SPACING, 0));

        contentPanel.add(itemPanel);
    }


    private Icon getPotionIcon(Potion potion) {
        String iconId = getPotionIconId(potion);
        BufferedImage image = graphics.AssetManager.getInstance().loadImage(iconId);
        if (image != null) {
            return new ImageIcon(image.getScaledInstance(16, 16, Image.SCALE_SMOOTH));
        }
        return null;
    }

    private Color getHpColor() {
        int currentHp = gameState.getPlayer().getHp();
        int maxHp = gameState.getPlayer().getMaxHp();
        double percent = (double) currentHp / maxHp;
        if (percent < 0.25) return Color.RED;
        if (percent < 0.5) return Color.ORANGE;
        return StyleConfig.getColor("panelText", Color.WHITE);
    }

    private String getPotionIconId(Potion potion) {
        String name = potion.getName().toLowerCase();
        if (name.contains("mana")) return "mana_potion";
        return "health_potion";
    }
}
