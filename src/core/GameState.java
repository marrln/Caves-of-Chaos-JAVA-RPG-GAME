package core;

import audio.MusicManager;
import config.Config;
import enemies.Enemy;
import java.util.ArrayList;
import java.util.List;
import map.FogOfWar;
import map.GameMap;
import player.AbstractPlayer;
import ui.GameUIManager;
import utils.CollisionSystem;
import utils.Positionable;

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

    private EventLogger logger;        
    private GameUIManager uiManager;        
    private CollisionSystem collisionManager;
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
        collisionManager = new CollisionSystem(getCurrentMap());
        updateCollisionEntities();
        enemies.AbstractEnemy.setCollisionManager(collisionManager);
        AbstractPlayer.setCollisionManager(collisionManager);
        enemies.AbstractEnemy.setCombatLogger((msg) -> {
            if (logger != null) logger.log(msg);
        });
    }

    private void updateCollisionEntities() {
        List<Positionable> entities = new ArrayList<>();
        entities.add(player);
        for (Enemy e : currentEnemies)
            if (e instanceof Positionable p && !e.isDead()) entities.add(p);
        collisionManager.updateEntities(entities);
    }

    public void goToNextLevel() { loadLevel(currentLevel + 1, true); }
    public void goToPreviousLevel() { if (currentLevel > 0) loadLevel(currentLevel - 1, false); }

    private void loadLevel(int targetLevel, boolean descendingDown) {
        currentLevel = targetLevel;
        
        if (currentLevel >= maps.size()) {
            maps.add(createMap(currentLevel, false));
        }

        GameMap map = getCurrentMap();
        fogOfWar = new FogOfWar(map.getWidth(), map.getHeight());
        setupCollision();
        loadEnemiesForCurrentLevel();
        int spawnX = descendingDown ? map.getEntranceX() : map.getExitX();
        int spawnY = descendingDown ? map.getEntranceY() : map.getExitY();
        
        if (!player.tryMoveTo(spawnX, spawnY)) player.setPosition(spawnX, spawnY);
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
                if (logger != null) logger.logPlayerTakesDamage(player.getName(), dmg);
                if (player.takeDamage(dmg) && !gameOver) {
                    gameOver = true;
                    if (logger != null) logger.logPlayerDefeated(player.getName());
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
            if (win.showGameOverDialog()) {
                if (logger != null) logger.logGameRestartRequested();
            } else {
                System.exit(0);
            }
        });
    }

    public void handleMedusaDeath(int x, int y) {
        if (logger != null) {
            logger.logMedusaDefeated();
            logger.logShardAppears();
        }
        boolean spawned = itemSpawner.spawnShardOfJudgement(getCurrentMap(), x, y);
        if (logger != null) {
            if (spawned) {
                logger.logShardAwaits();
            } else {
                logger.logShardSpawnFailed();
            }
        }
    }

    // ====== GETTERS & CONVENIENCE ======
    public EventLogger getLogger() { return logger; }
    public void setLogger(EventLogger logger) { this.logger = logger; }
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
