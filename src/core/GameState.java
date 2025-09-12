package core;

import config.Config;
import enemies.Enemy;
import enemies.EnemySpawner;
import java.util.ArrayList;
import java.util.List;
import map.FogOfWar;
import map.GameMap;
import map.Tile;
import player.AbstractPlayer;
import player.PlayerFactory;
import ui.GameUIManager;

/**
 * Manages the overall game state including player, maps, and level progression.
 */
public class GameState {
    
    // Game state
    private final List<GameMap> maps;
    private int currentLevel;
    private final int maxLevel;
    private GameMap map;
    private final AbstractPlayer player;
    private FogOfWar fogOfWar; // Not final - needs reassignment on level change
    private List<Enemy> currentEnemies; // Not final - needs reassignment during spawning
    private boolean gameOver;
    
    // UI system reference for logging
    private GameUIManager uiManager;
    
    // Collision management system
    private utils.CollisionManager collisionManager;
    
    /**
     * Sets up collision checking for enemy movement using the new CollisionManager.
     */
    private void setupEnemyCollisionChecking() {
        // Create collision manager with the current map
        collisionManager = new utils.CollisionManager(map);
        
        // Update entities list for collision checking
        updateCollisionEntities();
        
        // Set collision manager for all enemies and players
        enemies.AbstractEnemy.setCollisionManager(collisionManager);
        AbstractPlayer.setCollisionManager(collisionManager);
        
        // Set combat logger for all enemies
        enemies.AbstractEnemy.setCombatLogger(this::logCombatMessage);
    }
    
    /**
     * Updates the collision manager with current entities.
     * Call this when entities are added/removed/killed.
     */
    private void updateCollisionEntities() {
        if (collisionManager == null) return;
        
        // Create list of all collidable entities
        List<utils.CollisionManager.Positionable> entities = new ArrayList<>();
        
        // Add player (now implements Positionable interface)
        if (player != null) {
            entities.add(player);
        }
        
        // Add all alive enemies
        if (currentEnemies != null) {
            for (Enemy enemy : currentEnemies) {
                if (enemy instanceof utils.CollisionManager.Positionable && !enemy.isDead()) {
                    entities.add((utils.CollisionManager.Positionable) enemy);
                }
            }
        }
        
        // Update collision manager
        collisionManager.updateEntities(entities);
    }
    
    /**
     * Creates a new game state.
     * 
     * @param playerClass The class of player to create
     */
    public GameState(String playerClass) {
        // Initialize collections and basic state
        this.maps = new ArrayList<>();
        this.currentEnemies = new ArrayList<>();
        this.maxLevel = getMaxCaveLevelsFromConfigImpl();
        this.currentLevel = 0;
        this.gameOver = false;
        
        // Read map dimensions from config
        int[] mapDimensions = getMapDimensionsFromConfig();
        int mapWidth = mapDimensions[0];
        int mapHeight = mapDimensions[1];
        
        this.fogOfWar = new FogOfWar(mapWidth, mapHeight);
        
        // Generate the first level using config values
        double fillPercentage = getMapFillPercentageFromConfig();
        int iterations = getMapIterationsFromConfig();
        
        this.map = new GameMap(mapWidth, mapHeight);
        this.map.generateCaves(fillPercentage, iterations, false); // First level is never final
        maps.add(this.map);
        
        System.out.println("=== MAP GENERATION DEBUG ===");
        System.out.println("Generated map size: " + map.getWidth() + "x" + map.getHeight());
        System.out.println("Expected map size: " + mapWidth + "x" + mapHeight);
        System.out.println("========================");
        
        // Spawn player at the entrance (stairs up) - much more logical!
        this.player = PlayerFactory.createPlayer(playerClass, this.map.getEntranceX(), this.map.getEntranceY());
        
        System.out.println("=== PLAYER SPAWNED AT ENTRANCE ===");
        System.out.println("Player spawned at entrance: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("Exit located at: (" + this.map.getExitX() + ", " + this.map.getExitY() + ")");
        System.out.println("Player is on: " + (this.map.isBlocked(player.getX(), player.getY()) ? "WALL" : "FLOOR"));
        System.out.println("===========================");
        
        // Initialize fog of war with direct reference
        int viewRadius = getVisionRadiusFromConfig();
        fogOfWar.updateVisibility(this.map, player.getX(), player.getY(), viewRadius);
        
        // Setup collision checking for enemy movement
        setupEnemyCollisionChecking();
        
        System.out.println("[INIT] About to spawn enemies for initial level...");
        // Spawn enemies for the initial level
        initializeEnemiesForLevel();
        System.out.println("[INIT] Enemy spawning completed.");
    }
    
    /**
     * Creates a new game state with the provided player and map dimensions.
     * 
     * @param player The player instance
     * @param mapWidth The width of the map
     * @param mapHeight The height of the map
     * @param fillPercentage The initial fill percentage for cave generation
     */
    public GameState(AbstractPlayer player, int mapWidth, int mapHeight, double fillPercentage) {
        // Initialize collections and basic state
        this.maps = new ArrayList<>();
        this.currentEnemies = new ArrayList<>();
        this.maxLevel = getMaxCaveLevelsFromConfigImpl();
        this.currentLevel = 0;
        this.gameOver = false;
        
        this.player = player;
        this.fogOfWar = new FogOfWar(mapWidth, mapHeight);
        
        // Generate the first level with custom dimensions
        this.map = new GameMap(mapWidth, mapHeight);
        this.map.generateCaves(fillPercentage, getMapIterationsFromConfig(), false); // First level is never final
        maps.add(this.map);
        
        // Move player to the entrance position
        player.setPosition(this.map.getEntranceX(), this.map.getEntranceY());
        
        System.out.println("=== PLAYER MOVED TO ENTRANCE ===");
        System.out.println("Player moved to entrance: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("Exit located at: (" + this.map.getExitX() + ", " + this.map.getExitY() + ")");
        System.out.println("===========================");
        
        // Initialize fog of war with direct reference
        int viewRadius = getVisionRadiusFromConfig();
        fogOfWar.updateVisibility(this.map, player.getX(), player.getY(), viewRadius);
        
        // Setup collision checking for enemy movement
        setupEnemyCollisionChecking();
        
        System.out.println("[INIT] About to spawn enemies for initial level...");
        // Spawn enemies for the initial level
        initializeEnemiesForLevel();
        System.out.println("[INIT] Enemy spawning completed.");
    }
    
    /**
     * Generates a new level and adds it to the maps list.
     * 
     * @param mapWidth The width of the map
     * @param mapHeight The height of the map
     * @param fillPercentage The initial fill percentage for cave generation
     * @param iterations The number of cellular automata iterations
     */
    public void generateLevel(int mapWidth, int mapHeight, double fillPercentage, int iterations) {
        GameMap map = new GameMap(mapWidth, mapHeight);
        
        // Check if this will be the final level
        int maxLevels = getMaxCaveLevelsFromConfigImpl();
        boolean isFinalLevel = (maps.size() + 1) >= maxLevels; // +1 because we're about to add this level
        
        map.generateCaves(fillPercentage, iterations, isFinalLevel);
        maps.add(map);
    }
    
    /**
     * Updates the fog of war based on the player's position.
     */
    public void updateFogOfWar() {
        if (fogOfWar != null && player != null) {
            int viewRadius = getVisionRadiusFromConfig(); // Use config value instead of hardcoded
            fogOfWar.updateVisibility(getCurrentMap(), player.getX(), player.getY(), viewRadius);
        }
    }
    
    /**
     * Attempts to move the player in the specified direction.
     * 
     * @param dx The x direction (-1, 0, or 1)
     * @param dy The y direction (-1, 0, or 1)
     * @return true if the move was successful, false otherwise
     */
    public boolean movePlayer(int dx, int dy) {
        if (player == null || gameOver) {
            return false;
        }
        
        int newX = player.getX() + dx;
        int newY = player.getY() + dy;
        
        GameMap currentMap = getCurrentMap();
        
        // Check if the move is valid
        if (currentMap.isInBounds(newX, newY) && !currentMap.isBlocked(newX, newY)) {
            // Check for stairs
            Tile destinationTile = currentMap.getTile(newX, newY);
            
            switch (destinationTile.getType()) {
                case Tile.STAIRS_DOWN -> {
                    // Go to next level
                    goToNextLevel();
                    return true;
                }
                case Tile.STAIRS_UP -> {
                    // Go to previous level
                    goToPreviousLevel();
                    return true;
                }
                default -> {
                    // Regular move
                    player.setPosition(newX, newY);
                    updateFogOfWar();
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Goes to the next dungeon level.
     */
    public void goToNextLevel() {
        int nextLevel = currentLevel + 1;
        
        // Check if we need to generate a new level
        if (nextLevel >= maps.size()) {
            // Use config values for new level generation
            int[] mapDimensions = getMapDimensionsFromConfig();
            double fillPercentage = getMapFillPercentageFromConfig();
            int iterations = getMapIterationsFromConfig();
            generateLevel(mapDimensions[0], mapDimensions[1], fillPercentage, iterations);
        }
        
        // Change level
        currentLevel = nextLevel;
        
        // Move player to the entrance of the new level
        GameMap newMap = getCurrentMap();
        player.setPosition(newMap.getEntranceX(), newMap.getEntranceY());
        
        // Create new fog of war for this level
        fogOfWar = new FogOfWar(newMap.getWidth(), newMap.getHeight());
        updateFogOfWar();
        
        // Spawn enemies for the new level (enemies respawn on each entry)
        spawnEnemiesForCurrentLevel();
        
        System.out.println("=== LEVEL CHANGE: NEXT ===");
        System.out.println("Now on level: " + (currentLevel + 1) + " of " + getMaxCaveLevelsFromConfigImpl());
        System.out.println("New map size: " + newMap.getWidth() + "x" + newMap.getHeight());
        System.out.println("Player moved to entrance: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("========================");
    }
    
    /**
     * Goes to the previous dungeon level.
     */
    public void goToPreviousLevel() {
        if (currentLevel > 0) {
            currentLevel--;
            
            // Move player to the exit of the previous level
            GameMap newMap = getCurrentMap();
            player.setPosition(newMap.getExitX(), newMap.getExitY());
            
            // Create new fog of war for this level
            fogOfWar = new FogOfWar(newMap.getWidth(), newMap.getHeight());
            updateFogOfWar();
            
            // Spawn enemies for this level (enemies respawn on each entry)
            spawnEnemiesForCurrentLevel();
            
            System.out.println("=== LEVEL CHANGE: PREVIOUS ===");
            System.out.println("Now on level: " + (currentLevel + 1) + " of " + getMaxCaveLevelsFromConfigImpl());
            System.out.println("New map size: " + newMap.getWidth() + "x" + newMap.getHeight());
            System.out.println("Player moved to exit: (" + player.getX() + ", " + player.getY() + ")");
            System.out.println("===========================");
        }
    }
    
    /**
     * Gets the current map.
     * 
     * @return The current map
     */
    public GameMap getCurrentMap() {
        return maps.get(currentLevel);
    }
    
    /**
     * Gets the player.
     * 
     * @return The player
     */
    public AbstractPlayer getPlayer() {
        return player;
    }
    
    /**
     * Gets the fog of war.
     * 
     * @return The fog of war
     */
    public FogOfWar getFogOfWar() {
        return fogOfWar;
    }
    
    /**
     * Checks if the player can advance to the next level.
     * 
     * @return true if there are more levels available, false if at max level
     */
    public boolean canGoToNextLevel() {
        int maxLevels = getMaxCaveLevelsFromConfigImpl();
        return currentLevel < (maxLevels - 1); // -1 because levels are 0-indexed
    }
    
    /**
     * Gets a formatted string showing current level and max levels.
     * 
     * @return String like "Cave Floor: 3 of 5"
     */
    public String getLevelDisplayString() {
        int maxLevels = getMaxCaveLevelsFromConfigImpl();
        return "Cave Floor: " + (currentLevel + 1) + " of " + maxLevels;
    }
    
    /**
     * Gets the current dungeon level.
     * 
     * @return The current level
     */
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    /**
     * Checks if the game is over.
     * 
     * @return true if the game is over, false otherwise
     */
    public boolean isGameOver() {
        return gameOver;
    }
    
    /**
     * Sets the game over state.
     * 
     * @param gameOver true to end the game, false otherwise
     */
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
    
    /**
     * Sets the UI manager for combat logging.
     * 
     * @param uiManager The UI manager to use for logging
     */
    public void setUIManager(GameUIManager uiManager) {
        this.uiManager = uiManager;
    }
    
    /**
     * Logs a combat message to the UI with proper formatting.
     * 
     * @param message The combat message to log
     */
    public void logCombatMessage(String message) {
        if (uiManager != null) {
            uiManager.addLogMessage(message);
        } else {
            // Fallback to console if UI not available
            System.out.println(message);
        }
    }
    
    /**
     * Gets the list of enemies on the current level.
     * 
     * @return List of enemies
     */
    public List<Enemy> getCurrentEnemies() {
        return currentEnemies;
    }
    
    /**
     * Initializes enemies for the current level (safe for constructor use).
     */
    private void initializeEnemiesForLevel() {
        System.out.println("[ENEMY SPAWN] Starting enemy spawn for level " + (currentLevel + 1));
        currentEnemies.clear();
        int levelNumber = currentLevel + 1; // Convert to 1-based
        System.out.println("[ENEMY SPAWN] Calling EnemySpawner.spawnEnemiesForLevel with level " + levelNumber);
        currentEnemies.addAll(EnemySpawner.spawnEnemiesForLevel(getCurrentMap(), levelNumber));
        System.out.println("[ENEMY SPAWN] Spawned " + currentEnemies.size() + " enemies for level " + levelNumber);
    }
    
    /**
     * Spawns enemies for the current level.
     */
    public void spawnEnemiesForCurrentLevel() {
        System.out.println("[ENEMY SPAWN] Starting enemy spawn for level " + (currentLevel + 1));
        currentEnemies.clear();
        int levelNumber = currentLevel + 1; // Convert to 1-based
        System.out.println("[ENEMY SPAWN] Calling EnemySpawner.spawnEnemiesForLevel with level " + levelNumber);
        List<Enemy> newEnemies = EnemySpawner.spawnEnemiesForLevel(getCurrentMap(), levelNumber);
        currentEnemies.addAll(newEnemies);
        System.out.println("[ENEMY SPAWN] Spawned " + currentEnemies.size() + " enemies for level " + levelNumber);
    }
    
    /**
     * Updates all enemies on the current level.
     */
    public void updateEnemies() {
        if (player == null) return;
        
        for (Enemy enemy : currentEnemies) {
            if (!enemy.isDead()) {
                enemy.update(player.getX(), player.getY());
                
                // Check if enemy has pending damage to deal to player
                if (enemy instanceof enemies.AbstractEnemy abstractEnemy) {
                    if (abstractEnemy.hasPendingPlayerDamage()) {
                        int damage = abstractEnemy.getPendingPlayerDamage();
                        logCombatMessage(player.getName() + " takes " + damage + " damage!");
                        
                        // Apply damage to player using the proper method
                        boolean playerDied = player.takeDamage(damage);
                        
                        // Check if player died
                        if (playerDied) {
                            logCombatMessage(player.getName() + " has been defeated!");
                            // TODO: Handle player death (game over, respawn, etc.)
                        }
                    }
                }
            }
        }
        
        // Remove dead enemies and update collision system
        boolean enemiesRemoved = currentEnemies.removeIf(Enemy::isDead);
        if (enemiesRemoved) {
            updateCollisionEntities(); // Update collision system when enemies are removed
        }
    }
    
    /**
     * Checks if all enemies on the current level are defeated.
     * 
     * @return true if no living enemies remain
     */
    public boolean areAllEnemiesDefeated() {
        return currentEnemies.stream().allMatch(Enemy::isDead);
    }
    
    /**
     * Finds a safe spawn position for the player, preferring the entrance but
     * falling back to any floor tile if the entrance is blocked.
     * 
     * @param map The game map to search
     * @return An array containing [x, y] coordinates of a safe spawn position
     */
    /**
     * Reads map dimensions from config file.
     * 
     * @return Array containing [width, height] from config
     */
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
    
    /**
     * Reads map fill percentage from config file.
     * 
     * @return Fill percentage from config
     */
    private double getMapFillPercentageFromConfig() {
        String fillStr = Config.getSetting("mapFillPercentage");
        return Double.parseDouble(fillStr.trim());
    }
    
    /**
     * Reads map iterations from config file.
     * 
     * @return Iterations from config
     */
    private int getMapIterationsFromConfig() {
        String iterStr = Config.getSetting("mapIterations");
        return Integer.parseInt(iterStr.trim());
    }
    
    /**
     * Reads vision radius from config file.
     * 
     * @return Vision radius from config
     */
    private int getVisionRadiusFromConfig() {
        String radiusStr = Config.getSetting("visionRadius");
        return Integer.parseInt(radiusStr.trim());
    }
    
    /**
     * Reads the maximum number of cave levels from config file.
     * 
     * @return Maximum number of cave levels from config
     */
    private int getMaxCaveLevelsFromConfigImpl() {
        String levelsStr = Config.getSetting("caveLevelNumber");
        return Integer.parseInt(levelsStr.trim());
    }
}