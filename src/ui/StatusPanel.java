package ui;

import audio.MusicManager;
import config.StyleConfig;
import core.GameState;
import items.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import player.AbstractPlayer;

/**
 * Compact, scrollable status panel showing player info, inventory, and equipment.
 * Uses HTML selectively for text wrapping where needed.
 */
public class StatusPanel extends JPanel {

    // ====== CONFIGURATION ======
    private static final int SCROLLBAR_WIDTH = 8;  // Slim scrollbar for compact status panel
    private static final int SECTION_SPACING = 10;
    private static final int ITEM_SPACING = 0;
    private static final int CONTENT_PADDING = 10;

    // ====== FIELDS ======
    private GameState gameState;
    private final JPanel contentPanel = new JPanel();
    private final JScrollPane scrollPane;
    private JButton musicButton;

    // ====== CONSTRUCTOR ======
    public StatusPanel() {
        setLayout(new BorderLayout());
        setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
        setPreferredSize(new Dimension(300, 800));
        setBorder(BorderFactory.createLineBorder(StyleConfig.getColor("panelBorder", Color.DARK_GRAY), 1));
        setFocusable(false);

        // Content panel
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(getBackground());
        contentPanel.setFocusable(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(CONTENT_PADDING, CONTENT_PADDING, CONTENT_PADDING, CONTENT_PADDING));

        // Scroll pane with custom scrollbar styling
        scrollPane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.setFocusable(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setFocusable(false);
        customizeScrollBar(scrollPane.getVerticalScrollBar());

        add(scrollPane, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    // ====== SETTERS ======
    public void setGameState(GameState gameState) {
        this.gameState = gameState;
        refresh();
    }

    // ====== PUBLIC METHODS ======
    public void refresh() {
        if (gameState == null) return;
        contentPanel.removeAll();

        AbstractPlayer player = gameState.getPlayer();
        String playerName = capitalize(player.getName());

        refreshPlayerStats(player, playerName);
        refreshCaveInfo();
        refreshInventoryAndEquipment(player);

        contentPanel.revalidate();
        contentPanel.repaint();
        // Preserve scroll position on refresh for better UX
    }

    // ====== REFRESH HELPERS ======
    private void refreshPlayerStats(AbstractPlayer player, String playerName) {
        addBoldText(player.getClass().getSimpleName() + " " + playerName);
        addColoredText("HP: " + player.getHp() + "/" + player.getMaxHp(),
                getStatColor(player.getHp(), player.getMaxHp(), true));
        if (player.getMaxMp() > 0)
            addColoredText("MP: " + player.getMp() + "/" + player.getMaxMp(),
                    getStatColor(player.getMp(), player.getMaxMp(), false));
        addText("Level: " + player.getLevel() + " / " + player.getMaxLevel());
        addText(player.getLevel() >= player.getMaxLevel() ? "EXP: MAX"
                : "EXP: " + player.getExp() + "/" + player.getExpToNext());

        for (int i = 1; i <= 2; i++) {
            String cd = player.getCooldownDisplay(i);
            addColoredText(player.getAttackDisplayName(i) + ": " + cd,
                    "Ready".equals(cd)
                            ? StyleConfig.getColor("cooldownReady", Color.GREEN)
                            : StyleConfig.getColor("cooldownNotReady", Color.RED));
        }

        // Rest cooldown display
        String restCd = player.getRestCooldownDisplay();
        addColoredText("Rest: " + restCd,
                "Ready".equals(restCd)
                        ? StyleConfig.getColor("cooldownReady", Color.GREEN)
                        : StyleConfig.getColor("cooldownNotReady", Color.RED));
    }

    private void refreshCaveInfo() {
        addSectionTitle("CAVE INFO");
        addText(gameState.getLevelDisplayString());
        int aliveEnemies = gameState.getAliveEnemyCount();
        addAtmosphericText("Your senses deliver their verdict:");
        switch (aliveEnemies) {
            case 0 -> addText("You are alone… for now.");
            case 1 -> addText("A single presence lingers, watching.");
            default -> {
                addText("There are " + aliveEnemies + " enemies lurking here, each waiting for"); 
                addText("their moment.");
            }
        }
    }

    private void refreshInventoryAndEquipment(AbstractPlayer player) {
        addSectionTitle("INVENTORY");
        Inventory inv = player.getInventory();
        for (int i = 1; i <= 9; i++) addItemSlot(i, inv.getItem(i));

        addSectionTitle("EQUIPMENT");
        addWeaponDisplay(player.getEquippedWeapon());
    }

    // ====== BUTTON PANEL ======
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 5));
        panel.setBackground(getBackground());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        panel.setFocusable(false);

        musicButton = createButton(getMusicButtonText(), e -> {
            MusicManager.getInstance().toggleMusic();
            musicButton.setText(getMusicButtonText());
        });
        panel.add(musicButton);
        panel.add(createButton("Game Controls", e -> GameControlsWindow.showControls(getParentFrame())));
        return panel;
    }

    /**
     * Creates a themed button with scroll aesthetic colors and fonts.
     * Uses plain font to preserve emoji rendering without forced bold.
     */
    private JButton createButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);

        // Use styled font from StyleConfig ("statusButton" style preferred)
        Font styledFont = StyleConfig.getFont("Cinzel");
        btn.setFont(styledFont);
        btn.setBackground(StyleConfig.getColor("buttonBackground", new Color(139, 90, 43)));
        btn.setForeground(StyleConfig.getColor("buttonText", new Color(243, 235, 211)));

        btn.setFocusable(false);
        btn.addActionListener(action);
        return btn;
    }

    private String getMusicButtonText() {
        return MusicManager.getInstance().isMusicEnabled() ? "🔊 Toggle Music" : "🔇 Toggle Music";
    }

    private Frame getParentFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
    }

    // ====== LABEL / DISPLAY HELPERS ======
    private void addSectionTitle(String text) { addLabel(text, null, true, 16, SECTION_SPACING); }
    private void addText(String text) { addLabel(text, null, false, 14, ITEM_SPACING); }
    private void addBoldText(String text) { addLabel(text, null, true, 14, ITEM_SPACING); }
    private void addColoredText(String text, Color color) { addLabel(text, color, false, 14, ITEM_SPACING); }
    private void addAtmosphericText(String text) { addAtmosphericLabel(text); }

    private void addWeaponDisplay(Weapon weapon) {
        if (weapon == null) { 
            addText("Weapon: None"); 
            return; 
        }
        
        String name = "Weapon: " + weapon.getName();
        String damage = " (+" + weapon.getDamageBonus() + " damage)";
        
        if ((name + damage).length() <= 30) {
            addText(name + damage);
        } else { 
            addText(name); 
            addText(damage); 
        }
        if (weapon.getEffect() != config.ItemConfig.WeaponEffect.NONE) {
            String effectText = weapon.getEffectDescription();
            if (!effectText.isEmpty()) {
                addColoredText(effectText, StyleConfig.getColor("statHigh"));
            }
            else {
                addText("No special effect");
            }
        }
    }

    private void addLabel(String text, Color color, boolean bold, int size, int topSpacing) {
        JLabel label = new JLabel(text);
        Font font = StyleConfig.getFont(bold ? "statusTitle" : "statusNormal",
                new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size));
        label.setFont(font);
        label.setForeground(color != null ? color : StyleConfig.getColor("panelText", Color.WHITE));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(topSpacing, 0, ITEM_SPACING, 0));
        contentPanel.add(label);
    }
    
    private void addAtmosphericLabel(String text) {
        JLabel label = new JLabel(text);
        Font font = StyleConfig.getFont("statusAtmospheric", new Font("Serif", Font.ITALIC, 14));
        label.setFont(font);
        label.setForeground(StyleConfig.getColor("panelHighlight", Color.LIGHT_GRAY));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(BorderFactory.createEmptyBorder(ITEM_SPACING, 0, ITEM_SPACING, 0));
        contentPanel.add(label);
    }

    private void addItemSlot(int slot, Item item) {
        // Add icon inline for potions after slot number
        if (item instanceof Potion potion) {
            Icon icon = getPotionIcon(potion);
            if (icon != null) {
                JLabel label = new JLabel("[" + slot + "] ");
                label.setIcon(icon);
                label.setHorizontalTextPosition(SwingConstants.LEFT);
                label.setText("[" + slot + "] " + item.getDisplayName());
                label.setIconTextGap(4);
                Font font = StyleConfig.getFont("statusNormal", new Font("SansSerif", Font.PLAIN, 14));
                label.setFont(font);
                label.setForeground(StyleConfig.getColor("panelText", Color.WHITE));
                label.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(label);
                return;
            }
        }
        
        String text = "[" + slot + "] " + (item != null ? item.getDisplayName() : "--");
        addText(text);
    }

    // ====== UTILITY METHODS ======

    private Color getStatColor(int current, int max, boolean isHP) {
        double pct = (double) current / max;
        if (pct < 0.25) return StyleConfig.getColor("statLow", Color.RED);
        if (pct < 0.5) return StyleConfig.getColor("statMedium", Color.ORANGE);
        return isHP ? StyleConfig.getColor("statHigh", Color.GREEN) : StyleConfig.getColor("statMP", Color.BLUE);
    }

    private Icon getPotionIcon(Potion potion) {
        String id = (potion.getHpRestore() > 0 && potion.getMpRestore() > 0) ? "mana_health_potion"
                : potion.getMpRestore() > 0 ? "mana_potion" : "health_potion";
        BufferedImage img = graphics.AssetManager.getInstance().loadImage(id);
        return img != null ? new ImageIcon(img.getScaledInstance(16, 16, Image.SCALE_SMOOTH)) : null;
    }

    private String capitalize(String s) { return s.substring(0, 1).toUpperCase() + s.substring(1); }

    private void customizeScrollBar(JScrollBar bar) {
        bar.setPreferredSize(new Dimension(SCROLLBAR_WIDTH, 0));
        bar.setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
        bar.setFocusable(false);
        bar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                // Match LogPanel scrollbar colors: thumb = panelText, track = panelBorder
                this.thumbColor = StyleConfig.getColor("panelText", Color.LIGHT_GRAY);
                this.trackColor = StyleConfig.getColor("panelBorder", Color.DARK_GRAY);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createStyledButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createStyledButton();
            }

            private JButton createStyledButton() {
                JButton btn = new JButton();
                btn.setBackground(StyleConfig.getColor("panelBorder", Color.DARK_GRAY));
                btn.setForeground(StyleConfig.getColor("panelText", Color.LIGHT_GRAY));
                return btn;
            }
        });
    }
}
