package enemies;

public class EnemyFactory {

    public static Enemy createEnemy(EnemyType type, int x, int y) {
        return switch (type) {
            case SLIME -> new Slime(x, y);
            case ORC -> new Orc(x, y);
            case SKELETON -> new Skeleton(x, y);
            case WEREWOLF -> new Werewolf(x, y);
            case ARMORED_ORC -> new ArmoredOrc(x, y);
            case ARMORED_SKELETON -> new ArmoredSkeleton(x, y);
            case ELITE_ORC -> new EliteOrc(x, y);
            case GREATSWORD_SKELETON -> new GreatswordSkeleton(x, y);
            case ORC_RIDER -> new OrcRider(x, y);
            case WEREBEAR -> new Werebear(x, y);
            case MEDUSA_OF_CHAOS -> new MedusaOfChaos(x, y);
            default -> throw new IllegalArgumentException("Unknown enemy type: " + type);
        };
    }
}
