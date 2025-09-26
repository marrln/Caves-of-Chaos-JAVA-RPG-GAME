package core;

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
 * Provides methods for placing items at specific locations or randomly across the map.
 */
public class ItemSpawner {
    
    private final Random random;
    private final Set<String> spawnedWeaponTypes;
    
    public ItemSpawner() {
        this.random = new Random();
        this.spawnedWeaponTypes = new HashSet<>();
    }

    public void spawnItemsForLevel(GameMap map, int level, player.AbstractPlayer player) {
        // Clear spawned weapon types for new level
        spawnedWeaponTypes.clear();
        
        // Calculate number of items to spawn based on level
        int baseItems = 3 + (level / 2); // 3-4 items on level 1, 5-6 on level 3, etc.
        int itemsToSpawn = baseItems + random.nextInt(3); // Add 0-2 random items
        
        List<Tile> validTiles = findValidSpawnTiles(map);
        
        for (int i = 0; i < itemsToSpawn && !validTiles.isEmpty(); i++) {
            // Pick a random valid tile
            int tileIndex = random.nextInt(validTiles.size());
            Tile tile = validTiles.get(tileIndex);
            validTiles.remove(tileIndex); // Don't spawn multiple items on same tile
            
            // Create and place an item
            Item item = createRandomItem(level, player);
            tile.setItem(item);
        }
    }
    
    public boolean placeItem(GameMap map, Item item, int x, int y) {
        if (x < 0 || y < 0 || x >= map.getWidth() || y >= map.getHeight()) {
            return false;
        }
        
        Tile tile = map.getTile(x, y);
        
        // Can't place items on blocked tiles or tiles that already have items
        if (tile.isBlocked() || tile.hasItem()) {
            return false;
        }
        
        tile.setItem(item);
        return true;
    }
    
    private Item createRandomItem(int level, player.AbstractPlayer player) {
        double roll = random.nextDouble();
        
        // 60% chance for consumables, 25% chance for weapons, 15% chance for traps
        if (roll < 0.6) {
            return createRandomConsumable(level, player);
        } else if (roll < 0.85) {
            return createRandomWeapon(level, player);
        } else {
            return createRandomTrap(level);
        }
    }
    
    private Potion createRandomConsumable(int level, player.AbstractPlayer player) {
        boolean playerUsesMana = (player != null && player.getMaxMp() > 0);
        
        // Choose from 8 different potion types
        int roll = random.nextInt(8);
        
        // Scale potions with level
        int baseHeal = 15 + (level * 3);
        int baseMana = 10 + (level * 2);
        
        switch (roll) {
            case 0 -> {
                // Minor Health Potion
                return new Potion("Minor Health Potion", "Restores " + baseHeal + " HP", baseHeal, 0);
            }
                
            case 1 -> {
                // Health Potion
                int healAmount = baseHeal + 10;
                return new Potion("Health Potion", "Restores " + healAmount + " HP", healAmount, 0);
            }
                
            case 2 -> {
                // Greater Health Potion
                int greaterHeal = baseHeal + 20;
                return new Potion("Greater Health Potion", "Restores " + greaterHeal + " HP", greaterHeal, 0);
            }
                
            case 3 -> {
                if (playerUsesMana) {
                    // Minor Mana Potion
                    return new Potion("Minor Mana Potion", "Restores " + baseMana + " MP", 0, baseMana);
                } else {
                    // Superior Health Potion for non-mana users
                    int superiorHeal = baseHeal + 30;
                    return new Potion("Superior Health Potion", "Restores " + superiorHeal + " HP", superiorHeal, 0);
                }
            }
                
            case 4 -> {
                if (playerUsesMana) {
                    // Mana Potion
                    int manaAmount = baseMana + 8;
                    return new Potion("Mana Potion", "Restores " + manaAmount + " MP", 0, manaAmount);
                } else {
                    // Ultimate Health Potion for non-mana users
                    int ultimateHeal = baseHeal + 40;
                    return new Potion("Ultimate Health Potion", "Restores " + ultimateHeal + " HP", ultimateHeal, 0);
                }
            }
                
            case 5 -> {
                if (playerUsesMana) {
                    // Greater Mana Potion
                    int greaterMana = baseMana + 15;
                    return new Potion("Greater Mana Potion", "Restores " + greaterMana + " MP", 0, greaterMana);
                } else {
                    // Elixir of Vitality for non-mana users
                    int vitalityHeal = baseHeal + 25;
                    return new Potion("Elixir of Vitality", "Restores " + vitalityHeal + " HP", vitalityHeal, 0);
                }
            }
                
            case 6 -> {
                if (playerUsesMana) {
                    // Minor Elixir (small heal + mana)
                    int minorHeal = baseHeal / 2;
                    int minorMana = baseMana / 2;
                    return new Potion("Minor Elixir",
                            "Restores " + minorHeal + " HP and " + minorMana + " MP",
                            minorHeal, minorMana);
                } else {
                    // Potion of Regeneration for non-mana users
                    int regenHeal = baseHeal + 15;
                    return new Potion("Potion of Regeneration", "Restores " + regenHeal + " HP", regenHeal, 0);
                }
            }
                
            default -> {
                if (playerUsesMana) {
                    // Superior Elixir (good heal + mana)
                    int superiorHeal = (int)(baseHeal * 0.8);
                    int superiorMana = (int)(baseMana * 0.8);
                    return new Potion("Superior Elixir",
                            "Restores " + superiorHeal + " HP and " + superiorMana + " MP",
                            superiorHeal, superiorMana);
                } else {
                    // Potion of Divine Healing for non-mana users
                    int divineHeal = baseHeal + 35;
                    return new Potion("Potion of Divine Healing", "Restores " + divineHeal + " HP", divineHeal, 0);
                }
            }
        }
    }
    
    private Weapon createRandomWeapon(int level, player.AbstractPlayer player) {
        
        int damageBonus = 3 + (level * 2);  // Base damage bonus increases with level
        String playerClass = player.getClass().getSimpleName().toLowerCase();
        List<String> availableWeapons = new ArrayList<>();
        
        switch (playerClass) {
            case "duelist" -> {
                if (!spawnedWeaponTypes.contains("Iron Sword")) availableWeapons.add("Iron Sword");
                if (!spawnedWeaponTypes.contains("War Hammer")) availableWeapons.add("War Hammer");
                if (!spawnedWeaponTypes.contains("Elven Blade")) availableWeapons.add("Elven Blade");
            }
            case "wizard" -> {
                if (!spawnedWeaponTypes.contains("Magic Staff")) availableWeapons.add("Magic Staff");
                if (!spawnedWeaponTypes.contains("Elven Blade")) availableWeapons.add("Elven Blade");
            }
            default -> {
                // Default: any weapon for unknown player types
                if (!spawnedWeaponTypes.contains("Iron Sword")) availableWeapons.add("Iron Sword");
                if (!spawnedWeaponTypes.contains("War Hammer")) availableWeapons.add("War Hammer");
                if (!spawnedWeaponTypes.contains("Elven Blade")) availableWeapons.add("Elven Blade");
                if (!spawnedWeaponTypes.contains("Magic Staff")) availableWeapons.add("Magic Staff");
            }
        }
        
        // If no new weapons available, allow duplicates
        if (availableWeapons.isEmpty()) {
            switch (playerClass) {
                case "duelist" -> {
                    availableWeapons.add("Iron Sword");
                    availableWeapons.add("War Hammer");
                    availableWeapons.add("Elven Blade");
                }
                case "wizard" -> {
                    availableWeapons.add("Magic Staff");
                    availableWeapons.add("Elven Blade");
                }
                default -> {
                    availableWeapons.add("Iron Sword");
                    availableWeapons.add("War Hammer");
                    availableWeapons.add("Elven Blade");
                    availableWeapons.add("Magic Staff");
                }
            }
        }
        
        // Pick a random available weapon
        String selectedWeapon = availableWeapons.get(random.nextInt(availableWeapons.size()));
        spawnedWeaponTypes.add(selectedWeapon);
        
        // Create and return the weapon
        return switch (selectedWeapon) {
            case "Iron Sword" -> new Weapon("Iron Sword", "A sturdy iron blade", 
                    damageBonus, "duelist", "sword");
            case "War Hammer" -> new Weapon("War Hammer", "A heavy crushing weapon", 
                    damageBonus + 2, "duelist", "hammer");
            case "Elven Blade" -> new Weapon("Elven Blade", "A swift and precise weapon", 
                    damageBonus + 1, "any", "blade");
            default -> new Weapon("Magic Staff", "A staff crackling with energy", 
                    damageBonus + 1, "wizard", "staff");
        }; // Magic Staff
    }
    
    private Item createRandomTrap(int level) {

        final double MIN_LIGHT_CHANCE = 0.1;
        final double BASE_LIGHT_CHANCE = 0.7;
        final double LIGHT_CHANCE_DECREASE_PER_LEVEL = 0.05;
        final double BASE_MODERATE_CHANCE = 0.2;
        final double MODERATE_CHANCE_INCREASE_PER_LEVEL = 0.03;

        // Calculate chances
        double lightChance = Math.max(MIN_LIGHT_CHANCE, BASE_LIGHT_CHANCE - (level * LIGHT_CHANCE_DECREASE_PER_LEVEL));
        double moderateChance = BASE_MODERATE_CHANCE + (level * MODERATE_CHANCE_INCREASE_PER_LEVEL);

        double roll = random.nextDouble();
        if (roll < lightChance) return new SpikeTrap(); 
        if (roll < lightChance + moderateChance) return new PoisonDartTrap();
        else return new ExplosiveRuneTrap(); //  (roll >= lightChance + moderateChance)
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
        
        // Check if the tile is valid for spawning
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
            return false; // Could not find a valid location
        }
        tile.setItem(new items.ShardOfJudgement());
        return true;
    }
}