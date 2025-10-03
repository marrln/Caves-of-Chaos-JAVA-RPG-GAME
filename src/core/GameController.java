package core;

import enemies.Enemy;
import items.AbstractTrap;
import items.Inventory;
import items.Item;
import items.Potion;
import items.Weapon;
import java.util.Optional;
import map.GameMap;
import map.Tile;
import player.AbstractPlayer;
import player.Wizard;
import ui.GameUIManager;
import utils.LineUtils;

public class GameController {
    private static final int ITEM_PICKUP_XP = 50; // XP awarded for picking up items
    
    private final GameState gameState;
    private GameUIManager uiManager;
    private final ProjectileManager projectileManager;

    public GameController(GameState gameState) {
        this.gameState = gameState;
        this.projectileManager = new ProjectileManager();
    }

    public void setUIManager(GameUIManager uiManager) { this.uiManager = uiManager; }

    // ====== PLAYER MOVEMENT ======
    public boolean movePlayer(int dx, int dy) {
        AbstractPlayer player = gameState.getPlayer();
        int newX = player.getX() + dx;
        int newY = player.getY() + dy;
        GameMap map = gameState.getCurrentMap();

        // Boundaries and visibility
        if (newX < 0 || newY < 0 || newX >= map.getWidth() || newY >= map.getHeight()) return false;
        if (uiManager != null && !uiManager.isPositionVisible(newX, newY)) return false;

        Tile tile = map.getTile(newX, newY);

        return switch (tile.getType()) {
            case Tile.FLOOR -> {
                if (player.tryMoveTo(newX, newY)) {
                    gameState.updateFogOfWar();
                    checkForTraps(newX, newY);
                    yield true;
                }
                yield false;
            }
            case Tile.STAIRS_DOWN -> {
                if (gameState.canGoToNextLevel()) {
                    gameState.goToNextLevel();
                    gameState.logMessage("You descend to floor " + (gameState.getCurrentLevel() + 1) + ".");
                    yield true;
                }
                yield false;
            }
            case Tile.STAIRS_UP -> {
                if (gameState.getCurrentLevel() > 0) {
                    gameState.goToPreviousLevel();
                    gameState.logMessage("You climb back to floor " + (gameState.getCurrentLevel() + 1) + ".");
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    // ====== RESTING ======
    public void rest() {
        AbstractPlayer player = gameState.getPlayer();
        int oldHp = player.getHp();
        int oldMp = player.getMp();

        player.rest();
        logRestoration("Rested", player, player.getHp() - oldHp, player.getMp() - oldMp);
    }

    private void logRestoration(String source, AbstractPlayer player, int hpRestored, int mpRestored) {
        if (hpRestored > 0) {
            gameState.logMessage(source + ": Restored " + hpRestored + " HP (" + player.getHp() + "/" + player.getMaxHp() + " HP)");
        }
        if (mpRestored > 0) {
            gameState.logMessage(source + ": Restored " + mpRestored + " MP (" + player.getMp() + "/" + player.getMaxMp() + " MP)");
        }
        if (hpRestored == 0 && mpRestored == 0) {
            gameState.logMessage(source + ": No effect, already at full stats.");
        }
    }

    // ====== ATTACKING ======
    public void attack(int attackType) {
        AbstractPlayer player = gameState.getPlayer();
        player.updateCombat();

        int mpBefore = player.getMp();
        player.attack(attackType);
        int mpAfter = player.getMp();

        boolean success = !(player instanceof Wizard) || mpAfter < mpBefore;
        if (!success) return;

        if (player instanceof Wizard wizard) {
            handleWizardAttack(wizard, attackType);
        } else {
            handleMeleeAttack(player, attackType);
        }
    }

    private void handleWizardAttack(Wizard wizard, int attackType) {
        Projectile projectile = wizard.createProjectile(
            attackType, gameState.getCurrentEnemies(), gameState.getFogOfWar()
        );

        if (projectile != null) {
            addProjectile(projectile);
            String spell = attackType == 1 ? "Fire Spell" : "Ice Spell";
            gameState.logMessage(wizard.getName() + " casts " + spell + "!");
        } else {
            gameState.logMessage("No targets in sight. The spell fizzles.");
        }
    }

    private void handleMeleeAttack(AbstractPlayer player, int attackType) {
        for (Enemy target : gameState.getCurrentEnemies()) {
            if (target.isDead()) continue;
            if (!LineUtils.isCardinallyAdjacent(player.getX(), player.getY(), target.getX(), target.getY())) continue;

            int dmg = player.getAttackDamage(attackType);
            gameState.logMessage(player.getName() + " attacks " + target.getName() + " for " + dmg + " damage!");

            boolean dead = target.takeDamage(dmg);
            if (dead) {
                int exp = target.getExpReward();
                int levelsGained = player.addExperience(exp);
                gameState.logMessage(target.getName() + " has been defeated! You gained " + exp + " exp!");
                
                // Log level-up message
                if (levelsGained > 0) {
                    if (levelsGained == 1) {
                        gameState.logMessage(player.getName() + " reached level " + player.getLevel() + "! (HP: " + player.getMaxHp() + ", MP: " + player.getMaxMp() + ")");
                    } else {
                        gameState.logMessage(player.getName() + " gained " + levelsGained + " levels! Now level " + player.getLevel() + "!");
                    }
                }
            }
            return; // only one enemy per attack
        }
    }

    // ====== ITEMS ======
    public void useItem(int slot) {
        AbstractPlayer player = gameState.getPlayer();
        Item item = player.getInventory().getItem(slot);
        if (item == null) return;

        if (item instanceof Potion potion) {
            int oldHp = player.getHp();
            int oldMp = player.getMp();
            player.useItem(slot);
            logRestoration("Used " + potion.getName(), player, player.getHp() - oldHp, player.getMp() - oldMp);
        } else {
            player.useItem(slot);
        }
    }

    public void pickupItem() {
        AbstractPlayer player = gameState.getPlayer();
        Tile tile = gameState.getCurrentMap().getTile(player.getX(), player.getY());
        
        if (!tile.hasItem()) return;

        Optional<Item> itemOpt = tile.getItem();
        if (itemOpt.isEmpty()) {
            tile.removeItem(); // Clear the ghost item
            gameState.logMessage("You find nothing here...");
            return;
        }
        
        Item item = itemOpt.get();
        if (item == null) {
            tile.removeItem(); // Clear the null item
            gameState.logMessage("You find nothing here...");
            return;
        }

        if (item instanceof items.ShardOfJudgement shard) {
            tile.removeItem();
            gameState.logMessage("You found the legendary " + shard.getName() + "!");
            shard.use(player);
            return;
        }

        Inventory inventory = player.getInventory();
        if (inventory.addItem(item, player)) {
            // Award XP for finding items
            int levelsGained = player.addExperience(ITEM_PICKUP_XP);
            
            // Check if this was a weapon upgrade/downgrade
            if (inventory.wasLastAddWeaponUpgrade()) {
                Weapon weapon = (Weapon) item;
                gameState.logMessage("Upgraded " + weapon.getName() + " (+" + weapon.getDamageBonus() + " dmg)! +" + ITEM_PICKUP_XP + " exp");
            } else if (inventory.wasLastAddWeaponDowngrade()) {
                gameState.logMessage("You already have a better " + item.getName() + ". +" + ITEM_PICKUP_XP + " exp");
            } else {
                gameState.logMessage("Picked up: " + item.getName() + " (+" + ITEM_PICKUP_XP + " exp)");
            }
            
            // Log level-up message if applicable
            if (levelsGained > 0) {
                if (levelsGained == 1) {
                    gameState.logMessage(player.getName() + " reached level " + player.getLevel() + "! (HP: " + player.getMaxHp() + ", MP: " + player.getMaxMp() + ")");
                } else {
                    gameState.logMessage(player.getName() + " gained " + levelsGained + " levels! Now level " + player.getLevel() + "!");
                }
            }
            
            tile.removeItem();
        } else {
            gameState.logMessage("Inventory is full!");
        }
    }

    // ====== PROJECTILES ======
    public void updateProjectiles(double dt) {
        projectileManager.updateAll(dt, gameState.getCurrentMap(), gameState.getCurrentEnemies(), gameState.getFogOfWar());
    }

    public void addProjectile(Projectile p) { projectileManager.addProjectile(p); }
    public java.util.List<Projectile> getActiveProjectiles() { return projectileManager.getActiveProjectiles(); }
    public void clearProjectiles() { projectileManager.clearAll(); }

    // ====== TRAPS ======
    private void checkForTraps(int x, int y) {
        Tile tile = gameState.getCurrentMap().getTile(x, y);
        if (!tile.hasItem()) return;

        Item item = tile.getItem().get();
        if (item instanceof AbstractTrap trap) {
            AbstractPlayer player = gameState.getPlayer();
            
            // Log trap trigger with damage in a single clear message
            int dmg = trap.getDamage();
            boolean isDead = player.takeDamage(dmg);
            int hpAfter = player.getHp();
            
            // Consolidated trap message: trigger + damage in one line
            gameState.logMessage(trap.getTriggerMessage() + " " + trap.getDamageMessage());
            gameState.logMessage(player.getName() + "took " + dmg + " damage! (" + hpAfter + "/" + player.getMaxHp() + " HP)");
            
            if (isDead && !gameState.isGameOver()) {
                gameState.setGameOver(true);
                gameState.logMessage("The " + trap.getName() + " was LETHAL! " + player.getName() + " has been defeated!");
                // Trigger game over on the event dispatch thread
                javax.swing.SwingUtilities.invokeLater(() -> {
                    ui.GameOverWindow win = new ui.GameOverWindow(null, player.getName(), gameState.getCurrentLevel());
                    if (win.showGameOverDialog()) {
                        gameState.logMessage("Game restart requested - not yet implemented");
                    } else {
                        System.exit(0);
                    }
                });
            }
            tile.removeItem();
        }
    }
}
