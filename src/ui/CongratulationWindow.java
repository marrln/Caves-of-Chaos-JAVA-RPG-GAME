package ui;

import config.StyleConfig;
import java.awt.*;
import javax.swing.*;

/**
 * Congratulation window displayed when the player wins the game.
 * Shows victory message and options to play again or exit.
 */
public class CongratulationWindow extends JDialog {
    
    private boolean shouldPlayAgain = false;
    
    public CongratulationWindow(JFrame parent, String playerName) {
        super(parent, "Victory!", true);
        initializeWindow(playerName);
    }
    
    private void initializeWindow(String playerName) {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        
        // Create main panel with dark background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(StyleConfig.getColor("windowBackground", Color.BLACK));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Victory message panel
        JPanel messagePanel = createMessagePanel(playerName);
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
    
    private JPanel createMessagePanel(String playerName) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(StyleConfig.getColor("windowBackground", Color.BLACK));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Victory crown icon (using text as fallback)
        JLabel crownLabel = new JLabel("👑", SwingConstants.CENTER);
        crownLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        crownLabel.setForeground(new Color(255, 215, 0)); // Gold color
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(crownLabel, gbc);
        
        // "Congratulations!" message
        JLabel victoryLabel = new JLabel("Congratulations!", SwingConstants.CENTER);
        victoryLabel.setFont(StyleConfig.getFont("victoryTitle", new Font("SansSerif", Font.BOLD, 36)));
        victoryLabel.setForeground(new Color(255, 215, 0)); // Gold color
        gbc.gridy = 1;
        panel.add(victoryLabel, gbc);
        
        // Player achievement
        JLabel playerLabel = new JLabel(playerName + " has conquered the Caves of Chaos!", SwingConstants.CENTER);
        playerLabel.setFont(StyleConfig.getFont("victoryText", new Font("SansSerif", Font.BOLD, 18)));
        playerLabel.setForeground(StyleConfig.getColor("windowText", Color.WHITE));
        gbc.gridy = 2;
        panel.add(playerLabel, gbc);
        
        // Medusa defeat message
        JLabel medusaLabel = new JLabel("The Medusa of Chaos has been defeated!", SwingConstants.CENTER);
        medusaLabel.setFont(StyleConfig.getFont("victorySubtext", new Font("SansSerif", Font.PLAIN, 16)));
        medusaLabel.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 3;
        panel.add(medusaLabel, gbc);
        
        // Shard message
        JLabel shardLabel = new JLabel("You have claimed the Shard of Judgement!", SwingConstants.CENTER);
        shardLabel.setFont(StyleConfig.getFont("victorySubtext", new Font("SansSerif", Font.ITALIC, 16)));
        shardLabel.setForeground(Color.CYAN);
        gbc.gridy = 4;
        panel.add(shardLabel, gbc);
        
        // Final flavor text
        JLabel flavorLabel = new JLabel("The darkness lifts, and light returns to the realm...", SwingConstants.CENTER);
        flavorLabel.setFont(StyleConfig.getFont("victoryFlavor", new Font("SansSerif", Font.ITALIC, 14)));
        flavorLabel.setForeground(Color.GRAY);
        gbc.gridy = 5;
        panel.add(flavorLabel, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setBackground(StyleConfig.getColor("windowBackground", Color.BLACK));
        
        // Play Again button
        JButton playAgainButton = new JButton("Play Again");
        playAgainButton.setFont(StyleConfig.getFont("buttonText", new Font("SansSerif", Font.BOLD, 16)));
        playAgainButton.setBackground(StyleConfig.getColor("buttonBackground", Color.DARK_GRAY));
        playAgainButton.setForeground(new Color(255, 215, 0)); // Gold color
        playAgainButton.setFocusPainted(false);
        playAgainButton.setBorderPainted(false);
        playAgainButton.addActionListener(unused -> {
            shouldPlayAgain = true;
            dispose();
        });
        
        // Exit button
        JButton exitButton = new JButton("Exit Game");
        exitButton.setFont(StyleConfig.getFont("buttonText", new Font("SansSerif", Font.BOLD, 16)));
        exitButton.setBackground(StyleConfig.getColor("buttonBackground", Color.DARK_GRAY));
        exitButton.setForeground(StyleConfig.getColor("buttonText", Color.WHITE));
        exitButton.setFocusPainted(false);
        exitButton.setBorderPainted(false);
        exitButton.addActionListener(unused -> {
            shouldPlayAgain = false;
            dispose();
            System.exit(0);
        });
        
        panel.add(playAgainButton);
        panel.add(exitButton);
        
        return panel;
    }
    
    public boolean showVictoryDialog() {
        // Make the dialog modal and wait for user input
        setModalityType(ModalityType.APPLICATION_MODAL);
        setVisible(true);
        
        // The dialog will block here until user clicks a button
        // (buttons call dispose() which closes the dialog)
        return shouldPlayAgain;
    }
}