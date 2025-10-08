package ui;

import config.StyleConfig;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 * Panel for displaying game logs and messages at the bottom of the screen.
 * Messages have a typewriter effect with proper queueing (FIFO) to prevent race conditions.
 * Supports colored messages via method overloading.
 */
public class LogPanel extends JPanel {

    // ====== CONFIGURATION ======
    private static final int MAX_MESSAGES    = 50;
    private static final int PANEL_HEIGHT    = 160;
    private static final int SCROLLBAR_WIDTH = 8;
    private static final int MARGIN_SIZE     = 12;

    // ====== UI COMPONENTS ======
    private final JTextPane logTextPane;
    private final StyledDocument styledDoc;
    private final JScrollPane scrollPane;

    // ====== ANIMATION ======
    private final TypewriterAnimator animator;

    // ====== INITIAL MESSAGES ======
    private final String initialMessage = """
    Welcome to Caves of Chaos!
    Use WASD or arrow keys to move. Press E to attack, R to rest. Press 1-9 to use inventory items.
    Explore the caves and find the Medusa of Chaos at the deepest part!
    Slaying the monster and acquiring the Shard of Judgement will end your quest.
    """;

    // ====== CONSTRUCTOR ======
    public LogPanel() {
        initializePanel();
        logTextPane = createTextPane();
        styledDoc = logTextPane.getStyledDocument();
        scrollPane = createScrollPane(logTextPane);
        animator = new TypewriterAnimator(styledDoc, this::updateDisplay, this::autoScrollToBottom);
        add(scrollPane, BorderLayout.CENTER);
        addMessage(initialMessage);
    }

    // ====== INITIALIZATION HELPERS ======
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
        setPreferredSize(new Dimension(800, PANEL_HEIGHT));
        setBorder(BorderFactory.createLineBorder(StyleConfig.getColor("panelBorder", Color.DARK_GRAY), 1));
        setFocusable(false);
    }

    private JTextPane createTextPane() {
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFocusable(false);
        textPane.setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
        textPane.setForeground(StyleConfig.getColor("panelText", Color.WHITE));
        textPane.setFont(StyleConfig.getFont("log", new Font("Monospaced", Font.PLAIN, 12)));
        textPane.setMargin(new Insets(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));
        return textPane;
    }

    private JScrollPane createScrollPane(JTextPane textPane) {
        JScrollPane scroll = new JScrollPane(textPane);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.setFocusable(false);
        scroll.getViewport().setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
        customizeScrollBar(scroll.getVerticalScrollBar());
        return scroll;
    }

    private void customizeScrollBar(JScrollBar bar) {
        bar.setPreferredSize(new Dimension(SCROLLBAR_WIDTH, 0));
        bar.setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
        bar.setFocusable(false);
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = StyleConfig.getColor("panelText", Color.LIGHT_GRAY);
                this.trackColor = StyleConfig.getColor("panelBorder", Color.DARK_GRAY);
            }
            @Override protected JButton createDecreaseButton(int o) { return createStyledButton(); }
            @Override protected JButton createIncreaseButton(int o) { return createStyledButton(); }
            private JButton createStyledButton() {
                JButton b = new JButton();
                b.setBackground(StyleConfig.getColor("panelBorder", Color.DARK_GRAY));
                b.setForeground(StyleConfig.getColor("panelText", Color.LIGHT_GRAY));
                return b;
            }
        });
    }

    // ====== PUBLIC API ======
    public void addMessage(String message) {
        addMessage(message, StyleConfig.getColor("panelText", Color.WHITE));
    }

    public void addMessage(String message, Color color) {
        SwingUtilities.invokeLater(() -> animator.queueMessage(message, color));
    }

    public void clearLog() {
        SwingUtilities.invokeLater(() -> {
            animator.clear();
            try {
                styledDoc.remove(0, styledDoc.getLength());
            } catch (BadLocationException e) {
                System.err.println("Error clearing log: " + e.getMessage());
            }
        });
    }

    public String getLogText() { return logTextPane.getText(); }
    public int getMaxMessages() { return MAX_MESSAGES; }
    public boolean isTyping() { return animator.isTyping(); }
    public int getQueueSize() { return animator.getQueueSize(); }

    // ====== DISPLAY / UPDATE HELPERS ======
    private void updateDisplay() {
        try {
            styledDoc.remove(0, styledDoc.getLength());
            
            // Retrieve completed messages and cleanup if needed
            java.util.List<TypewriterAnimator.ColoredMessage> messages = animator.getCompletedMessages();
            messages = cleanupOldMessages(messages);
            
            // Insert all completed messages with their original colors
            for (TypewriterAnimator.ColoredMessage msg : messages) {
                if (styledDoc.getLength() > 0) {
                    styledDoc.insertString(styledDoc.getLength(), "\n", createStyle(Color.WHITE));
                }
                Color color = msg.color != null ? msg.color : StyleConfig.getColor("panelText", Color.WHITE);
                styledDoc.insertString(styledDoc.getLength(), msg.text, createStyle(color));
            }

            // Insert currently-typing partial message with its color
            if (animator.hasPartialMessage()) {
                if (styledDoc.getLength() > 0) {
                    styledDoc.insertString(styledDoc.getLength(), "\n", createStyle(Color.WHITE));
                }
                styledDoc.insertString(styledDoc.getLength(), animator.getPartialMessage(),
                        createStyle(animator.getCurrentColor()));
            }
        } catch (BadLocationException e) {
            System.err.println("Error updating log display: " + e.getMessage());
        }
    }

    private java.util.List<TypewriterAnimator.ColoredMessage> cleanupOldMessages(
            java.util.List<TypewriterAnimator.ColoredMessage> messages) {
        if (messages.size() <= MAX_MESSAGES) {
            return messages;
        }
        
        // Keep only the last MAX_MESSAGES - create a new list to avoid subList view issues
        java.util.List<TypewriterAnimator.ColoredMessage> trimmed = new java.util.ArrayList<>(
            messages.subList(messages.size() - MAX_MESSAGES, messages.size())
        );
        animator.setCompletedMessages(trimmed);
        return trimmed;
    }

    private void autoScrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    // ====== STYLED DOCUMENT HELPERS ======
    private AttributeSet createStyle(Color color) {
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);
        return style;
    }
}
