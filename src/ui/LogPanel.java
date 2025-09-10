package ui;

import config.StyleConfig;
import java.awt.*;
import javax.swing.*;

/**
 * Panel for displaying game logs and messages at the bottom of the screen.
 * Features scrollable text area with auto-scroll to bottom for new messages.
 */
public class LogPanel extends JPanel {
    private final JTextArea logTextArea;
    private final JScrollPane scrollPane;
    private static final int MAX_MESSAGES = 50;
    private int messageCount = 0;
    
    public LogPanel() {
        setLayout(new BorderLayout());
        setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
        setPreferredSize(new Dimension(800, 100));
        setBorder(BorderFactory.createLineBorder(StyleConfig.getColor("panelBorder", Color.DARK_GRAY), 1));
        
        // Create the text area for log messages
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
        logTextArea.setForeground(StyleConfig.getColor("panelText", Color.WHITE));
        logTextArea.setFont(StyleConfig.getFont("log", new Font("Monospaced", Font.PLAIN, 12)));
        logTextArea.setLineWrap(true);
        logTextArea.setWrapStyleWord(true);
        logTextArea.setMargin(new Insets(8, 8, 8, 8)); // Increased margin for better spacing
        
        // Create scroll pane
        scrollPane = new JScrollPane(logTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
        
        // Style the scroll bars with better visibility
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        verticalScrollBar.setBackground(StyleConfig.getColor("panelBackground", Color.BLACK));
        verticalScrollBar.setPreferredSize(new Dimension(12, 0)); // Make scroll bar wider
        verticalScrollBar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = StyleConfig.getColor("panelText", Color.LIGHT_GRAY);
                this.trackColor = StyleConfig.getColor("panelBorder", Color.DARK_GRAY);
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                JButton button = super.createDecreaseButton(orientation);
                button.setBackground(StyleConfig.getColor("panelBorder", Color.DARK_GRAY));
                button.setForeground(StyleConfig.getColor("panelText", Color.LIGHT_GRAY));
                return button;
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                JButton button = super.createIncreaseButton(orientation);
                button.setBackground(StyleConfig.getColor("panelBorder", Color.DARK_GRAY));
                button.setForeground(StyleConfig.getColor("panelText", Color.LIGHT_GRAY));
                return button;
            }
        });
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Initialize with some test messages (using direct text setting to avoid overridable method call)
        String initialText = """
                Welcome to Caves of Chaos!
                Use WASD or arrow keys to move.
                Press E to attack, R to rest.
                Press 1-9 to use inventory items.
                Explore the caves and find the exit!
                Beware of dangerous monsters lurking in the darkness.
                Collect potions and equipment to aid your journey.
                Good luck, adventurer!
                """;
        
        logTextArea.setText(initialText);
        messageCount = initialText.split("\n").length - 1; // Count initial messages (subtract 1 for empty string at end)
        
        // Scroll to bottom initially
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalScrollBar2 = scrollPane.getVerticalScrollBar();
            verticalScrollBar2.setValue(verticalScrollBar2.getMaximum());
        });
    }
    
    /**
     * Adds a new message to the log.
     * Automatically scrolls to the bottom to show the newest message.
     * Maintains a maximum of 50 messages, removing oldest when exceeded.
     * 
     * @param message The message to add
     */
    public void addMessage(String message) {
        // Add timestamp if desired
        String timestampedMessage = message + "\n";
        
        // Append the new message
        logTextArea.append(timestampedMessage);
        messageCount++;
        
        // Remove oldest messages if we exceed the limit
        if (messageCount > MAX_MESSAGES) {
            String text = logTextArea.getText();
            String[] lines = text.split("\n");
            
            // Keep only the last MAX_MESSAGES lines
            StringBuilder newText = new StringBuilder();
            int startIndex = Math.max(0, lines.length - MAX_MESSAGES);
            for (int i = startIndex; i < lines.length; i++) {
                if (i > startIndex) {
                    newText.append("\n");
                }
                newText.append(lines[i]);
            }
            
            logTextArea.setText(newText.toString());
            messageCount = MAX_MESSAGES;
        }
        
        // Auto-scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
            verticalScrollBar.setValue(verticalScrollBar.getMaximum());
        });
    }
    
    /**
     * Clears all messages from the log.
     */
    public void clearLog() {
        logTextArea.setText("");
        messageCount = 0;
    }
    
    /**
     * Gets the current log text.
     * 
     * @return The current log content
     */
    public String getLogText() {
        return logTextArea.getText();
    }
}
