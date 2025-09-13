package ui;

import config.StyleConfig;
import core.GameController;
import core.GameState;
import graphics.EnemyRenderer;
import graphics.TileRenderer;
import input.GameInputHandler;
import java.awt.*;
import javax.swing.JPanel;
import javax.swing.Timer;
import map.FogOfWar;
import map.GameMap;
import player.AbstractPlayer;

public class GamePanel extends JPanel {
    private final GameState gameState;
    private final GameController controller;
    private TileRenderer tileRenderer;
    private final Camera camera;

    private int visibleMapWidth = 20;
    private int visibleMapHeight = 15;

    private GameUIManager uiManager;
    private GameInputHandler inputHandler;

    private boolean debugNoFog = false;
    private int lastKnownLevel = -1;

    private Timer gameTimer;
    private static final int GAME_UPDATE_DELAY = 100; 

    public GamePanel(GameState gameState, GameController controller) {
        this.gameState = gameState;
        this.controller = controller;
        setBackground(StyleConfig.getColor("gameBackground", Color.BLACK));
        setFocusable(true);

        GameMap map = gameState.getCurrentMap();
        AbstractPlayer player = gameState.getPlayer();

        camera = new Camera(visibleMapWidth, visibleMapHeight);
        camera.setMapSize(map.getWidth(), map.getHeight());
        camera.centerOn(player.getX(), player.getY());
        camera.snapToTarget(player.getX(), player.getY());

        setupInputHandler();
        setupGameLoop();
    }

    public void centerCameraOnPlayer() {
        AbstractPlayer player = gameState.getPlayer();
        GameMap map = gameState.getCurrentMap();
        if (player == null || map == null) return;

        float originalSmoothing = camera.getSmoothing();
        camera.setSmoothing(0.0f);
        camera.setMapSize(map.getWidth(), map.getHeight());
        camera.centerOn(player.getX(), player.getY());
        camera.snapToTarget(player.getX(), player.getY());
        camera.setSmoothing(originalSmoothing);
        repaint();
    }

    public void toggleFogOfWar() {
        debugNoFog = !debugNoFog;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        AbstractPlayer player = gameState.getPlayer();
        GameMap map = gameState.getCurrentMap();

        ensureTileRendererInitialized();

        int currentLevel = gameState.getCurrentLevel();
        if (currentLevel != lastKnownLevel) {
            reinitializeCameraForNewLevel(player);
            uiManager.refreshStatusPanel();
            lastKnownLevel = currentLevel;
        }

        FogOfWar fogOfWar = gameState.getFogOfWar();
        tileRenderer.renderVisibleArea(g, map, camera, fogOfWar, debugNoFog, getWidth(), getHeight());

        Graphics2D g2d = (Graphics2D) g;
        int tileSize = getTileSize();
        EnemyRenderer.renderEnemies(g2d, gameState.getCurrentEnemies(), tileSize, camera.getX() * tileSize, camera.getY() * tileSize, fogOfWar);

        int playerX = (player.getX() - camera.getX()) * tileSize;
        int playerY = (player.getY() - camera.getY()) * tileSize;
        g.setColor(StyleConfig.getColor("playerBody", Color.CYAN));
        g.fillOval(playerX, playerY, tileSize, tileSize);
        g.setColor(StyleConfig.getColor("playerBorder", Color.WHITE));
        g.drawOval(playerX, playerY, tileSize, tileSize);
    }

    private void reinitializeCameraForNewLevel(AbstractPlayer player) {
        camera.centerOn(player.getX(), player.getY());
        camera.snapToTarget(player.getX(), player.getY());
        repaint();
    }

    private void setupInputHandler() {
        inputHandler = new GameInputHandler(gameState, controller, this);
        addKeyListener(inputHandler);
    }

    public void setUIManager(GameUIManager uiManager) {
        this.uiManager = uiManager;
        inputHandler.setUIManager(uiManager);
    }

    public void setVisibleArea(int width, int height) {
        this.visibleMapWidth = width;
        this.visibleMapHeight = height;
        camera.setViewSize(width, height);
        AbstractPlayer player = gameState.getPlayer();
        camera.snapToTarget(player.getX(), player.getY());
    }

    private void ensureTileRendererInitialized() {
        if (tileRenderer != null) return;

        GameMap currentMap = gameState.getCurrentMap();
        if (currentMap == null) return;

        if (currentMap.getEntranceX() >= 0 && currentMap.getEntranceY() >= 0 &&
            currentMap.getExitX() >= 0 && currentMap.getExitY() >= 0) {
            tileRenderer = new TileRenderer();
        }
    }

    public int getTileSize() { return config.Config.getIntSetting("tile_size"); }
    public Camera getCamera() { return camera; }
    public int getVisibleMapWidth() { return visibleMapWidth; }
    public int getVisibleMapHeight() { return visibleMapHeight; }

    private void setupGameLoop() {
        gameTimer = new Timer(GAME_UPDATE_DELAY, _ -> updateGameLogic());
        gameTimer.start();
    }

    private void updateGameLogic() {
        if (gameState == null) return;

        gameState.updateEnemies();

        AbstractPlayer player = gameState.getPlayer();
        if (player != null) camera.centerOn(player.getX(), player.getY());

        uiManager.refreshStatusPanel();
        repaint();
    }

    public void stopGameLoop() { if (gameTimer != null) gameTimer.stop(); }
}
