package core;

import audio.MusicManager;
import config.Config;
import enemies.Enemy;
import java.util.ArrayList;
import java.util.List;
import map.FogOfWar;
import map.GameMap;
import map.Tile;
import player.AbstractPlayer;
import player.PlayerFactory;
import ui.GameUIManager;

/**
 * Manages the overall game state including player, maps, enemies,
 * fog of war, collision, music, and level progression.
 */
public class GameState {

    // ====== CORE GAME STATE ======
    private final List<GameMap> maps;
    private int currentLevel;
    private final int maxLevel;
    private GameMap map;
    private final AbstractPlayer player;
    private FogOfWar fogOfWar; // re-created on level change
    private List<Enemy> currentEnemies; // reassigned on spawn
    private boolean gameOver;

    // ====== SYSTEM REFERENCES ======
    private GameUIManager uiManager; // for logging
    private utils.CollisionManager collisionManager; // for movement/collision
    private final MusicManager musicManager; // dynamic music
    private boolean anyEnemyHasNoticedPlayer = false;

    // ====== CONSTRUCTORS ======
    public GameState(String playerClass) {
        this.maps = new ArrayList<>();
        this.currentEnemies = new ArrayList<>();
        this.maxLevel = getMaxCaveLevelsFromConfigImpl();
        this.currentLevel = 0;
        this.gameOver = false;

        this.musicManager = MusicManager.getInstance();
        this.musicManager.startExplorationMusic();

        int[] mapDimensions = getMapDimensionsFromConfig();
        int mapWidth = mapDimensions[0];
        int mapHeight = mapDimensions[1];

        this.fogOfWar = new FogOfWar(mapWidth, mapHeight);

        this.map = new GameMap(mapWidth, mapHeight);
        this.map.generateCaves(getMapFillPercentageFromConfig(), getMapIterationsFromConfig(), false);
        maps.add(this.map);

        this.player = PlayerFactory.createPlayer(playerClass, map.getEntranceX(), map.getEntranceY());

        fogOfWar.updateVisibility(map, player.getX(), player.getY(), getVisionRadiusFromConfig());

        setupEnemyCollisionChecking();
        initializeEnemiesForLevel();
    }

    public GameState(AbstractPlayer player, int mapWidth, int mapHeight, double fillPercentage) {
        this.maps = new ArrayList<>();
        this.currentEnemies = new ArrayList<>();
        this.maxLevel = getMaxCaveLevelsFromConfigImpl();
        this.currentLevel = 0;
        this.gameOver = false;

        this.musicManager = MusicManager.getInstance();
        this.musicManager.startExplorationMusic();

        this.player = player;
        this.fogOfWar = new FogOfWar(mapWidth, mapHeight);

        this.map = new GameMap(mapWidth, mapHeight);
        this.map.generateCaves(fillPercentage, getMapIterationsFromConfig(), false);
        maps.add(this.map);

        player.setPosition(map.getEntranceX(), map.getEntranceY());

        fogOfWar.updateVisibility(map, player.getX(), player.getY(), getVisionRadiusFromConfig());

        setupEnemyCollisionChecking();
        initializeEnemiesForLevel();
    }

    // ====== COLLISION MANAGEMENT ======
    private void setupEnemyCollisionChecking() {
        collisionManager = new utils.CollisionManager(map);
        updateCollisionEntities();

        enemies.AbstractEnemy.setCollisionManager(collisionManager);
        AbstractPlayer.setCollisionManager(collisionManager);

        enemies.AbstractEnemy.setCombatLogger(this::logCombatMessage);
    }

    private void updateCollisionEntities() {
        if (collisionManager == null) return;

        List<utils.CollisionManager.Positionable> entities = new ArrayList<>();
        if (player != null) entities.add(player);

        if (currentEnemies != null) {
            for (Enemy enemy : currentEnemies) {
                if (enemy instanceof utils.CollisionManager.Positionable pos && !enemy.isDead()) {
                    entities.add(pos);
                }
            }
        }
        collisionManager.updateEntities(entities);
    }

    // ====== LEVEL GENERATION ======
    public void generateLevel(int mapWidth, int mapHeight, double fillPercentage, int iterations) {
        GameMap map = new GameMap(mapWidth, mapHeight);

        boolean isFinalLevel = (maps.size() + 1) >= getMaxCaveLevelsFromConfigImpl();
        map.generateCaves(fillPercentage, iterations, isFinalLevel);

        maps.add(map);
    }

    // ====== FOG OF WAR ======
    public void updateFogOfWar() {
        if (fogOfWar != null && player != null) {
            fogOfWar.updateVisibility(getCurrentMap(), player.getX(), player.getY(), getVisionRadiusFromConfig());
        }
    }

    // ====== PLAYER MOVEMENT ======
    public boolean movePlayer(int dx, int dy) {
        if (player == null || gameOver) return false;

        int newX = player.getX() + dx;
        int newY = player.getY() + dy;
        GameMap currentMap = getCurrentMap();

        if (currentMap.isInBounds(newX, newY) && !currentMap.isBlocked(newX, newY)) {
            Tile destinationTile = currentMap.getTile(newX, newY);

            return switch (destinationTile.getType()) {
                case Tile.STAIRS_DOWN -> { goToNextLevel(); yield true; }
                case Tile.STAIRS_UP   -> { goToPreviousLevel(); yield true; }
                default -> {
                    player.setPosition(newX, newY);
                    updateFogOfWar();
                    yield true;
                }
            };
        }
        return false;
    }

    // ====== LEVEL PROGRESSION ======
    public void goToNextLevel() {
        int nextLevel = currentLevel + 1;

        if (nextLevel >= maps.size()) {
            int[] dims = getMapDimensionsFromConfig();
            generateLevel(dims[0], dims[1], getMapFillPercentageFromConfig(), getMapIterationsFromConfig());
        }

        currentLevel = nextLevel;

        GameMap newMap = getCurrentMap();
        player.setPosition(newMap.getEntranceX(), newMap.getEntranceY());

        fogOfWar = new FogOfWar(newMap.getWidth(), newMap.getHeight());
        updateFogOfWar();

        spawnEnemiesForCurrentLevel();
    }

    public void goToPreviousLevel() {
        if (currentLevel > 0) {
            currentLevel--;

            GameMap newMap = getCurrentMap();
            player.setPosition(newMap.getExitX(), newMap.getExitY());

            fogOfWar = new FogOfWar(newMap.getWidth(), newMap.getHeight());
            updateFogOfWar();

            spawnEnemiesForCurrentLevel();
        }
    }

    // ====== ENEMY MANAGEMENT ======
    private void initializeEnemiesForLevel() {
        currentEnemies.clear();
        currentEnemies.addAll(EnemySpawner.spawnEnemiesForLevel(getCurrentMap(), currentLevel + 1));
    }

    public void spawnEnemiesForCurrentLevel() {
        currentEnemies.clear();
        currentEnemies.addAll(EnemySpawner.spawnEnemiesForLevel(getCurrentMap(), currentLevel + 1));
    }

    public void updateEnemies() {
        if (player == null) return;

        boolean currentlyCombatActive = false;

        for (Enemy enemy : currentEnemies) {
            if (!enemy.isDead()) {
                enemy.update(player.getX(), player.getY());

                if (enemy instanceof enemies.AbstractEnemy abstractEnemy) {
                    if (abstractEnemy.hasNoticedPlayer()) currentlyCombatActive = true;

                    if (abstractEnemy.hasPendingPlayerDamage()) {
                        int damage = abstractEnemy.getPendingPlayerDamage();
                        logCombatMessage(player.getName() + " takes " + damage + " damage!");

                        if (player.takeDamage(damage)) {
                            logCombatMessage(player.getName() + " has been defeated!");
                            // TODO: handle game over properly
                        }
                    }
                }
            }
        }

        updateMusicForCombatState(currentlyCombatActive);

        if (currentEnemies.removeIf(Enemy::isDead)) {
            updateCollisionEntities();
        }
    }

    public boolean areAllEnemiesDefeated() {
        return currentEnemies.stream().allMatch(Enemy::isDead);
    }

    // ====== MUSIC MANAGEMENT ======
    private void updateMusicForCombatState(boolean combatActive) {
        if (combatActive && !anyEnemyHasNoticedPlayer) {
            anyEnemyHasNoticedPlayer = true;
            musicManager.startCombatMusic();
        } else if (!combatActive && anyEnemyHasNoticedPlayer) {
            anyEnemyHasNoticedPlayer = false;
            musicManager.endCombatMusic();
        }
    }

    // ====== UI LOGGING ======
    public void logCombatMessage(String message) {
        if (uiManager != null) {
            uiManager.addLogMessage(message);
        } else {
            System.out.println(message);
        }
    }

    public void setUIManager(GameUIManager uiManager) {
        this.uiManager = uiManager;
    }

    // ====== GETTERS & STATE ======
    public GameMap getCurrentMap() { return maps.get(currentLevel); }
    public AbstractPlayer getPlayer() { return player; }
    public FogOfWar getFogOfWar() { return fogOfWar; }
    public List<Enemy> getCurrentEnemies() { return currentEnemies; }
    public int getCurrentLevel() { return currentLevel; }
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public boolean canGoToNextLevel() { return currentLevel < (getMaxCaveLevelsFromConfigImpl() - 1); }
    public String getLevelDisplayString() { return "Cave Floor: " + (currentLevel + 1) + " of " + getMaxCaveLevelsFromConfigImpl(); }

    // ====== CONFIG HELPERS ======
    private int[] getMapDimensionsFromConfig() {
        String mapSizeStr = Config.getSetting("mapSize");
        int wIdx = mapSizeStr.indexOf("width=\"");
        int hIdx = mapSizeStr.indexOf("height=\"");
        int wEnd = mapSizeStr.indexOf('"', wIdx + 7);
        int hEnd = mapSizeStr.indexOf('"', hIdx + 8);

        int width = Integer.parseInt(mapSizeStr.substring(wIdx + 7, wEnd));
        int height = Integer.parseInt(mapSizeStr.substring(hIdx + 8, hEnd));
        return new int[]{width, height};
    }
    private double getMapFillPercentageFromConfig() { return Double.parseDouble(Config.getSetting("mapFillPercentage").trim()); }
    private int getMapIterationsFromConfig() { return Integer.parseInt(Config.getSetting("mapIterations").trim()); }
    private int getVisionRadiusFromConfig() { return Integer.parseInt(Config.getSetting("visionRadius").trim()); }
    private int getMaxCaveLevelsFromConfigImpl() { return Integer.parseInt(Config.getSetting("caveLevelNumber").trim()); }
}