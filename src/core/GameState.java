package core;

import config.Config;
import java.util.ArrayList;
import java.util.List;
import map.FogOfWar;
import map.GameMap;
import map.Tile;
import player.AbstractPlayer;
import player.PlayerFactory;

/**
 * Manages the overall game state including player, maps, and level progression.
 */
public class GameState {
    
    // Game state
    private final List<GameMap> maps = new ArrayList<>();
    private AbstractPlayer player;
    private int currentLevel;
    private FogOfWar fogOfWar;
    private boolean gameOver;
    
    /**
     * Creates a new game state.
     * 
     * @param playerClass The class of player to create
     */
    public GameState(String playerClass) {
        // Read map dimensions from config
        int[] mapDimensions = getMapDimensionsFromConfig();
        int mapWidth = mapDimensions[0];
        int mapHeight = mapDimensions[1];
        
        this.fogOfWar = new FogOfWar(mapWidth, mapHeight);
        this.currentLevel = 0;
        this.gameOver = false;
        
        // Generate the first level using config values
        double fillPercentage = getMapFillPercentageFromConfig();
        int iterations = getMapIterationsFromConfig();
        
        GameMap map = new GameMap(mapWidth, mapHeight);
        map.generateCaves(fillPercentage, iterations, false); // First level is never final
        maps.add(map);
        
        System.out.println("=== MAP GENERATION DEBUG ===");
        System.out.println("Generated map size: " + map.getWidth() + "x" + map.getHeight());
        System.out.println("Expected map size: " + mapWidth + "x" + mapHeight);
        System.out.println("========================");
        
        // Spawn player at the entrance (stairs up) - much more logical!
        player = PlayerFactory.createPlayer(playerClass, map.getEntranceX(), map.getEntranceY());
        
        System.out.println("=== PLAYER SPAWNED AT ENTRANCE ===");
        System.out.println("Player spawned at entrance: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("Exit located at: (" + map.getExitX() + ", " + map.getExitY() + ")");
        System.out.println("Player is on: " + (map.isBlocked(player.getX(), player.getY()) ? "WALL" : "FLOOR"));
        System.out.println("===========================");
        
        // Initialize fog of war with direct reference
        int viewRadius = getVisionRadiusFromConfig();
        fogOfWar.updateVisibility(map, player.getX(), player.getY(), viewRadius);
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
        this.player = player;
        this.fogOfWar = new FogOfWar(mapWidth, mapHeight);
        this.currentLevel = 0;
        this.gameOver = false;
        
        // Generate the first level with custom dimensions
        GameMap map = new GameMap(mapWidth, mapHeight);
        map.generateCaves(fillPercentage, getMapIterationsFromConfig(), false); // First level is never final
        maps.add(map);
        
        // Move player to the entrance position
        player.setPosition(map.getEntranceX(), map.getEntranceY());
        
        System.out.println("=== PLAYER MOVED TO ENTRANCE ===");
        System.out.println("Player moved to entrance: (" + player.getX() + ", " + player.getY() + ")");
        System.out.println("Exit located at: (" + map.getExitX() + ", " + map.getExitY() + ")");
        System.out.println("===========================");
        
        // Initialize fog of war with direct reference
        int viewRadius = getVisionRadiusFromConfig();
        fogOfWar.updateVisibility(map, player.getX(), player.getY(), viewRadius);
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
        int maxLevels = getMaxCaveLevelsFromConfig();
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
        
        System.out.println("=== LEVEL CHANGE: NEXT ===");
        System.out.println("Now on level: " + (currentLevel + 1) + " of " + getMaxCaveLevelsFromConfig());
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
            
            System.out.println("=== LEVEL CHANGE: PREVIOUS ===");
            System.out.println("Now on level: " + (currentLevel + 1) + " of " + getMaxCaveLevelsFromConfig());
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
        int maxLevels = getMaxCaveLevelsFromConfig();
        return currentLevel < (maxLevels - 1); // -1 because levels are 0-indexed
    }
    
    /**
     * Gets a formatted string showing current level and max levels.
     * 
     * @return String like "Cave Floor: 3 of 5"
     */
    public String getLevelDisplayString() {
        int maxLevels = getMaxCaveLevelsFromConfig();
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
    private int getMaxCaveLevelsFromConfig() {
        String levelsStr = Config.getSetting("caveLevelNumber");
        return Integer.parseInt(levelsStr.trim());
    }
}