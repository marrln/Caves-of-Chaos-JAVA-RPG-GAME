package enemies;

/**
 * Enumeration of all enemy types in the game.
 * Each type corresponds to the sprite naming in assets.xml.
 */
public enum EnemyType {
    // Basic enemies (2 attacks)
    SLIME("slime"),
    ORC("orc"),
    SKELETON("skeleton"),
    WEREWOLF("werewolf"),
    
    // Advanced enemies (3 attacks)  
    ARMORED_ORC("armored_orc"),
    ARMORED_SKELETON("armored_skeleton"),
    ELITE_ORC("elite_orc"),
    GREATSWORD_SKELETON("greatsword_skeleton"),
    ORC_RIDER("orc_rider"),
    WEREBEAR("werebear"),
    
    // Boss
    MEDUSA_OF_CHAOS("medusa_of_chaos");
    
    private final String spritePrefix;
    
    EnemyType(String spritePrefix) {
        this.spritePrefix = spritePrefix;
    }
    
    public String getSpritePrefix() {
        return spritePrefix;
    }
    
    public String getDisplayName() {
        return name().toLowerCase().replace('_', ' ');
    }
    
    public boolean isBoss() {
        return this == MEDUSA_OF_CHAOS;
    }
    
    public int getAttackCount() {
        return switch (this) {
            case SLIME, ORC, SKELETON, WEREWOLF -> 2;
            case ARMORED_ORC, ARMORED_SKELETON, ELITE_ORC, 
                 GREATSWORD_SKELETON, ORC_RIDER, WEREBEAR, MEDUSA_OF_CHAOS -> 3;
        };
    }
}
