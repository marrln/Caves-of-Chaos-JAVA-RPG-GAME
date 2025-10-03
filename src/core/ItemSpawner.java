package core;

import config.ItemConfig;
import items.ExplosiveRuneTrap;
import items.Item;
import items.PoisonDartTrap;
import items.Potion;
import items.SpikeTrap;
import items.Weapon;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import map.GameMap;
import map.Tile;

/**
 * Handles spawning items on the game map.
 * Uses ItemConfig for all balance values and item definitions.
 */
public class ItemSpawner {
    
    private final Random random;
    private final Set<ItemConfig.WeaponType> spawnedWeaponTypes;
    
    public ItemSpawner() {
        this.random = new Random();
        this.spawnedWeaponTypes = new HashSet<>();
    }

    public void spawnItemsForLevel(GameMap map, int level, player.AbstractPlayer player) {
        spawnedWeaponTypes.clear();
        
        int itemsToSpawn = ItemConfig.getItemCountForLevel(level, random.nextDouble());
        List<Tile> validTiles = findValidSpawnTiles(map);
        
        for (int i = 0; i < itemsToSpawn && !validTiles.isEmpty(); i++) {
            int tileIndex = random.nextInt(validTiles.size());
            Tile tile = validTiles.get(tileIndex);
            validTiles.remove(tileIndex);
            
            Item item = createRandomItem(level, player);
            tile.setItem(item);
        }
    }
    
    public boolean placeItem(GameMap map, Item item, int x, int y) {
        if (x < 0 || y < 0 || x >= map.getWidth() || y >= map.getHeight()) {
            return false;
        }
        
        Tile tile = map.getTile(x, y);
        if (tile.isBlocked() || tile.hasItem()) return false;
        
        tile.setItem(item);
        return true;
    }
    
    private Item createRandomItem(int level, player.AbstractPlayer player) {
        String itemType = ItemConfig.getItemTypeFromRoll(random.nextDouble(), level);
        
        return switch (itemType) {
            case "consumable" -> createRandomConsumable(level, player);
            case "weapon" -> createRandomWeapon(level, player);
            default -> createRandomTrap(level);
        };
    }
    
    private Potion createRandomConsumable(int level, player.AbstractPlayer player) {
        boolean playerUsesMana = (player != null && player.getMaxMp() > 0);
        
        int roll = random.nextInt(ItemConfig.getPotionTypeCount());
        ItemConfig.PotionType potionType = ItemConfig.getRandomPotionType(playerUsesMana, roll);
        
        int hpAmount = ItemConfig.calculatePotionHp(potionType, level);
        int mpAmount = ItemConfig.calculatePotionMp(potionType, level);
        String description = ItemConfig.createPotionDescription(hpAmount, mpAmount);
        
        return new Potion(potionType.name, description, hpAmount, mpAmount);
    }
    
    private Weapon createRandomWeapon(int level, player.AbstractPlayer player) {
        String playerClass = player.getClass().getSimpleName().toLowerCase();
        
        ItemConfig.WeaponType[] classWeapons = ItemConfig.getAvailableWeapons(playerClass);
        List<ItemConfig.WeaponType> availableWeapons = new ArrayList<>();
        
        // Prefer weapons that haven't been spawned yet
        for (ItemConfig.WeaponType weaponType : classWeapons) {
            if (!spawnedWeaponTypes.contains(weaponType)) {
                availableWeapons.add(weaponType);
            }
        }
        
        // If all weapons spawned, allow duplicates
        if (availableWeapons.isEmpty()) {
            for (ItemConfig.WeaponType weaponType : classWeapons) {
                availableWeapons.add(weaponType);
            }
        }
        
        ItemConfig.WeaponType selectedWeapon = availableWeapons.get(random.nextInt(availableWeapons.size()));
        spawnedWeaponTypes.add(selectedWeapon);
        
        int damageBonus = ItemConfig.calculateWeaponDamage(selectedWeapon, level);
        
        return new Weapon(
            selectedWeapon.name,
            selectedWeapon.description,
            damageBonus,
            selectedWeapon.playerClass,
            selectedWeapon.weaponType
        );
    }
    
    private Item createRandomTrap(int level) {
        ItemConfig.TrapType trapType = ItemConfig.getTrapTypeFromRoll(level, random.nextDouble());
        
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
                
                // Valid tiles are floor tiles without items and not blocked
                if (tile.getType() == Tile.FLOOR && !tile.isBlocked() && !tile.hasItem()) {
                    validTiles.add(tile);
                }
            }
        }
        
        return validTiles;
    }
    
    public boolean spawnShardOfJudgement(GameMap map, int x, int y) {
        Tile tile = map.getTile(x, y);
        
        if (tile.getType() != Tile.FLOOR || tile.isBlocked() || tile.hasItem()) {
            // Try to find a nearby valid tile
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    
                    Tile nearbyTile = map.getTile(x + dx, y + dy);
                    if (nearbyTile != null) {
                        if (nearbyTile.getType() == Tile.FLOOR && !nearbyTile.isBlocked() && !nearbyTile.hasItem()) {
                            nearbyTile.setItem(new items.ShardOfJudgement());
                            return true;
                        }
                    }
                }
            }
            System.out.println("Could not find valid tile for Shard of Judgement!");
            return false;
        }
        tile.setItem(new items.ShardOfJudgement());
        return true;
    }
}