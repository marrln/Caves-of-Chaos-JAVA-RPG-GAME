package core;

import audio.SFXManager;
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
    private final EventLogger logger;
    private GameUIManager uiManager;
    private final ProjectileManager projectileManager;

    public GameController(GameState gameState, EventLogger logger) {
        this.gameState = gameState;
        this.logger = logger;
        this.projectileManager = new ProjectileManager();
    }

    public void setUIManager(GameUIManager uiManager) { 
        this.uiManager = uiManager;
        logger.setUIManager(uiManager);
    }

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
        logger.logFloorDescend(gameState.getCurrentLevel() + 1);
        return true;
    }

    private boolean handleStairsUp() {
        if (gameState.getCurrentLevel() <= 0) return false;
        gameState.goToPreviousLevel();
        logger.logFloorAscend(gameState.getCurrentLevel() + 1);
        return true;
    }

    // ====== RESTING ======
    public void rest() {
        AbstractPlayer player = gameState.getPlayer();
        if (!player.canRest()) return;
        if (isBeingChased()) {
            logger.logCannotRestWhileChased();
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
        logger.logRest(player, hpRestored, mpRestored);
    }

    // ====== ATTACKING ======
    public void attack(int attackType) {
        AbstractPlayer player = gameState.getPlayer();
        if (!player.canAttack(attackType)) {
            // Only show MP error if the attack actually costs MP and cooldown is ready
            if (player.getRemainingCooldown(attackType) == 0 && player.getAttackMpCost(attackType) > 0) {
                logger.logNotEnoughMp(player.getAttackDisplayName(attackType));
            }
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
            SFXManager.getInstance().playSpellCast();
            String spell = Wizard.getAttackName(attackType);
            logger.logSpellCast(wizard.getName(), spell);
        } else {
            logger.logSpellFizzle();
        }
    }

    private void handleMeleeAttack(AbstractPlayer player, int attackType) {
        for (Enemy target : gameState.getCurrentEnemies()) {
            if (target.isDead()) continue;
            if (!GeometryHelpers.isCardinallyAdjacent(player.getX(), player.getY(), target.getX(), target.getY())) continue;

            int dmg = player.getAttackDamage(attackType);
            
            // Play Duelist hit sound for successful melee attacks
            if (player.getPlayerClass().equals("Duelist")) {
                SFXManager.getInstance().playDuelistHit();
            }
            
            logger.logMeleeAttack(player.getName(), target.getName(), dmg);
            boolean dead = target.takeDamage(dmg);

            if (player.getEquippedWeapon() != null) player.getEquippedWeapon().applyOnHitEffect(player, dmg);
            if (dead) handleEnemyDefeat(player, target);
            return; // only one enemy per attack
        }
        
        // If we get here, the attack missed (no adjacent enemies)
        if (player.getPlayerClass().equals("Duelist")) {
            SFXManager.getInstance().playDuelistMiss();
        }
    }

    private void handleEnemyDefeat(AbstractPlayer player, Enemy target) {
        int exp = target.getExpReward();
        int levels = player.addExperience(exp);
        logger.logEnemyDefeated(target.getName(), exp);
        if (levels > 0) logger.logLevels(player, levels);
    }

    // ====== ITEMS ======
    public void useItem(int slot) {
        AbstractPlayer player = gameState.getPlayer();
        Item item = player.getInventory().getItem(slot);
        if (item == null) return;

        if (item instanceof Potion potion) {
            int oldHp = player.getHp(), oldMp = player.getMp();
            player.useItem(slot);
            logger.logPotionUsed(potion.getName(), player, player.getHp() - oldHp, player.getMp() - oldMp);
        } else player.useItem(slot);
    }

    public void pickupItem() {
        AbstractPlayer player = gameState.getPlayer();
        Tile tile = gameState.getCurrentMap().getTile(player.getX(), player.getY());
        if (!tile.hasItem()) return;

        Optional<Item> itemOpt = tile.getItem();
        if (itemOpt.isEmpty() || itemOpt.get() == null) {
            tile.removeItem();
            logger.logNothingHere();
            return;
        }

        Item item = itemOpt.get();
        if (handleLegendaryItem(item, player, tile)) return;
        handleInventoryAdd(item, player, tile);
    }

    private boolean handleLegendaryItem(Item item, AbstractPlayer player, Tile tile) {
        if (!(item instanceof items.ShardOfJudgement shard)) return false;
        tile.removeItem();
        logger.logLegendaryItemFound(shard.getName());
        shard.use(player);
        return true;
    }

    private void handleInventoryAdd(Item item, AbstractPlayer player, Tile tile) {
        Inventory inv = player.getInventory();
        if (!inv.addItem(item, player)) { 
            logger.logInventoryFull(); 
            return; 
        }

        SFXManager.getInstance().playItemPickup();
        int levels = player.addExperience(ITEM_PICKUP_XP);
        if (inv.wasLastAddWeaponUpgrade()) {
            Weapon w = (Weapon) item;
            logger.logWeaponUpgrade(w.getName(), w.getDamageBonus(), ITEM_PICKUP_XP);
        } else if (inv.wasLastAddWeaponDowngrade()) {
            logger.logWeaponDowngrade(item.getName(), ITEM_PICKUP_XP);
        } else {
            logger.logItemPickup(item.getName(), ITEM_PICKUP_XP);
        }

        if (levels > 0) logger.logLevels(player, levels);
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
        logger.logTrapTriggered(trap.getTriggerMessage(), trap.getDamageMessage());
        logger.logDamage(player.getName(), dmg, player.getHp(), player.getMaxHp());

        if (dead && !gameState.isGameOver()) handlePlayerTrapDeath(player, trap);
        tile.removeItem();
    }

    private void handlePlayerTrapDeath(AbstractPlayer player, AbstractTrap trap) {
        gameState.setGameOver(true);
        logger.logTrapLethal(trap.getName(), player.getName());

        javax.swing.SwingUtilities.invokeLater(() -> {
            ui.GameOverWindow win = new ui.GameOverWindow(null, player.getName(), gameState.getCurrentLevel());
            if (win.showGameOverDialog()) {
                logger.logGameRestartRequested();
            } else {
                System.exit(0);
            }
        });
    }
}
