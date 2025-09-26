package core;

import enemies.Enemy;
import items.AbstractTrap;
import items.Item;
import items.Potion;
import map.GameMap;
import map.Tile;
import player.AbstractPlayer;
import player.Wizard;
import ui.GameUIManager;
import utils.LineUtils;

public class GameController {
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
                player.addExperience(exp);
                gameState.logMessage(target.getName() + " has been defeated! You gained " + exp + " exp!");
            }
            return; // only one enemy per attack
        }
    }

    private Enemy findEnemyAt(int x, int y) {
        for (Enemy e : gameState.getCurrentEnemies()) {
            if (!e.isDead() && e.getX() == x && e.getY() == y) return e;
        }
        return null;
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

        Item item = tile.getItem().get();

        if (item instanceof items.ShardOfJudgement shard) {
            tile.removeItem();
            gameState.logMessage("You found the legendary " + shard.getName() + "!");
            shard.use(player);
            return;
        }

        if (player.getInventory().addItem(item)) {
            tile.removeItem();
            gameState.logMessage("Picked up: " + item.getName());
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
            gameState.logMessage(trap.getTriggerMessage());

            int dmg = trap.getDamage();
            player.takeDamage(dmg);

            int after = player.getHp();
            gameState.logMessage(trap.getDamageMessage() + " You lost " + dmg + " HP! (" + after + "/" + player.getMaxHp() + ")");
            if (after <= 0) {
                gameState.logMessage("The " + trap.getName() + " was LETHAL! You have died!");
            }

            tile.removeItem();
        }
    }
}
