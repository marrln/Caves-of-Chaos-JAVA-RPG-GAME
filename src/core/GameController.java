package core;

import config.StyleConfig;
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
import utils.GeometryHelpers;

public class GameController {

    private static final int ITEM_PICKUP_XP = 20;

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
        int newX = player.getX() + dx, newY = player.getY() + dy;
        GameMap map = gameState.getCurrentMap();

        if (!isMoveValid(newX, newY, map)) return false;

        Tile tile = map.getTile(newX, newY);

        return switch (tile.getType()) {
            case Tile.FLOOR -> moveToFloor(player, newX, newY);
            case Tile.STAIRS_DOWN -> handleStairsDown();
            case Tile.STAIRS_UP -> handleStairsUp();
            default -> false;
        };
    }

    private boolean isMoveValid(int x, int y, GameMap map) {
        if (x < 0 || y < 0 || x >= map.getWidth() || y >= map.getHeight()) return false;
        return uiManager == null || uiManager.isPositionVisible(x, y);
    }

    private boolean moveToFloor(AbstractPlayer player, int x, int y) {
        if (!player.tryMoveTo(x, y)) return false;
        gameState.updateFogOfWar();
        checkForTraps(x, y);
        return true;
    }

    private boolean handleStairsDown() {
        if (!gameState.canGoToNextLevel()) return false;
        gameState.goToNextLevel();
        gameState.logMessage("You descend to floor " + (gameState.getCurrentLevel() + 1) + ".");
        return true;
    }

    private boolean handleStairsUp() {
        if (gameState.getCurrentLevel() <= 0) return false;
        gameState.goToPreviousLevel();
        gameState.logMessage("You climb back to floor " + (gameState.getCurrentLevel() + 1) + ".");
        return true;
    }

    // ====== RESTING ======
    public void rest() {
        AbstractPlayer player = gameState.getPlayer();
        if (!player.canRest()) return;
        if (isBeingChased()) {
            gameState.logMessage("You cannot rest while enemies are chasing you!");
            return;
        }

        int oldHp = player.getHp(), oldMp = player.getMp();
        player.rest();
        triggerRestEffects(player, oldHp, oldMp);
    }

    private boolean isBeingChased() {
        return gameState.getCurrentEnemies().stream()
                .anyMatch(e -> !e.isDead() && e.hasNoticedPlayer());
    }

    private void triggerRestEffects(AbstractPlayer player, int oldHp, int oldMp) {
        int hpRestored = player.getHp() - oldHp, mpRestored = player.getMp() - oldMp;
        if (hpRestored > 0) player.triggerHealingEffect();
        if (mpRestored > 0) player.triggerManaEffect();
        logRestoration("Rested", player, hpRestored, mpRestored);
    }

    // ====== ATTACKING ======
    public void attack(int attackType) {
        AbstractPlayer player = gameState.getPlayer();
        if (!player.canAttack(attackType)) {
            if (player.getRemainingCooldown(attackType) == 0)
                logMessage("Not enough MP for " + player.getAttackDisplayName(attackType) + "!");
            return;
        }

        int mpBefore = player.getMp();
        player.attack(attackType);
        if (!isAttackSuccessful(player, mpBefore)) return;

        if (player instanceof Wizard wizard) handleWizardAttack(wizard, attackType);
        else handleMeleeAttack(player, attackType);
    }

    private boolean isAttackSuccessful(AbstractPlayer player, int mpBefore) {
        return !(player instanceof Wizard) || player.getMp() < mpBefore;
    }

    private void handleWizardAttack(Wizard wizard, int attackType) {
        Projectile proj = wizard.createProjectile(attackType, gameState.getCurrentEnemies(), gameState.getFogOfWar());
        if (proj != null) {
            addProjectile(proj);
            String spell = Wizard.getAttackName(attackType);
            logMessage(wizard.getName() + " casts " + spell + "!");
        } else logMessage("No targets in sight. The spell fizzles.");
    }

    private void handleMeleeAttack(AbstractPlayer player, int attackType) {
        for (Enemy target : gameState.getCurrentEnemies()) {
            if (target.isDead()) continue;
            if (!GeometryHelpers.isCardinallyAdjacent(player.getX(), player.getY(), target.getX(), target.getY())) continue;

            int dmg = player.getAttackDamage(attackType);
            logMessage(player.getName() + " attacks " + target.getName() + " for " + dmg + " damage!");
            boolean dead = target.takeDamage(dmg);

            if (player.getEquippedWeapon() != null) player.getEquippedWeapon().applyOnHitEffect(player, dmg);
            if (dead) handleEnemyDefeat(player, target);
            return; // only one enemy per attack
        }
    }

    private void handleEnemyDefeat(AbstractPlayer player, Enemy target) {
        int exp = target.getExpReward();
        int levels = player.addExperience(exp);
        logMessage(target.getName() + " has been defeated! You gained " + exp + " exp!", "accent");
        if (levels > 0) logLevelUp(player, levels);
    }

    // ====== ITEMS ======
    public void useItem(int slot) {
        AbstractPlayer player = gameState.getPlayer();
        Item item = player.getInventory().getItem(slot);
        if (item == null) return;

        if (item instanceof Potion potion) {
            int oldHp = player.getHp(), oldMp = player.getMp();
            player.useItem(slot);
            logRestoration("Used " + potion.getName(), player, player.getHp() - oldHp, player.getMp() - oldMp);
        } else player.useItem(slot);
    }

    public void pickupItem() {
        AbstractPlayer player = gameState.getPlayer();
        Tile tile = gameState.getCurrentMap().getTile(player.getX(), player.getY());
        if (!tile.hasItem()) return;

        Optional<Item> itemOpt = tile.getItem();
        if (itemOpt.isEmpty() || itemOpt.get() == null) {
            tile.removeItem();
            logMessage("You find nothing here...");
            return;
        }

        Item item = itemOpt.get();
        if (handleLegendaryItem(item, player, tile)) return;
        handleInventoryAdd(item, player, tile);
    }

    private boolean handleLegendaryItem(Item item, AbstractPlayer player, Tile tile) {
        if (!(item instanceof items.ShardOfJudgement shard)) return false;
        tile.removeItem();
        logMessage("You found the legendary " + shard.getName() + "!", "shardCyan");
        shard.use(player);
        return true;
    }

    private void handleInventoryAdd(Item item, AbstractPlayer player, Tile tile) {
        Inventory inv = player.getInventory();
        if (!inv.addItem(item, player)) { logMessage("Inventory is full!"); return; }

        int levels = player.addExperience(ITEM_PICKUP_XP);
        if (inv.wasLastAddWeaponUpgrade()) {
            Weapon w = (Weapon) item;
            logMessage("Upgraded " + w.getName() + " (+" + w.getDamageBonus() + " dmg)! +" + ITEM_PICKUP_XP + " exp", "accent");
        } else if (inv.wasLastAddWeaponDowngrade()) {
            logMessage("You already have a better " + item.getName() + ". +" + ITEM_PICKUP_XP + " exp", "accent");
        } else {
            logMessage("Picked up: " + item.getName() + " (+" + ITEM_PICKUP_XP + " exp)", "accent");
        }

        if (levels > 0) logLevelUp(player, levels);
        tile.removeItem();
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
        if (!(item instanceof AbstractTrap trap)) return;

        AbstractPlayer player = gameState.getPlayer();
        int dmg = trap.getDamage();
        boolean dead = player.takeDamage(dmg);
        logMessage(trap.getTriggerMessage() + " " + trap.getDamageMessage());
        logDamage(player.getName(), dmg, player.getHp(), player.getMaxHp());

        if (dead && !gameState.isGameOver()) handlePlayerTrapDeath(player, trap);
        tile.removeItem();
    }

    private void handlePlayerTrapDeath(AbstractPlayer player, AbstractTrap trap) {
        gameState.setGameOver(true);
        logMessage("The " + trap.getName() + " was LETHAL! " + player.getName() + " has been defeated!", "deathRed");

        javax.swing.SwingUtilities.invokeLater(() -> {
            ui.GameOverWindow win = new ui.GameOverWindow(null, player.getName(), gameState.getCurrentLevel());
            if (win.showGameOverDialog()) logMessage("Game restart requested - not yet implemented");
            else System.exit(0);
        });
    }

    // ====== LOGGING ======
    private void logDamage(String source, int damage, int hp, int maxHp) {
        logMessage(source + " took " + damage + " damage! (" + hp + "/" + maxHp + " HP)", "danger");
    }

    private void logLevelUp(AbstractPlayer player, int levelsGained) {
        if (levelsGained == 1)
            logMessage(player.getName() + " reached level " + player.getLevel() + "! (HP: " + player.getMaxHp() + ", MP: " + player.getMaxMp() + ")", "victoryGold");
        else
            logMessage(player.getName() + " gained " + levelsGained + " levels! Now level " + player.getLevel() + "!", "victoryGold");
    }
    
    private void logRestoration(String source, AbstractPlayer player, int hp, int mp) {
        if (hp > 0) logMessage(source + ": Restored " + hp + " HP (" + player.getHp() + "/" + player.getMaxHp() + " HP)", "success");
        if (mp > 0) logMessage(source + ": Restored " + mp + " MP (" + player.getMp() + "/" + player.getMaxMp() + " MP)", "success");
        if (hp == 0 && mp == 0) logMessage(source + ": No effect, already at full stats.");
    }

    private void logMessage(String msg) { gameState.logMessage(msg); }
    private void logMessage(String msg, String colorKey) { gameState.logMessage(msg, StyleConfig.getColor(colorKey)); }

}
