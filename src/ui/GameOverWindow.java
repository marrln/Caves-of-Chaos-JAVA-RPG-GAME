package ui;

import config.StyleConfig;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * Game Over window displayed when the player dies.
 * Shows death message and options to restart or exit.
 */
public class GameOverWindow extends JDialog {
    
    private boolean shouldRestart = false;

    public GameOverWindow(JFrame parent, String playerName, int level) {
        super(parent, "Game Over", true);
        initializeWindow(playerName, level);
    }
    
    private void initializeWindow(String playerName, int level) {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        
        // Create main panel with dark background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(StyleConfig.getColor("windowBackground", Color.BLACK));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Death message panel
        JPanel messagePanel = createMessagePanel(playerName, level);
        mainPanel.add(messagePanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        pack();
        
        // Center on parent
        if (getParent() != null) {
            setLocationRelativeTo(getParent());
        } else {
            setLocationRelativeTo(null);
        }
    }
    
    private JPanel createMessagePanel(String playerName, int level) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(StyleConfig.getColor("windowBackground", Color.BLACK));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Death skull icon (using text as fallback)
        JLabel skullLabel = new JLabel("☠", SwingConstants.CENTER);
        skullLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        skullLabel.setForeground(Color.RED);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(skullLabel, gbc);
        
        // "You have died!" message
        JLabel deathLabel = new JLabel("You have died!", SwingConstants.CENTER);
        deathLabel.setFont(StyleConfig.getFont("gameOverTitle", new Font("SansSerif", Font.BOLD, 32)));
        deathLabel.setForeground(Color.RED);
        gbc.gridy = 1;
        panel.add(deathLabel, gbc);
        
        // Player info
        JLabel playerLabel = new JLabel(playerName + " reached Cave Floor " + level, SwingConstants.CENTER);
        playerLabel.setFont(StyleConfig.getFont("gameOverText", new Font("SansSerif", Font.PLAIN, 18)));
        playerLabel.setForeground(StyleConfig.getColor("windowText", Color.WHITE));
        gbc.gridy = 2;
        panel.add(playerLabel, gbc);
        
        // Flavor text
        JLabel flavorLabel = new JLabel("The darkness of the caves has claimed another soul...", SwingConstants.CENTER);
        flavorLabel.setFont(StyleConfig.getFont("gameOverFlavor", new Font("SansSerif", Font.ITALIC, 14)));
        flavorLabel.setForeground(Color.GRAY);
        gbc.gridy = 3;
        panel.add(flavorLabel, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setBackground(StyleConfig.getColor("windowBackground", Color.BLACK));
        
        // Restart button
        JButton restartButton = new JButton("Play Again");
        restartButton.setFont(StyleConfig.getFont("buttonText", new Font("SansSerif", Font.BOLD, 16)));
        restartButton.setBackground(StyleConfig.getColor("buttonBackground", Color.DARK_GRAY));
        restartButton.setForeground(StyleConfig.getColor("buttonText", Color.WHITE));
        restartButton.setFocusPainted(false);
        restartButton.setBorderPainted(false);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shouldRestart = true;
                dispose();
            }
        });
        
        // Exit button
        JButton exitButton = new JButton("Exit Game");
        exitButton.setFont(StyleConfig.getFont("buttonText", new Font("SansSerif", Font.BOLD, 16)));
        exitButton.setBackground(StyleConfig.getColor("buttonBackground", Color.DARK_GRAY));
        exitButton.setForeground(StyleConfig.getColor("buttonText", Color.WHITE));
        exitButton.setFocusPainted(false);
        exitButton.setBorderPainted(false);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shouldRestart = false;
                dispose();
                System.exit(0);
            }
        });
        
        panel.add(restartButton);
        panel.add(exitButton);
        
        return panel;
    }
    
    public boolean showGameOverDialog() {
        setVisible(true);
        return shouldRestart;
    }
}