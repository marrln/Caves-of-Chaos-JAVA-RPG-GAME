package ui;

import config.StyleConfig;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * A styled window displaying game controls and tips.
 * Designed to look like an ancient scroll with parchment aesthetics.
 *
 * Layout note:
 * - Uses GridBagLayout for the content area so each HTML JLabel receives the
 *   full available width and wraps naturally, preventing strange narrow columns
 *   or vertical overlap.
 */
public class GameControlsWindow extends JDialog {

    // ====== CONFIGURATION ======
    private static final int WINDOW_WIDTH = 700;
    private static final int WINDOW_HEIGHT = 600;
    private static final int BORDER_SIZE = 30;
    private static final int SCROLL_SPEED = 16;
    private static final int SCROLLBAR_WIDTH = 8;

    // internal grid row counter for GridBag
    private int gridRow = 0;

    // ====== CONSTRUCTOR ======
    public GameControlsWindow(Frame owner) {
        super(owner, "Explorer's Guide to the Caves", true);
        initializeWindow();
        add(createContentPanel());
        setLocationRelativeTo(owner);
    }

    // ====== INITIALIZATION ======
    private void initializeWindow() {
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(
                StyleConfig.getColor("scrollBackground", new Color(240, 230, 210)));
    }

    // ====== CONTENT CREATION ======
    private JPanel createContentPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        
        // Create decorative scroll border with aged paper effect
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                // Outer dark border for the scroll edges
                BorderFactory.createLineBorder(
                        StyleConfig.getColor("scrollBorder", new Color(139, 90, 43)), 4),
                BorderFactory.createCompoundBorder(
                        // Subtle inner shadow effect
                        BorderFactory.createLineBorder(
                                StyleConfig.getColor("scrollShadow", new Color(200, 190, 170)), 2),
                        new EmptyBorder(BORDER_SIZE - 12, BORDER_SIZE - 12, BORDER_SIZE - 12, BORDER_SIZE - 12)
                )
        ));

        JScrollPane scrollPane = createScrollPane(createTextContent());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        return mainPanel;
    }

    private JPanel createTextContent() {
        gridRow = 0; // reset row counter each time we build

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 25, 10, 25)); // consistent margins

        // Title
        addTitle(panel, "EXPLORER'S GUIDE");
        addVerticalSpace(panel, 12);

        // Movement
        addSectionHeader(panel, "MOVEMENT");
        addBullet(panel, "WASD / Arrow Keys — Move your character through the caves.");
        addVerticalSpace(panel, 12);

        // Actions
        addSectionHeader(panel, "ACTIONS");
        addBullet(panel, "SPACE — Investigate the rocks on the cave floor. Some conceal danger, others grant small rewards or a touch of experience.");
        addBullet(panel, "Q — Light attack. Quick, reliable, with a short cooldown.");
        addBullet(panel, "E — Heavy attack. Slower but stronger; longer cooldown.");
        addBullet(panel, "R — Rest to recover. Only possible when unseen by enemies. Resting has a big cooldown.");
        addBullet(panel, "1–9 — Use or equip items from your inventory.");
        addVerticalSpace(panel, 12);

        // Notes
        addSectionHeader(panel, "NOTES FOR THE BRAVE");
        addVerticalSpace(panel, 5);

        addNote(panel, "→ Timing is everything. Watch the shadows and strike only when the moment feels right.");
        addNote(panel, "→ Be cautious, some stones hide danger, while others conceal treasures waiting to be uncovered. Investigating them may even grant a touch of experience for your curiosity.");
        addNote(panel, "→ Always trust your senses, knowing how many foes remain can mean the difference between survival and doom.");
        addNote(panel, "→ Every enemy you defeat strengthens your resolve and grants EXP toward your next level.");
        addNote(panel, "→ Each level earned sharpens your skills and hardens your body, granting a small boost to your abilities.");
        addNote(panel, "→ Use the weapons you find, they may even grant you special abilities. Discovering another of the same kind refines and strengthens it further.");
        addNote(panel, "→ Your ultimate quest is to hunt down the Medusa of Chaos who lurks deep within the Caves of Chaos.");
        addNote(panel, "→ Should you slay the monster and triumph, the Shard of Judgement shall be yours.");
        addVerticalSpace(panel, 15);

        // Final message centered
        addFinalMessage(panel, "Good luck, brave explorer. The caves watch in silence and punish those who falter, be sure to show your strength in front of the Chaos Powers.");

        return panel;
    }

    private JScrollPane createScrollPane(JPanel content) {
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                StyleConfig.getColor("scrollBorder", new Color(139, 90, 43)), 1));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_SPEED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Slim scrollbar styling
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setPreferredSize(new Dimension(SCROLLBAR_WIDTH, 0));
        verticalBar.setBackground(
                StyleConfig.getColor("scrollBackground", new Color(240, 230, 210)));
        verticalBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = StyleConfig.getColor("scrollThumb", new Color(139, 90, 43));
                this.trackColor = StyleConfig.getColor("scrollTrack", new Color(220, 210, 190));
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
        });
        return scrollPane;
    }

    // ====== TEXT HELPERS (GridBag-friendly) ======
    private void addToGrid(JPanel panel, Component comp, Insets insets) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = gridRow++;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = insets;
        panel.add(comp, gbc);
    }

    private void addToGridCenter(JPanel panel, Component comp, Insets insets) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = gridRow++;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = insets;
        panel.add(comp, gbc);
    }

    private void addTitle(JPanel panel, String text) {
        // Add decorative scroll flourish above title
        JLabel flourishTop = new JLabel("⸎═══════════════⸎", SwingConstants.CENTER);
        flourishTop.setFont(StyleConfig.getFont("controlsFlourish", new Font("Serif", Font.PLAIN, 12)));
        flourishTop.setForeground(StyleConfig.getColor("scrollFlourishText", new Color(139, 90, 43)));
        flourishTop.setOpaque(false);
        flourishTop.setHorizontalAlignment(SwingConstants.CENTER);
        addToGridCenter(panel, flourishTop, new Insets(0, 0, 5, 0));
        
        // Title
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(StyleConfig.getFont("controlsTitle", new Font("Serif", Font.BOLD, 18)));
        label.setForeground(StyleConfig.getColor("scrollTitleText", new Color(101, 67, 33)));
        label.setOpaque(false);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        addToGridCenter(panel, label, new Insets(0, 0, 5, 0));
        
        // Add decorative scroll flourish below title
        JLabel flourishBottom = new JLabel("⸎═══════════════⸎", SwingConstants.CENTER);
        flourishBottom.setFont(StyleConfig.getFont("controlsFlourish", new Font("Serif", Font.PLAIN, 12)));
        flourishBottom.setForeground(StyleConfig.getColor("scrollFlourishText", new Color(139, 90, 43)));
        flourishBottom.setOpaque(false);
        flourishBottom.setHorizontalAlignment(SwingConstants.CENTER);
        addToGridCenter(panel, flourishBottom, new Insets(0, 0, 10, 0));
    }

    private void addSectionHeader(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setFont(StyleConfig.getFont("controlsSection", new Font("Serif", Font.BOLD, 16)));
        label.setForeground(StyleConfig.getColor("scrollSectionText", new Color(120, 80, 40)));
        label.setOpaque(false);
        addToGrid(panel, label, new Insets(10, 0, 5, 0));
    }

    private void addBullet(JPanel panel, String text) {
        JLabel label = createWrappedLabel("• " + text, new Font("Serif", Font.PLAIN, 14), new Color(60, 40, 20));
        addToGrid(panel, label, new Insets(0, 15, 5, 0));
    }

    private void addNote(JPanel panel, String text) {
        JLabel label = createWrappedLabel(text, new Font("Serif", Font.ITALIC, 13), new Color(80, 60, 40));
        addToGrid(panel, label, new Insets(0, 15, 5, 0));
    }

    private void addFinalMessage(JPanel panel, String text) {
        // Create a centered, wrapped label for the final message
        int availableWidth = WINDOW_WIDTH - (2 * BORDER_SIZE) - 200 - SCROLLBAR_WIDTH;
        
        JLabel label = new JLabel("<html><div style='width:" + availableWidth + "px; text-align:center;'><i>" + text + "</i></div></html>");
        label.setFont(StyleConfig.getFont("controlsFinal", new Font("Garamond", Font.BOLD | Font.ITALIC, 14)));
        label.setForeground(StyleConfig.getColor("scrollFinalText", new Color(139, 90, 43)));
        label.setOpaque(false);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        addToGridCenter(panel, label, new Insets(10, 0, 0, 0));
    }

    private void addVerticalSpace(JPanel panel, int height) {
        addToGrid(panel, Box.createVerticalStrut(height), new Insets(0, 0, 0, 0));
    }

    /**
     * Creates an HTML-wrapped JLabel that will fill the available horizontal space
     * and wrap its text naturally. Do NOT force preferred height here — let the
     * label compute its own preferred height based on the allocated width.
     */
    private JLabel createWrappedLabel(String text, Font font, Color color) {
        // Calculate available width: window width - borders - margins - scrollbar
        int availableWidth = WINDOW_WIDTH - (2 * BORDER_SIZE) - 200 - SCROLLBAR_WIDTH;
        
        JLabel label = new JLabel("<html><div style='width:" + availableWidth + "px; text-align:justify;'>" + text + "</div></html>");
        label.setFont(StyleConfig.getFont("controlsText", font));
        label.setForeground(StyleConfig.getColor("scrollText", color));
        label.setOpaque(false);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    // ====== PUBLIC API ======
    public static void showControls(Frame parentFrame) {
        SwingUtilities.invokeLater(() -> {
            GameControlsWindow window = new GameControlsWindow(parentFrame);
            window.setVisible(true);
        });
    }
}
