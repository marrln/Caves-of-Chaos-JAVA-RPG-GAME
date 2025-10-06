package core;

import audio.MusicManager;
import config.Config;
import config.StyleConfig;
import enemies.Enemy;
import java.util.ArrayList;
import java.util.List;
import map.FogOfWar;
import map.GameMap;
import map.Tile;
import player.AbstractPlayer;
import ui.GameUIManager;

/**
 * Manages the overall game state:
 * - Player, maps, enemies, fog of war
 * - Collision handling, music, and level progression
 */
public final class GameState {
    // ====== SINGLETON INSTANCE ======
    private static GameState instance;

    // ====== CORE GAME STATE ======
    private final List<GameMap> maps;
    private int currentLevel;
    private final AbstractPlayer player;
    private FogOfWar fogOfWar;                  
    private final List<Enemy> currentEnemies;   
    private boolean gameOver;
    
    // ====== DEBUG FLAGS ======
    private boolean medusaDeathHandled = false; // Tracks if we handled Medusa's death

    // ====== SYSTEM REFERENCES ======
    private GameUIManager uiManager;        
    private utils.CollisionManager collisionManager;
    private final MusicManager musicManager;
    private final ItemSpawner itemSpawner;

    // ====== CONFIG CONSTANTS  ======
    private static final int MAP_WIDTH = Config.getIntSetting("mapWidth");
    private static final int MAP_HEIGHT = Config.getIntSetting("mapHeight");
    private static final double MAP_FILL_PERCENTAGE = Config.getDoubleSetting("mapFillPercentage");
    private static final int MAP_ITERATIONS = Config.getIntSetting("mapIterations");
    private static final int CAVE_MAX_LEVEL = Config.getIntSetting("caveLevelNumber");
    private static final int VISION_RADIUS = Config.getIntSetting("visionRadius");

    /** Returns the singleton instance of GameState */
    public static GameState getInstance() {
        if (instance == null) {
            throw new IllegalStateException("GameState has not been initialized!");
        }
        return instance;
    }

    // ====== CONSTRUCTOR ======
    public GameState(AbstractPlayer player, int startingLevel) {
        if (instance != null) {
            throw new IllegalStateException("GameState instance already exists!");
        }
        instance = this;
        this.maps = new ArrayList<>();
        this.currentEnemies = new ArrayList<>();
        this.currentLevel = Math.max(0, Math.min(startingLevel, CAVE_MAX_LEVEL - 1));
        this.gameOver = false;

        this.musicManager = MusicManager.getInstance();
        this.musicManager.startExplorationMusic();
        this.itemSpawner = new ItemSpawner();

        this.player = player;
        this.fogOfWar = new FogOfWar(MAP_WIDTH, MAP_HEIGHT);

        initGame();
    }

    /** Performs initial setup after construction */
    private void initGame() {
        for (int level = 0; level <= currentLevel; level++) {
            maps.add(createAndPopulateMap(level, true));
        }

        GameMap startMap = getCurrentMap();
        player.setPosition(startMap.getEntranceX(), startMap.getEntranceY());

        fogOfWar.updateVisibility(startMap, player.getX(), player.getY(), VISION_RADIUS);
        setupEnemyCollisionChecking();
        loadEnemiesForCurrentLevel();
    }

    /** Creates and fully populates a new map */
    private GameMap createAndPopulateMap(int level, boolean allowEntrance) {
        GameMap map = new GameMap(MAP_WIDTH, MAP_HEIGHT);
        boolean isFinalLevel = (level == CAVE_MAX_LEVEL - 1);

        map.generateCaves(
            MAP_FILL_PERCENTAGE,
            MAP_ITERATIONS,
            isFinalLevel,
            allowEntrance
        );

        itemSpawner.spawnItemsForLevel(map, level + 1, player);
        return map;
    }

    // ====== COLLISION MANAGEMENT ======
    private void setupEnemyCollisionChecking() {
        collisionManager = new utils.CollisionManager(getCurrentMap());
        updateCollisionEntities();

        enemies.AbstractEnemy.setCollisionManager(collisionManager);
        AbstractPlayer.setCollisionManager(collisionManager);

        enemies.AbstractEnemy.setCombatLogger(this::logMessage);
    }

    private void updateCollisionEntities() {
        if (collisionManager == null) return;

        List<utils.CollisionManager.Positionable> entities = new ArrayList<>();
        entities.add(player);
        for (Enemy enemy : currentEnemies) {
            if (enemy instanceof utils.CollisionManager.Positionable pos && !enemy.isDead()) {
                entities.add(pos);
            }
        }
        collisionManager.updateEntities(entities);
    }

    // ====== LEVEL GENERATION ======
    public void generateLevel(int mapWidth, int mapHeight, double fillPercentage, int iterations) {
        int level = maps.size();
        maps.add(createAndPopulateMap(level, false));
    }

    // ====== FOG OF WAR ======
    public void updateFogOfWar() {
        if (fogOfWar != null) {
            fogOfWar.updateVisibility(
                getCurrentMap(),
                player.getX(),
                player.getY(),
                VISION_RADIUS
            );
        }
    }

    // ====== PLAYER MOVEMENT ======
    public boolean movePlayer(int dx, int dy) {
        if (player == null || gameOver) return false;

        int newX = player.getX() + dx;
        int newY = player.getY() + dy;
        GameMap currentMap = getCurrentMap();

        if (currentMap.isInBounds(newX, newY) && !currentMap.isBlocked(newX, newY)) {
            Tile dest = currentMap.getTile(newX, newY);

            return switch (dest.getType()) {
                case Tile.STAIRS_DOWN -> { goToNextLevel(); yield true; }
                case Tile.STAIRS_UP   -> { goToPreviousLevel(); yield true; }
                default -> {
                    if (player.tryMoveTo(newX, newY)) {
                        updateFogOfWar();
                        yield true;
                    } else yield false;
                }
            };
        }
        return false;
    }

    // ====== LEVEL PROGRESSION ======
    public void goToNextLevel() {
        int nextLevel = currentLevel + 1;
        if (nextLevel >= maps.size()) generateLevel(MAP_WIDTH, MAP_HEIGHT, MAP_FILL_PERCENTAGE, MAP_ITERATIONS);
        currentLevel = nextLevel;
        GameMap newMap = getCurrentMap();
        loadLevel(newMap.getEntranceX(), newMap.getEntranceY());
    }

    public void goToPreviousLevel() {
        if (currentLevel > 0) {
            currentLevel--;
            GameMap newMap = getCurrentMap();
            loadLevel(newMap.getExitX(), newMap.getExitY());
        }
    }

    private void loadLevel(int playerX, int playerY) {
        GameMap newMap = getCurrentMap();
        fogOfWar = new FogOfWar(newMap.getWidth(), newMap.getHeight());
        setupEnemyCollisionChecking();
        loadEnemiesForCurrentLevel();
        itemSpawner.spawnItemsForLevel(newMap, currentLevel + 1, player);

        if (!player.tryMoveTo(playerX, playerY)) {
            player.setPosition(playerX, playerY);
        }
        updateFogOfWar();
    }

    // ====== ENEMY MANAGEMENT ======
    public void loadEnemiesForCurrentLevel() {
        currentEnemies.clear();
        currentEnemies.addAll(
            EnemySpawner.spawnEnemiesForLevel(getCurrentMap(), currentLevel + 1)
        );
        updateCollisionEntities();
    }

    public void updateEnemies() {
        if (player == null || gameOver) return; // Stop all enemy updates if game is over
        player.updateCombat();

        for (Enemy enemy : currentEnemies) {
            enemy.update(player.getX(), player.getY());

            if (!enemy.isDead() && enemy instanceof enemies.AbstractEnemy abstractEnemy) {
                if (abstractEnemy.hasPendingPlayerDamage()) {
                    int dmg = abstractEnemy.getPendingPlayerDamage();
                    logMessage(player.getName() + " takes " + dmg + " damage!", 
                        StyleConfig.getColor("danger")); // Red for player taking damage

                    if (player.takeDamage(dmg)) {
                        if (!gameOver) {
                            gameOver = true;
                            logMessage(player.getName() + " has been defeated!", 
                                StyleConfig.getColor("deathRed")); // Bright red for death
                            handlePlayerDeath();
                            return; // CRITICAL: Stop processing immediately after player death
                        }
                    }
                }
            }
        }

        // Ensure collision entities are updated so dead enemies are no longer blocking
        updateCollisionEntities();
        musicManager.updateForCombatState(currentEnemies);
    }
    
    /**
     * Checks if Medusa died this game tick and handles the death event.
     * MUST be called AFTER combat resolution (melee, projectiles) but BEFORE next frame.
     * This is separate from updateEnemies() to handle death detection across multiple damage sources.
     * Only runs on the final level where Medusa spawns.
     */
    public void checkMedusaDeath() {
        // Skip if not on final level (Medusa only spawns on final level)
        if (currentLevel != CAVE_MAX_LEVEL - 1) return;
        
        // Skip if game is over or no enemies
        if (gameOver || currentEnemies.isEmpty()) return;
        
        // Find Medusa and check if she just died
        for (Enemy enemy : currentEnemies) {
            if (enemy instanceof enemies.MedusaOfChaos medusa) {
                // Check if Medusa is dead
                if (medusa.isDead()) {
                    // Handle death only once
                    if (!medusaDeathHandled) {
                        updateCollisionEntities();
                        handleMedusaDeath(medusa.getX(), medusa.getY());
                        medusaDeathHandled = true;
                    }
                    return;
                }
                return;
            }
        }
    }

    // ====== PLAYER DEATH & BOSS ======
    private void handlePlayerDeath() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ui.GameOverWindow win = new ui.GameOverWindow(null, player.getName(), currentLevel);
            if (win.showGameOverDialog()) {
                logMessage("Game restart requested - not yet implemented");
            } else {
                System.exit(0);
            }
        });
    }

    /**
     * Handles Medusa defeat and spawns the Shard of Judgement.
     * Called from checkMedusaDeath() when Medusa is killed by any damage source.
     */
    public void handleMedusaDeath(int x, int y) {
        logMessage("The Medusa of Chaos has been defeated! The evil presence lifts...", 
            StyleConfig.getColor("victoryGold")); // Victory gold for boss defeat
        logMessage("A brilliant shard of light materializes where the beast fell!", 
            StyleConfig.getColor("shardCyan")); // Cyan for the magical shard
        boolean spawned = itemSpawner.spawnShardOfJudgement(getCurrentMap(), x, y);
        if (spawned) {
            logMessage("The legendary Shard of Judgement awaits your claim!", 
                StyleConfig.getColor("shardCyan")); // Cyan for the legendary item
        } else {
            logMessage("The Shard of Judgement failed to spawn, but you have still won!", 
                StyleConfig.getColor("victoryGold")); // Gold - still a victory!
        }
    }

    // ====== CONVINIENCE ======
    public void setUIManager(GameUIManager ui) { this.uiManager = ui; }
    public GameMap getCurrentMap() { return maps.get(currentLevel); }
    public AbstractPlayer getPlayer() { return player; }
    public FogOfWar getFogOfWar() { return fogOfWar; }
    public List<Enemy> getCurrentEnemies() { return currentEnemies; }
    public int getAliveEnemyCount() { return (int) currentEnemies.stream().filter(enemy -> !enemy.isDead()).count(); }
    public int getCurrentLevel() { return currentLevel; }
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean val) { this.gameOver = val; }
    public boolean canGoToNextLevel() { return currentLevel < (CAVE_MAX_LEVEL - 1); }
    public String getLevelDisplayString() { return "Cave Floor: " + (currentLevel + 1) + " of " + CAVE_MAX_LEVEL; }
    public void logMessage(String msg) { uiManager.addMessage(msg); }
    public void logMessage(String msg, java.awt.Color color) { uiManager.addMessage(msg, color); }
}
