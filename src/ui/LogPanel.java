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
        setPreferredSize(new Dimension(800, 160)); 
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
                Use WASD or arrow keys to move. Press E to attack, R to rest. Press 1-9 to use inventory items.
                Explore the caves and find the Medusa of Chaos at the deepest part!
                Slaying the monster and aquiring the Shard of Judgement will end your quest.
                """;
        
        logTextArea.setText(initialText);
        messageCount = initialText.split("\n").length - 1; // Count initial messages (subtract 1 for empty string at end)
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
    
    public void clearLog() {
        logTextArea.setText("");
        messageCount = 0;
    }

    public String getLogText() {
        return logTextArea.getText();
    }
}
