package items;

import javax.swing.SwingUtilities;
import ui.CongratulationWindow;

/**
 * The legendary Shard of Judgement - the ultimate victory item.
 * When picked up, the player wins the game.
 */
public class ShardOfJudgement extends Item {
    
    // Victory flag - used to signal game completion
    private static boolean victoryTriggered = false;
    
    public ShardOfJudgement() {
        super("Shard of Judgement", 
              ItemType.CONSUMABLE,
              "A crystalline fragment of pure light that radiates with divine power. " +
              "Legend says it can banish all darkness from the realm.");
    }
    
    @Override
    public boolean canUse(player.AbstractPlayer player) {
        return true; // Victory item can always be "used" (picked up)
    }
    
    @Override
    public boolean use(player.AbstractPlayer player) {
        // This is a victory item - when picked up, the player wins
        SwingUtilities.invokeLater(() -> {
            // Show victory window
            CongratulationWindow congratsWindow = new CongratulationWindow(null, player.getName());
            boolean playAgain = congratsWindow.showVictoryDialog();
            
            if (playAgain) {
                // Signal to restart the game (will be handled by main game loop)
                victoryTriggered = false;
                // TODO: For now, just exit - a proper restart would require main game loop changes
                System.exit(0);
            } else {
                // Exit the game
                System.exit(0);
            }
        });
        
        victoryTriggered = true;
        return true; // Item consumed successfully
    }
    
    @Override
    public String getDisplayName() {
        return getName(); // No quantity for unique victory item
    }
    
    @Override
    public Item copy() {
        return new ShardOfJudgement();
    }
    
    /**
     * Check if victory has been triggered by this item.
     * 
     * @return true if the Shard of Judgement has been used
     */
    public static boolean isVictoryTriggered() {
        return victoryTriggered;
    }
    
    /**
     * Reset the victory state (for game restart).
     */
    public static void resetVictoryState() {
        victoryTriggered = false;
    }
}