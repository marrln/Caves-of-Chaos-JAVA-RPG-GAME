package core;

import java.util.ArrayList;
import java.util.List;
import player.AbstractPlayer;
import player.PlayerFactory;

/**
 * Holds the complete state of the game.
 */
public class GameState {
    
    // Constants
    private static final int DEFAULT_MAP_WIDTH = 80;
    private static final int DEFAULT_MAP_HEIGHT = 60;
    private static final double DEFAULT_FILL_PERCENTAGE = 0.45;
    private static final int DEFAULT_ITERATIONS = 5;
    
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
        // Generate the first level
        generateLevel(DEFAULT_MAP_WIDTH, DEFAULT_MAP_HEIGHT, DEFAULT_FILL_PERCENTAGE, DEFAULT_ITERATIONS);
        
        // Create the player
        GameMap firstMap = maps.get(0);
        player = PlayerFactory.createPlayer(playerClass, firstMap.getEntranceX(), firstMap.getEntranceY());
        
        // Initialize fog of war
        fogOfWar = new FogOfWar(DEFAULT_MAP_WIDTH, DEFAULT_MAP_HEIGHT);
        updateFogOfWar();
        
        // Set initial game state
        currentLevel = 0;
        gameOver = false;
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
        
        // Generate the first level with custom dimensions
        generateLevel(mapWidth, mapHeight, fillPercentage, DEFAULT_ITERATIONS);
        
        // Move player to the entrance
        GameMap firstMap = maps.get(0);
        player.setPosition(firstMap.getEntranceX(), firstMap.getEntranceY());
        
        // Initialize fog of war
        fogOfWar = new FogOfWar(mapWidth, mapHeight);
        updateFogOfWar();
        
        // Set initial game state
        currentLevel = 0;
        gameOver = false;
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
        map.generateCaves(fillPercentage, iterations);
        maps.add(map);
    }
    
    /**
     * Updates the fog of war based on the player's position.
     */
    public void updateFogOfWar() {
        if (fogOfWar != null && player != null) {
            int viewRadius = 8; // Player's view radius
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
            generateLevel(DEFAULT_MAP_WIDTH, DEFAULT_MAP_HEIGHT, DEFAULT_FILL_PERCENTAGE, DEFAULT_ITERATIONS);
        }
        
        // Change level
        currentLevel = nextLevel;
        
        // Move player to the entrance of the new level
        GameMap newMap = getCurrentMap();
        player.setPosition(newMap.getEntranceX(), newMap.getEntranceY());
        
        // Create new fog of war for this level
        fogOfWar = new FogOfWar(newMap.getWidth(), newMap.getHeight());
        updateFogOfWar();
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
}
