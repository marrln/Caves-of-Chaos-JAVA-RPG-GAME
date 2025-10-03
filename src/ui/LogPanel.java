package ui;

import config.StyleConfig;
import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.*;

/**
 * Panel for displaying game logs and messages at the bottom of the screen.
 * Messages have typewriter effect with proper queueing to prevent race conditions.
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
    
    // ====== MESSAGE QUEUE ======
    private final Queue<String> messageQueue = new LinkedList<>();
    private boolean processingQueue = false;

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
        setFocusable(false); // Prevent stealing focus from GamePanel

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFocusable(false); // Prevent stealing focus from GamePanel
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
        scrollPane.setFocusable(false); // Prevent scrollbar from stealing focus
        scrollPane.getViewport().setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));

        JScrollBar bar = scrollPane.getVerticalScrollBar();
        bar.setPreferredSize(new Dimension(SCROLLBAR_WIDTH, 0));
        bar.setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
        bar.setFocusable(false); // Prevent scrollbar buttons from stealing focus
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
        
        // Add initial message directly to avoid queue race condition
        baseText = initialMessage.trim();
        logTextArea.setText(baseText);
        autoScrollToBottom();
    }

    // ====== PUBLIC API ======
    public void addMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String msg = message.trim();
            if (msg.isEmpty()) return;
            
            // If currently typing, instantly complete it to keep up with game events
            if (isTyping) {
                instantCompleteCurrentMessage();
            }
            
            // Add to queue
            messageQueue.offer(msg);
            
            // Process queue if not already processing
            if (!processingQueue) {
                processNextMessage();
            }
        });
    }


    public void clearLog() {
        SwingUtilities.invokeLater(() -> {
            stopTyping();
            messageQueue.clear();
            logTextArea.setText("");
            baseText = "";
            processingQueue = false;
        });
    }

    public String getLogText() { return logTextArea.getText(); }
    public int getMaxMessages() { return MAX_MESSAGES; }
    public boolean isTyping() { return isTyping; }
    public int getQueueSize() { return messageQueue.size(); }

    // ====== QUEUE PROCESSING ======
    private void processNextMessage() {
        // If already typing or no messages, stop
        if (isTyping || messageQueue.isEmpty()) {
            processingQueue = false;
            return;
        }
        
        processingQueue = true;
        String nextMessage = messageQueue.poll();
        if (nextMessage != null) {
            startTypingMessage(nextMessage);
        } else {
            processingQueue = false;
        }
    }

    // ====== TYPEWRITER LOGIC ======
    private void startTypingMessage(String message) {
        // Guard: ensure clean state
        if (isTyping) {
            return; // Should not happen with queue, but defensive
        }
        
        currentMessage = message;
        typingIndex = 0;
        isTyping = true;

        typewriterTimer = new Timer(TYPEWRITER_DELAY, e -> typeNextCharacter());
        typewriterTimer.start();
    }

    private void typeNextCharacter() {
        // Guard: check if we should still be typing
        if (!isTyping || typingIndex >= currentMessage.length()) {
            finishCurrentMessage();
            return;
        }

        logTextArea.setText(baseText + (baseText.isEmpty() ? "" : "\n") + currentMessage.substring(0, typingIndex + 1));
        typingIndex++;
        autoScrollToBottom();
    }

    private void finishCurrentMessage() {
        if (!isTyping) return; // Already finished
        
        stopTyping();
        
        // Append complete message to base text
        if (!baseText.isEmpty()) baseText += "\n";
        baseText += currentMessage;
        cleanupOldMessages();
        logTextArea.setText(baseText);
        autoScrollToBottom();
        
        // Reset state
        currentMessage = "";
        typingIndex = 0;
        
        // Process next message in queue
        SwingUtilities.invokeLater(this::processNextMessage);
    }
    
    private void instantCompleteCurrentMessage() {
        if (!isTyping) return; // Not typing, nothing to complete
        
        // Stop the timer immediately
        if (typewriterTimer != null) {
            typewriterTimer.stop();
            typewriterTimer = null;
        }
        
        // Complete the message instantly without animation
        if (!baseText.isEmpty()) baseText += "\n";
        baseText += currentMessage;
        cleanupOldMessages();
        logTextArea.setText(baseText);
        autoScrollToBottom();
        
        // Reset state
        currentMessage = "";
        typingIndex = 0;
        isTyping = false;
        
        // CRITICAL FIX: Must process next message in queue, otherwise queue gets stuck!
        // This was causing the LogPanel to freeze after the first enemy notice
        SwingUtilities.invokeLater(this::processNextMessage);
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
        String[] lines = baseText.split("\n");
        if (lines.length <= MAX_MESSAGES) return;

        StringBuilder sb = new StringBuilder();
        int startIndex = lines.length - MAX_MESSAGES;
        for (int i = startIndex; i < lines.length; i++) {
            if (i > startIndex) sb.append("\n");
            sb.append(lines[i]);
        }
        baseText = sb.toString();
    }

    private void autoScrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }
}
