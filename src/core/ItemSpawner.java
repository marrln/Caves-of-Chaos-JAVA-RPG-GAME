package core;

import config.ItemConfig;
import config.ItemSpawnConfig;
import items.ExplosiveRuneTrap;
import items.Item;
import items.PoisonDartTrap;
import items.Potion;
import items.SpikeTrap;
import items.Weapon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import map.GameMap;
import map.Tile;

/**
 * Handles spawning items on the game map.
 * Uses ItemConfig for item definitions and ItemSpawnConfig for spawn logic.
 */
public class ItemSpawner {

    private final Random random = new Random();
    private final Set<ItemConfig.WeaponType> spawnedWeaponTypes = new HashSet<>();

    public void spawnItemsForLevel(GameMap map, int level, player.AbstractPlayer player) {
        spawnedWeaponTypes.clear();

        int itemsToSpawn = ItemSpawnConfig.getItemCountForLevel(level, random.nextDouble());
        List<Tile> validTiles = findValidSpawnTiles(map);

        for (int i = 0; i < itemsToSpawn && !validTiles.isEmpty(); i++) {
            int tileIndex = random.nextInt(validTiles.size());
            Tile tile = validTiles.remove(tileIndex);

            try {
                Item item = createRandomItem(level, player);
                if (item != null) tile.setItem(item);
            } catch (Exception e) {
                System.err.println("[ITEM SPAWNER] ERROR creating item for level " + level);
                e.printStackTrace();
            }
        }
    }

    public boolean placeItem(GameMap map, Item item, int x, int y) {
        if (x < 0 || y < 0 || x >= map.getWidth() || y >= map.getHeight()) return false;

        Tile tile = map.getTile(x, y);
        if (tile.isBlocked() || tile.hasItem()) return false;

        tile.setItem(item);
        return true;
    }

    private Item createRandomItem(int level, player.AbstractPlayer player) {
        String type = ItemSpawnConfig.getItemTypeFromRoll(random.nextDouble(), level);

        return switch (type) {
            case "consumable" -> createRandomConsumable(level, player);
            case "weapon" -> createRandomWeapon(level, player);
            default -> createRandomTrap();
        };
    }

    private Potion createRandomConsumable(int level, player.AbstractPlayer player) {
        boolean playerUsesMana = player != null && player.getMaxMp() > 0;
        ItemConfig.PotionType[] pool = playerUsesMana 
                ? ItemSpawnConfig.MANA_USER_POTIONS 
                : ItemSpawnConfig.NON_MANA_USER_POTIONS;

        ItemConfig.PotionType potionType = pool[random.nextInt(pool.length)];
        int hpAmount = ItemSpawnConfig.calculatePotionHp(potionType, level);
        int mpAmount = ItemSpawnConfig.calculatePotionMp(potionType, level);
        String description = ItemConfig.createPotionDescription(hpAmount, mpAmount);

        return new Potion(potionType.name, description, hpAmount, mpAmount);
    }

    private Weapon createRandomWeapon(int level, player.AbstractPlayer player) {
        String playerClass = player.getClass().getSimpleName().toLowerCase();
        ItemConfig.WeaponType[] classWeapons = ItemConfig.getAvailableWeapons(playerClass);

        List<ItemConfig.WeaponType> available = Arrays.stream(classWeapons)
                .filter(w -> !spawnedWeaponTypes.contains(w))
                .toList();

        if (available.isEmpty()) available = Arrays.asList(classWeapons);

        ItemConfig.WeaponType selected = available.get(random.nextInt(available.size()));
        spawnedWeaponTypes.add(selected);

        int damage = ItemSpawnConfig.calculateWeaponDamage(selected, level);
        return new Weapon(selected.name, damage, selected.playerClass, selected.effect);
    }

    private Item createRandomTrap() {
        ItemConfig.TrapType trapType = ItemSpawnConfig.getTrapTypeFromRoll(random.nextDouble());

        return switch (trapType) {
            case SPIKE -> new SpikeTrap();
            case POISON_DART -> new PoisonDartTrap();
            case EXPLOSIVE_RUNE -> new ExplosiveRuneTrap();
        };
    }

    private List<Tile> findValidSpawnTiles(GameMap map) {
        List<Tile> validTiles = new ArrayList<>();
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                Tile tile = map.getTile(x, y);
                if (tile.getType() == Tile.FLOOR && !tile.isBlocked() && !tile.hasItem())
                    validTiles.add(tile);
            }
        }
        return validTiles;
    }

    public boolean spawnShardOfJudgement(GameMap map, int x, int y) {
        // Try main tile
        if (placeShardIfValid(map, x, y)) return true;

        // Try nearby 3x3 tiles
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                if (placeShardIfValid(map, x + dx, y + dy)) return true;
            }
        }

        // Fallback to any valid tile
        List<Tile> validTiles = findValidSpawnTiles(map);
        if (!validTiles.isEmpty()) {
            validTiles.get(random.nextInt(validTiles.size())).setItem(new items.ShardOfJudgement());
            return true;
        }

        return false;
    }

    private boolean placeShardIfValid(GameMap map, int x, int y) {
        if (x < 0 || y < 0 || x >= map.getWidth() || y >= map.getHeight()) return false;

        Tile tile = map.getTile(x, y);
        if (tile.getType() == Tile.FLOOR && !tile.isBlocked() && !tile.hasItem()) {
            tile.setItem(new items.ShardOfJudgement());
            return true;
        }
        return false;
    }
}
