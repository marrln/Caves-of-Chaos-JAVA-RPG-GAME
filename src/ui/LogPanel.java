package ui;

import config.StyleConfig;
import java.awt.*;
import javax.swing.*;

/**
 * Panel for displaying game logs and messages at the bottom of the screen.
 * Messages have typewriter effect but instantly complete if a new message arrives.
 */
public class LogPanel extends JPanel {

    // ====== CONFIGURATION ======
    private static final int MAX_MESSAGES      = 50;
    private static final int PANEL_HEIGHT      = 160;
    private static final int SCROLLBAR_WIDTH   = 12;
    private static final int MARGIN_SIZE       = 8;
    private static final int TYPEWRITER_DELAY  = 20; // ms per character

    // ====== UI COMPONENTS ======
    private final JTextArea logTextArea;
    private final JScrollPane scrollPane;

    // ====== TYPEWRITER STATE ======
    private String baseText = "";          // All completed messages
    private String currentMessage = "";    // Message being typed
    private int typingIndex = 0;
    private boolean isTyping = false;
    private Timer typewriterTimer;

    // ====== INITIAL MESSAGES ======
    private final String initialMessage = """
    Welcome to Caves of Chaos!
    Use WASD or arrow keys to move. Press E to attack, R to rest. Press 1-9 to use inventory items.
    Explore the caves and find the Medusa of Chaos at the deepest part!
    Slaying the monster and acquiring the Shard of Judgement will end your quest.
    """;


    // ====== CONSTRUCTOR ======
    public LogPanel() {
        setLayout(new BorderLayout());
        setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
        setPreferredSize(new Dimension(800, PANEL_HEIGHT));
        setBorder(BorderFactory.createLineBorder(StyleConfig.getColor("panelBorder", Color.DARK_GRAY), 1));

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
        logTextArea.setForeground(StyleConfig.getColor("panelText", Color.WHITE));
        logTextArea.setFont(StyleConfig.getFont("log", new Font("Monospaced", Font.PLAIN, 12)));
        logTextArea.setLineWrap(true);
        logTextArea.setWrapStyleWord(true);
        logTextArea.setMargin(new Insets(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));

        scrollPane = new JScrollPane(logTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));

        JScrollBar bar = scrollPane.getVerticalScrollBar();
        bar.setPreferredSize(new Dimension(SCROLLBAR_WIDTH, 0));
        bar.setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
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

        add(scrollPane, BorderLayout.CENTER);
        SwingUtilities.invokeLater(() -> addMessage(initialMessage));
    }

    // ====== PUBLIC API ======
    public void addMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String msg = message.trim(); // <-- use a new local variable
            if (msg.isEmpty()) return;
            if (isTyping) completeCurrentMessageInstantly();
            startTypingMessage(msg);
        });
    }


    public void clearLog() {
        SwingUtilities.invokeLater(() -> {
            stopTyping();
            logTextArea.setText("");
            baseText = "";
        });
    }

    public String getLogText() { return logTextArea.getText(); }
    public int getMaxMessages() { return MAX_MESSAGES; }
    public boolean isTyping() { return isTyping; }

    // ====== TYPEWRITER LOGIC ======
    private void startTypingMessage(String message) {
        currentMessage = message;
        typingIndex = 0;
        isTyping = true;

        typewriterTimer = new Timer(TYPEWRITER_DELAY, e -> typeNextCharacter());
        typewriterTimer.start();
    }

    private void typeNextCharacter() {
        if (typingIndex >= currentMessage.length()) {
            stopTyping();
            appendToBaseText(currentMessage);
            return;
        }

        logTextArea.setText(baseText + (baseText.isEmpty() ? "" : "\n") + currentMessage.substring(0, typingIndex + 1));
        typingIndex++;
        autoScrollToBottom();
    }

    private void completeCurrentMessageInstantly() {
        if (!isTyping) return;
        stopTyping();
        appendToBaseText(currentMessage);
    }

    private void appendToBaseText(String message) {
        if (!baseText.isEmpty()) baseText += "\n";
        baseText += message;
        cleanupOldMessages();
        logTextArea.setText(baseText);
        autoScrollToBottom();
    }

    private void stopTyping() {
        if (typewriterTimer != null) {
            typewriterTimer.stop();
            typewriterTimer = null;
        }
        isTyping = false;
    }

    // ====== MESSAGE MANAGEMENT ======
    private void cleanupOldMessages() {
        String[] lines = logTextArea.getText().split("\n");
        if (lines.length <= MAX_MESSAGES) return;

        StringBuilder sb = new StringBuilder();
        for (int i = lines.length - MAX_MESSAGES; i < lines.length; i++) {
            if (i > lines.length - MAX_MESSAGES) sb.append("\n");
            sb.append(lines[i]);
        }
        logTextArea.setText(sb.toString());
        baseText = logTextArea.getText();
    }

    private void autoScrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }
}
