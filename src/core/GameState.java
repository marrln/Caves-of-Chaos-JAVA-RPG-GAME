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
 * Manages overall game state: player, maps, enemies, fog of war, collision, music, levels.
 */
public final class GameState {
    private static GameState instance;

    private final List<GameMap> maps = new ArrayList<>();
    private int currentLevel;
    private final AbstractPlayer player;
    private FogOfWar fogOfWar;                  
    private final List<Enemy> currentEnemies = new ArrayList<>();
    private boolean gameOver, medusaDeathHandled = false;

    private GameUIManager uiManager;        
    private utils.CollisionManager collisionManager;
    private final MusicManager musicManager = MusicManager.getInstance();
    private final ItemSpawner itemSpawner = new ItemSpawner();

    private static final int MAP_WIDTH = Config.getIntSetting("mapWidth");
    private static final int MAP_HEIGHT = Config.getIntSetting("mapHeight");
    private static final double MAP_FILL_PERCENTAGE = Config.getDoubleSetting("mapFillPercentage");
    private static final int MAP_ITERATIONS = Config.getIntSetting("mapIterations");
    private static final int CAVE_MAX_LEVEL = Config.getIntSetting("caveLevelNumber");
    private static final int VISION_RADIUS = Config.getIntSetting("visionRadius");

    public static GameState getInstance() { 
        if (instance == null) throw new IllegalStateException("GameState not initialized!");
        return instance; 
    }

    public GameState(AbstractPlayer player, int startingLevel) {
        if (instance != null) throw new IllegalStateException("GameState already exists!");
        instance = this;

        this.player = player;
        this.currentLevel = Math.max(0, Math.min(startingLevel, CAVE_MAX_LEVEL - 1));
        this.fogOfWar = new FogOfWar(MAP_WIDTH, MAP_HEIGHT);
        musicManager.startExplorationMusic();
        initGame();
    }

    private void initGame() {
        for (int i = 0; i <= currentLevel; i++) maps.add(createMap(i, true));

        GameMap startMap = getCurrentMap();
        player.setPosition(startMap.getEntranceX(), startMap.getEntranceY());
        fogOfWar.updateVisibility(startMap, player.getX(), player.getY(), VISION_RADIUS);

        setupCollision();
        loadEnemiesForCurrentLevel();
    }

    private GameMap createMap(int level, boolean allowEntrance) {
        GameMap map = new GameMap(MAP_WIDTH, MAP_HEIGHT);
        map.generateCaves(MAP_FILL_PERCENTAGE, MAP_ITERATIONS, level == CAVE_MAX_LEVEL - 1, allowEntrance);
        itemSpawner.spawnItemsForLevel(map, level + 1, player);
        return map;
    }

    private void setupCollision() {
        collisionManager = new utils.CollisionManager(getCurrentMap());
        updateCollisionEntities();
        enemies.AbstractEnemy.setCollisionManager(collisionManager);
        AbstractPlayer.setCollisionManager(collisionManager);
        enemies.AbstractEnemy.setCombatLogger(this::logMessage);
    }

    private void updateCollisionEntities() {
        List<utils.CollisionManager.Positionable> entities = new ArrayList<>();
        entities.add(player);
        for (Enemy e : currentEnemies)
            if (e instanceof utils.CollisionManager.Positionable p && !e.isDead()) entities.add(p);
        collisionManager.updateEntities(entities);
    }

    public boolean movePlayer(int dx, int dy) {
        if (player == null || gameOver) return false;
        int nx = player.getX() + dx, ny = player.getY() + dy;
        GameMap map = getCurrentMap();
        if (!map.isInBounds(nx, ny) || map.isBlocked(nx, ny)) return false;

        Tile tile = map.getTile(nx, ny);
        return switch (tile.getType()) {
            case Tile.STAIRS_DOWN -> { goToNextLevel(); yield true; }
            case Tile.STAIRS_UP   -> { goToPreviousLevel(); yield true; }
            default -> { if (player.tryMoveTo(nx, ny)) { updateFogOfWar(); yield true; } else yield false; }
        };
    }

    public void goToNextLevel() { loadLevel(currentLevel + 1); }
    public void goToPreviousLevel() { if (currentLevel > 0) loadLevel(currentLevel - 1); }

    private void loadLevel(int targetLevel) {
        currentLevel = targetLevel >= maps.size() ? maps.size() : targetLevel;
        if (currentLevel >= maps.size()) maps.add(createMap(currentLevel, false));

        GameMap map = getCurrentMap();
        fogOfWar = new FogOfWar(map.getWidth(), map.getHeight());
        setupCollision();
        loadEnemiesForCurrentLevel();
        itemSpawner.spawnItemsForLevel(map, currentLevel + 1, player);

        if (!player.tryMoveTo(map.getEntranceX(), map.getEntranceY())) 
            player.setPosition(map.getEntranceX(), map.getEntranceY());
        updateFogOfWar();
    }

    public void updateFogOfWar() {
        fogOfWar.updateVisibility(getCurrentMap(), player.getX(), player.getY(), VISION_RADIUS);
        if (uiManager != null) uiManager.centerCameraOnPlayer();
    }

    public void loadEnemiesForCurrentLevel() {
        currentEnemies.clear();
        currentEnemies.addAll(EnemySpawner.spawnEnemiesForLevel(getCurrentMap(), currentLevel + 1));
        updateCollisionEntities();
    }

    public void updateEnemies() {
        if (player == null || gameOver) return;
        player.updateCombat();

        for (Enemy e : currentEnemies) {
            e.update(player.getX(), player.getY());
            if (e.isDead() || !(e instanceof enemies.AbstractEnemy ae)) continue;

            if (ae.hasPendingPlayerDamage()) {
                int dmg = ae.getPendingPlayerDamage();
                logMessage(player.getName() + " takes " + dmg + " damage!", StyleConfig.getColor("danger"));
                if (player.takeDamage(dmg) && !gameOver) {
                    gameOver = true;
                    logMessage(player.getName() + " has been defeated!", StyleConfig.getColor("deathRed"));
                    handlePlayerDeath();
                    return;
                }
            }
        }
        updateCollisionEntities();
        musicManager.updateForCombatState(currentEnemies);
    }

    public void checkMedusaDeath() {
        if (currentLevel != CAVE_MAX_LEVEL - 1 || gameOver || currentEnemies.isEmpty()) return;
        for (Enemy e : currentEnemies)
            if (e instanceof enemies.MedusaOfChaos m && m.isDead() && !medusaDeathHandled) {
                updateCollisionEntities();
                handleMedusaDeath(m.getX(), m.getY());
                medusaDeathHandled = true;
                return;
            }
    }

    private void handlePlayerDeath() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ui.GameOverWindow win = new ui.GameOverWindow(null, player.getName(), currentLevel);
            if (win.showGameOverDialog()) logMessage("Game restart requested - not yet implemented");
            else System.exit(0);
        });
    }

    public void handleMedusaDeath(int x, int y) {
        logMessage("The Medusa of Chaos has been defeated! The evil presence lifts...", StyleConfig.getColor("victoryGold"));
        logMessage("A brilliant shard of light materializes where the beast fell!", StyleConfig.getColor("shardCyan"));
        boolean spawned = itemSpawner.spawnShardOfJudgement(getCurrentMap(), x, y);
        logMessage(spawned ? 
            "The legendary Shard of Judgement awaits your claim!" : 
            "The Shard of Judgement failed to spawn, but you have still won!",
            StyleConfig.getColor(spawned ? "shardCyan" : "victoryGold"));
    }

    // ====== LOGGING ======
    public void logMessage(String msg) { if (uiManager != null) uiManager.addMessage(msg); }
    public void logMessage(String msg, java.awt.Color color) { if (uiManager != null) uiManager.addMessage(msg, color); }

    // ====== GETTERS & CONVENIENCE ======
    public void setUIManager(GameUIManager ui) { this.uiManager = ui; }
    public GameMap getCurrentMap() { return maps.get(currentLevel); }
    public AbstractPlayer getPlayer() { return player; }
    public FogOfWar getFogOfWar() { return fogOfWar; }
    public List<Enemy> getCurrentEnemies() { return currentEnemies; }
    public int getAliveEnemyCount() { return (int) currentEnemies.stream().filter(e -> !e.isDead()).count(); }
    public int getCurrentLevel() { return currentLevel; }
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean val) { gameOver = val; }
    public boolean canGoToNextLevel() { return currentLevel < (CAVE_MAX_LEVEL - 1); }
    public String getLevelDisplayString() { return "Cave Floor: " + (currentLevel + 1) + " of " + CAVE_MAX_LEVEL; }
}
