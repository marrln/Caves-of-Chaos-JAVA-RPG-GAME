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
    
    // Boss (3 attacks)
    MEDUSA_OF_CHAOS("medusa_of_chaos");
    
    private final String spritePrefix;
    
    EnemyType(String spritePrefix) {
        this.spritePrefix = spritePrefix;
    }
    
    /**
     * Gets the sprite prefix used in assets.xml for this enemy type.
     * 
     * @return The sprite prefix string
     */
    public String getSpritePrefix() {
        return spritePrefix;
    }
    
    /**
     * Gets the display name for this enemy type.
     * 
     * @return A formatted display name
     */
    public String getDisplayName() {
        return name().toLowerCase().replace('_', ' ');
    }
    
    /**
     * Checks if this enemy type is a boss.
     * 
     * @return true if this is a boss enemy, false otherwise
     */
    public boolean isBoss() {
        return this == MEDUSA_OF_CHAOS;
    }
    
    /**
     * Gets the number of attack types this enemy has based on assets.
     * 
     * @return The number of different attack animations (1-3)
     */
    public int getAttackCount() {
        return switch (this) {
            case SLIME, ORC, SKELETON, WEREWOLF -> 2;
            case ARMORED_ORC, ARMORED_SKELETON, ELITE_ORC, 
                 GREATSWORD_SKELETON, ORC_RIDER, WEREBEAR, MEDUSA_OF_CHAOS -> 3;
        };
    }
}
