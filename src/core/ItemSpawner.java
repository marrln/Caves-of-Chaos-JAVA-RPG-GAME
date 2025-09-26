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
    
    /**
     * Creates a new ItemSpawner with a random seed.
     */
    public ItemSpawner() {
        this.random = new Random();
        this.spawnedWeaponTypes = new HashSet<>();
    }
    
    /**
     * Creates a new ItemSpawner with a specific seed for reproducible spawning.
     * 
     * @param seed The seed for the random number generator
     */
    public ItemSpawner(long seed) {
        this.random = new Random(seed);
        this.spawnedWeaponTypes = new HashSet<>();
    }
    
    /**
     * Spawns items randomly across the map based on the level.
     * Higher levels have better items and more spawn chances.
     * 
     * @param map The map to spawn items on
     * @param level The current level (affects item quality and spawn rate)
     */
    public void spawnItemsForLevel(GameMap map, int level) {
        spawnItemsForLevel(map, level, null);
    }
    
    /**
     * Spawns items randomly across the map based on the level and player type.
     * Higher levels have better items and more spawn chances.
     * Player type affects what consumables are spawned.
     * 
     * @param map The map to spawn items on
     * @param level The current level (affects item quality and spawn rate)
     * @param player The player (used to determine appropriate consumables, can be null)
     */
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
    
    /**
     * Places a specific item at the given coordinates.
     * 
     * @param map The map to place the item on
     * @param item The item to place
     * @param x The x coordinate
     * @param y The y coordinate
     * @return true if the item was placed successfully, false if the tile is invalid or occupied
     */
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
    

    
    /**
     * Creates a random item appropriate for the given level and player.
     * 
     * @param level The current level (affects item quality)
     * @param player The player (affects consumable types, can be null)
     * @return A randomly generated item
     */
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
    

    
    /**
     * Creates a random consumable item appropriate for the player.
     * 8 different potion types based on level and player capabilities.
     * 
     * @param level The current level (affects item power)
     * @param player The player (affects what consumables are appropriate, can be null)
     * @return A random consumable item
     */
    private Potion createRandomConsumable(int level, player.AbstractPlayer player) {
        boolean playerUsesMana = (player != null && player.getMaxMp() > 0);
        
        // Choose from 8 different potion types
        int roll = random.nextInt(8);
        
        // Scale potions with level
        int baseHeal = 15 + (level * 3);
        int baseMana = 10 + (level * 2);
        
        switch (roll) {
            case 0:
                // Minor Health Potion
                return new Potion("Minor Health Potion", "Restores " + baseHeal + " HP", baseHeal, 0);
                
            case 1:
                // Health Potion
                int healAmount = baseHeal + 10;
                return new Potion("Health Potion", "Restores " + healAmount + " HP", healAmount, 0);
                
            case 2:
                // Greater Health Potion
                int greaterHeal = baseHeal + 20;
                return new Potion("Greater Health Potion", "Restores " + greaterHeal + " HP", greaterHeal, 0);
                
            case 3:
                if (playerUsesMana) {
                    // Minor Mana Potion
                    return new Potion("Minor Mana Potion", "Restores " + baseMana + " MP", 0, baseMana);
                } else {
                    // Superior Health Potion for non-mana users
                    int superiorHeal = baseHeal + 30;
                    return new Potion("Superior Health Potion", "Restores " + superiorHeal + " HP", superiorHeal, 0);
                }
                
            case 4:
                if (playerUsesMana) {
                    // Mana Potion
                    int manaAmount = baseMana + 8;
                    return new Potion("Mana Potion", "Restores " + manaAmount + " MP", 0, manaAmount);
                } else {
                    // Ultimate Health Potion for non-mana users
                    int ultimateHeal = baseHeal + 40;
                    return new Potion("Ultimate Health Potion", "Restores " + ultimateHeal + " HP", ultimateHeal, 0);
                }
                
            case 5:
                if (playerUsesMana) {
                    // Greater Mana Potion
                    int greaterMana = baseMana + 15;
                    return new Potion("Greater Mana Potion", "Restores " + greaterMana + " MP", 0, greaterMana);
                } else {
                    // Elixir of Vitality for non-mana users
                    int vitalityHeal = baseHeal + 25;
                    return new Potion("Elixir of Vitality", "Restores " + vitalityHeal + " HP", vitalityHeal, 0);
                }
                
            case 6:
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
                
            default:
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
    

    
    /**
     * Creates a random weapon item appropriate for the player class.
     * Avoids duplicating weapon types on the same level.
     * 
     * @param level The current level (affects weapon power)
     * @param player The player (affects what weapons are appropriate, can be null)
     * @return A random weapon item
     */
    private Weapon createRandomWeapon(int level, player.AbstractPlayer player) {
        // Base damage bonus increases with level
        int damageBonus = 3 + (level * 2);
        
        // Determine player class for appropriate weapon generation
        String playerClass = (player != null) ? player.getClass().getSimpleName().toLowerCase() : "any";
        
        // Define available weapons based on player class
        List<String> availableWeapons = new ArrayList<>();
        
        if ("duelist".equals(playerClass)) {
            if (!spawnedWeaponTypes.contains("Iron Sword")) availableWeapons.add("Iron Sword");
            if (!spawnedWeaponTypes.contains("War Hammer")) availableWeapons.add("War Hammer");
            if (!spawnedWeaponTypes.contains("Elven Blade")) availableWeapons.add("Elven Blade");
        } else if ("wizard".equals(playerClass)) {
            if (!spawnedWeaponTypes.contains("Magic Staff")) availableWeapons.add("Magic Staff");
            if (!spawnedWeaponTypes.contains("Elven Blade")) availableWeapons.add("Elven Blade");
        } else {
            // Default: any weapon for unknown player types
            if (!spawnedWeaponTypes.contains("Iron Sword")) availableWeapons.add("Iron Sword");
            if (!spawnedWeaponTypes.contains("War Hammer")) availableWeapons.add("War Hammer");
            if (!spawnedWeaponTypes.contains("Elven Blade")) availableWeapons.add("Elven Blade");
            if (!spawnedWeaponTypes.contains("Magic Staff")) availableWeapons.add("Magic Staff");
        }
        
        // If no new weapons available, allow duplicates
        if (availableWeapons.isEmpty()) {
            if ("duelist".equals(playerClass)) {
                availableWeapons.add("Iron Sword");
                availableWeapons.add("War Hammer");
                availableWeapons.add("Elven Blade");
            } else if ("wizard".equals(playerClass)) {
                availableWeapons.add("Magic Staff");
                availableWeapons.add("Elven Blade");
            } else {
                availableWeapons.add("Iron Sword");
                availableWeapons.add("War Hammer");
                availableWeapons.add("Elven Blade");
                availableWeapons.add("Magic Staff");
            }
        }
        
        // Pick a random available weapon
        String selectedWeapon = availableWeapons.get(random.nextInt(availableWeapons.size()));
        spawnedWeaponTypes.add(selectedWeapon);
        
        // Create and return the weapon
        switch (selectedWeapon) {
            case "Iron Sword":
                return new Weapon("Iron Sword", "A sturdy iron blade", 
                    damageBonus, "duelist", "sword");
            case "War Hammer":
                return new Weapon("War Hammer", "A heavy crushing weapon", 
                    damageBonus + 2, "duelist", "hammer");
            case "Elven Blade":
                return new Weapon("Elven Blade", "A swift and precise weapon", 
                    damageBonus + 1, "any", "blade");
            default: // Magic Staff
                return new Weapon("Magic Staff", "A staff crackling with energy", 
                    damageBonus + 1, "wizard", "staff");
        }
    }
    
    /**
     * Creates a random trap appropriate for the given level.
     * Higher levels have a chance for more dangerous traps.
     * 
     * @param level The current level (affects trap severity distribution)
     * @return A random trap item
     */
    private Item createRandomTrap(int level) {
        // Calculate trap severity chances based on level
        // Early levels: mostly light traps
        // Higher levels: more dangerous traps become possible
        
        double lightChance = Math.max(0.1, 0.7 - (level * 0.05)); // 70% at level 1, decreases to 10% minimum
        double moderateChance = 0.2 + (level * 0.03); // 20% at level 1, increases with level
        double severeChance = 1.0 - lightChance - moderateChance; // Remainder goes to severe
        
        double roll = random.nextDouble();
        
        if (roll < lightChance) {
            // Light trap: Spike Trap (3 damage)
            return new SpikeTrap();
        } else if (roll < lightChance + moderateChance) {
            // Moderate trap: Poison Dart Trap (7 damage)
            return new PoisonDartTrap();
        } else {
            // Severe trap: Explosive Rune Trap (12 damage)
            return new ExplosiveRuneTrap();
        }
    }
    
    /**
     * Finds all valid tiles where items can be spawned.
     * 
     * @param map The map to search
     * @return A list of tiles suitable for item placement
     */
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
    
    /**
     * Spawns the Shard of Judgement at the specified location.
     * This is the victory item that appears when the Medusa of Chaos is defeated.
     * 
     * @param map The game map to spawn on
     * @param x The x coordinate to spawn at
     * @param y The y coordinate to spawn at
     * @return true if the shard was successfully placed, false otherwise
     */
    public boolean spawnShardOfJudgement(GameMap map, int x, int y) {
        System.out.println("DEBUG: Attempting to spawn Shard of Judgement at (" + x + ", " + y + ")");
        Tile tile = map.getTile(x, y);
        
        if (tile == null) {
            System.out.println("DEBUG: Tile at (" + x + ", " + y + ") is null!");
            return false;
        }
        
        System.out.println("DEBUG: Tile type: " + tile.getType() + ", blocked: " + tile.isBlocked() + ", hasItem: " + tile.hasItem());
        
        // Check if the tile is valid for spawning
        if (tile.getType() != Tile.FLOOR || tile.isBlocked() || tile.hasItem()) {
            System.out.println("DEBUG: Original tile not valid, searching nearby...");
            // Try to find a nearby valid tile
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx == 0 && dy == 0) continue;
                    
                    Tile nearbyTile = map.getTile(x + dx, y + dy);
                    System.out.println("DEBUG: Checking tile at (" + (x + dx) + ", " + (y + dy) + ")");
                    if (nearbyTile != null) {
                        System.out.println("DEBUG: Nearby tile type: " + nearbyTile.getType() + 
                                         ", blocked: " + nearbyTile.isBlocked() + 
                                         ", hasItem: " + nearbyTile.hasItem());
                        if (nearbyTile.getType() == Tile.FLOOR && 
                            !nearbyTile.isBlocked() && 
                            !nearbyTile.hasItem()) {
                            
                            nearbyTile.setItem(new items.ShardOfJudgement());
                            System.out.println("DEBUG: Shard spawned at nearby tile (" + (x + dx) + ", " + (y + dy) + ")");
                            System.out.println("DEBUG: Tile now hasItem: " + nearbyTile.hasItem());
                            return true;
                        }
                    } else {
                        System.out.println("DEBUG: Nearby tile is null");
                    }
                }
            }
            
            System.out.println("Could not find valid tile for Shard of Judgement!");
            return false; // Could not find a valid location
        }
        
        // Spawn the Shard of Judgement at the specified location
        tile.setItem(new items.ShardOfJudgement());
        System.out.println("DEBUG: Shard spawned at original tile (" + x + ", " + y + ")");
        System.out.println("DEBUG: Tile now hasItem: " + tile.hasItem());
        return true;
    }
}