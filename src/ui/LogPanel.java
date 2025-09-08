package ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/**
 * Panel for displaying game logs and messages at the bottom of the screen.
 */
public class LogPanel extends JPanel {
    private List<String> logMessages;
    private final int MAX_MESSAGES = 5;
    private final Font logFont = new Font("Monospaced", Font.PLAIN, 14);
    
    public LogPanel() {
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 100));
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        logMessages = new ArrayList<>();
        
        // Add some test messages
        addMessage("Welcome to Caves of Chaos!");
        addMessage("Use WASD or arrow keys to move.");
        addMessage("Press E to attack, R to rest.");
    }
    
    /**
     * Adds a new message to the log.
     * If the log is full, the oldest message will be removed.
     * 
     * @param message The message to add
     */
    public void addMessage(String message) {
        logMessages.add(message);
        if (logMessages.size() > MAX_MESSAGES) {
            logMessages.remove(0);
        }
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.setFont(logFont);
        g.setColor(Color.WHITE);
        
        int y = 20;
        for (String message : logMessages) {
            g.drawString(message, 10, y);
            y += 20;
        }
    }
}
