package core;

import config.ItemConfig;
import config.ItemSpawnConfig;
import items.*;
import java.util.*;
import map.GameMap;
import map.Tile;

/**
 * Handles spawning items on the game map.
 */
public class ItemSpawner {

    private final Random random = new Random();
    private final Set<ItemConfig.WeaponType> spawnedWeaponTypes = new HashSet<>();

    public void spawnItemsForLevel(GameMap map, int level, player.AbstractPlayer player) {
        spawnedWeaponTypes.clear();
        int itemsToSpawn = ItemSpawnConfig.getItemCountForLevel(level, random.nextDouble());
        List<Tile> validTiles = findValidSpawnTiles(map);

        for (int i = 0; i < itemsToSpawn && !validTiles.isEmpty(); i++) {
            Tile tile = validTiles.remove(random.nextInt(validTiles.size()));
            Item item = createRandomItem(level, player);
            if (item != null) tile.setItem(item);
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
        return switch (ItemSpawnConfig.getItemTypeFromRoll(random.nextDouble(), level)) {
            case "consumable" -> createRandomConsumable(level, player);
            case "weapon" -> createRandomWeapon(level, player);
            default -> createRandomTrap();
        };
    }

    private Potion createRandomConsumable(int level, player.AbstractPlayer player) {
        boolean usesMana = player != null && player.getMaxMp() > 0;
        ItemConfig.PotionType[] pool = usesMana ? ItemSpawnConfig.MANA_USER_POTIONS : ItemSpawnConfig.NON_MANA_USER_POTIONS;
        ItemConfig.PotionType type = pool[random.nextInt(pool.length)];

        int hp = ItemSpawnConfig.calculatePotionHp(type, level);
        int mp = ItemSpawnConfig.calculatePotionMp(type, level);
        return new Potion(type.name, ItemConfig.createPotionDescription(hp, mp), hp, mp);
    }

    private Weapon createRandomWeapon(int level, player.AbstractPlayer player) {
        String cls = player.getClass().getSimpleName().toLowerCase();
        List<ItemConfig.WeaponType> available = Arrays.stream(ItemConfig.getAvailableWeapons(cls))
            .filter(w -> !spawnedWeaponTypes.contains(w))
            .toList();
        if (available.isEmpty()) available = Arrays.asList(ItemConfig.getAvailableWeapons(cls));

        ItemConfig.WeaponType selected = available.get(random.nextInt(available.size()));
        spawnedWeaponTypes.add(selected);
        int dmg = ItemSpawnConfig.calculateWeaponDamage(selected, level);
        return new Weapon(selected.name, dmg, selected.playerClass, selected.effect);
    }

    private Item createRandomTrap() {
        return switch (ItemSpawnConfig.getTrapTypeFromRoll(random.nextDouble())) {
            case SPIKE -> new SpikeTrap();
            case POISON_DART -> new PoisonDartTrap();
            case EXPLOSIVE_RUNE -> new ExplosiveRuneTrap();
        };
    }

    private List<Tile> findValidSpawnTiles(GameMap map) {
        List<Tile> tiles = new ArrayList<>();
        for (int x = 0; x < map.getWidth(); x++)
            for (int y = 0; y < map.getHeight(); y++) {
                Tile t = map.getTile(x, y);
                if (t.getType() == Tile.FLOOR && !t.isBlocked() && !t.hasItem()) tiles.add(t);
            }
        return tiles;
    }

    public boolean spawnShardOfJudgement(GameMap map, int x, int y) {
        // Try 3x3 grid first
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                if (tryPlaceShard(map, x + dx, y + dy)) return true;

        // Fallback anywhere
        List<Tile> valid = findValidSpawnTiles(map);
        if (!valid.isEmpty()) {
            valid.get(random.nextInt(valid.size())).setItem(new ShardOfJudgement());
            return true;
        }
        return false;
    }

    private boolean tryPlaceShard(GameMap map, int x, int y) {
        if (x < 0 || y < 0 || x >= map.getWidth() || y >= map.getHeight()) return false;
        Tile t = map.getTile(x, y);
        if (t.getType() == Tile.FLOOR && !t.isBlocked() && !t.hasItem()) {
            t.setItem(new ShardOfJudgement());
            return true;
        }
        return false;
    }
}
