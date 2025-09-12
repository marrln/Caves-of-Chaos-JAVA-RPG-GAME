package enemies;

/**
 * Configuration class containing all enemy statistics and combat parameters.
 * This class centralizes enemy balance configuration to make tweaking easier.
 * 
 * All damage values use dice-based calculations with the Dice utility class.
 * Attack chances should sum to 100 for each enemy type.
 */
public class EnemyConfig {
    
    /**
     * Configuration data for a single enemy type.
     */
    public static class EnemyStats {
        public final int baseHp;
        public final int baseDamage;
        public final int expReward;
        public final int movementSpeed;           // Tiles per turn
        public final int attackCooldown;          // Milliseconds between attacks
        public final int noticeRadius;            // Tiles within which enemy notices player
        public final int diceSides;               // Number of sides on damage dice
        public final int variationPercent;        // Damage variation percentage
        public final int[] attackChances;         // Probability for each attack type
        public final int[] attackDamageMultipliers; // Damage multiplier for each attack
        
        public EnemyStats(int baseHp, int baseDamage, int expReward, int movementSpeed, 
                         int attackCooldown, int noticeRadius, int diceSides, 
                         int variationPercent, int[] attackChances, int[] attackDamageMultipliers) {
            this.baseHp = baseHp;
            this.baseDamage = baseDamage;
            this.expReward = expReward;
            this.movementSpeed = movementSpeed;
            this.attackCooldown = attackCooldown;
            this.noticeRadius = noticeRadius;
            this.diceSides = diceSides;
            this.variationPercent = variationPercent;
            this.attackChances = attackChances;
            this.attackDamageMultipliers = attackDamageMultipliers;
        }
    }
    
    /**
     * Gets the configuration for the specified enemy type.
     * 
     * @param type The enemy type to get configuration for
     * @return The enemy stats configuration
     */
    public static EnemyStats getStats(EnemyType type) {
        return switch (type) {
            case SLIME -> new EnemyStats(
                15,          // baseHp
                3,           // baseDamage  
                5,           // expReward
                1,           // movementSpeed
                1200,        // attackCooldown (1.2 seconds)
                4,           // noticeRadius (4 tiles)
                6,           // diceSides (d6)
                30,          // variationPercent (±30%)
                new int[]{70, 30},           // attackChances: 70% basic, 30% acid
                new int[]{100, 150}          // attackDamageMultipliers: 100%, 150%
            );
            
            case ORC -> new EnemyStats(
                25,          // baseHp
                5,           // baseDamage
                10,          // expReward  
                1,           // movementSpeed
                1000,        // attackCooldown (1.0 seconds)
                5,           // noticeRadius
                6,           // diceSides
                25,          // variationPercent
                new int[]{60, 40},           // attackChances: 60% slash, 40% heavy
                new int[]{100, 140}          // attackDamageMultipliers
            );
            
            case SKELETON -> new EnemyStats(
                20,          // baseHp
                4,           // baseDamage
                8,           // expReward
                1,           // movementSpeed
                1100,        // attackCooldown  
                6,           // noticeRadius
                6,           // diceSides
                20,          // variationPercent
                new int[]{75, 25},           // attackChances: 75% stab, 25% bone throw
                new int[]{100, 120}          // attackDamageMultipliers
            );
            
            case WEREWOLF -> new EnemyStats(
                30,          // baseHp
                6,           // baseDamage
                15,          // expReward
                2,           // movementSpeed (faster!)
                800,         // attackCooldown (0.8 seconds)
                7,           // noticeRadius  
                8,           // diceSides
                35,          // variationPercent
                new int[]{50, 50},           // attackChances: 50% claw, 50% bite
                new int[]{100, 130}          // attackDamageMultipliers
            );
            
            case ARMORED_ORC -> new EnemyStats(
                40,          // baseHp
                7,           // baseDamage
                20,          // expReward
                1,           // movementSpeed
                1200,        // attackCooldown
                5,           // noticeRadius
                8,           // diceSides
                30,          // variationPercent
                new int[]{50, 30, 20},       // attackChances: 50% slash, 30% heavy, 20% shield bash
                new int[]{100, 140, 110}     // attackDamageMultipliers
            );
            
            case ARMORED_SKELETON -> new EnemyStats(
                35,          // baseHp
                6,           // baseDamage
                18,          // expReward
                1,           // movementSpeed
                1100,        // attackCooldown
                6,           // noticeRadius
                8,           // diceSides
                25,          // variationPercent
                new int[]{60, 25, 15},       // attackChances
                new int[]{100, 120, 160}     // attackDamageMultipliers (last is critical)
            );
            
            case ELITE_ORC -> new EnemyStats(
                45,          // baseHp
                8,           // baseDamage
                25,          // expReward
                1,           // movementSpeed
                900,         // attackCooldown (faster attacks)
                6,           // noticeRadius
                10,          // diceSides
                40,          // variationPercent
                new int[]{40, 35, 25},       // attackChances: balanced attacks
                new int[]{100, 130, 170}     // attackDamageMultipliers
            );
            
            case GREATSWORD_SKELETON -> new EnemyStats(
                50,          // baseHp
                9,           // baseDamage
                30,          // expReward
                1,           // movementSpeed
                1400,        // attackCooldown (slow but powerful)
                5,           // noticeRadius
                12,          // diceSides
                45,          // variationPercent
                new int[]{30, 40, 30},       // attackChances
                new int[]{100, 150, 200}     // attackDamageMultipliers (devastating final attack)
            );
            
            case ORC_RIDER -> new EnemyStats(
                38,          // baseHp
                7,           // baseDamage
                22,          // expReward
                2,           // movementSpeed (mounted = faster)
                1000,        // attackCooldown
                8,           // noticeRadius (mounted = better vision)
                8,           // diceSides
                35,          // variationPercent
                new int[]{45, 35, 20},       // attackChances: lance, trample, charge
                new int[]{100, 120, 160}     // attackDamageMultipliers
            );
            
            case WEREBEAR -> new EnemyStats(
                60,          // baseHp (tanky)
                10,          // baseDamage
                35,          // expReward
                1,           // movementSpeed
                1500,        // attackCooldown (slow but devastating)
                6,           // noticeRadius
                12,          // diceSides
                50,          // variationPercent (very random)
                new int[]{40, 35, 25},       // attackChances: claw, maul, roar
                new int[]{100, 140, 180}     // attackDamageMultipliers
            );
            
            case MEDUSA_OF_CHAOS -> new EnemyStats(
                200,         // baseHp (BOSS!)
                15,          // baseDamage
                100,         // expReward
                1,           // movementSpeed
                2000,        // attackCooldown (2 seconds)
                10,          // noticeRadius (sees everything)
                20,          // diceSides
                60,          // variationPercent (very unpredictable)
                new int[]{30, 35, 35},       // attackChances: snake bite, stone gaze, chaos magic
                new int[]{100, 160, 220}     // attackDamageMultipliers (escalating danger)
            );
        };
    }
    
    /**
     * Gets the base movement cooldown in milliseconds.
     * This can be modified by enemy movement speed.
     * 
     * @return Base movement delay
     */
    public static int getBaseMovementCooldown() {
        return 800; // 0.8 seconds base movement
    }
    
    /**
     * Gets the maximum number of random movement attempts.
     * Used when enemies perform Brownian (random) movement.
     * 
     * @return Maximum attempts for random movement
     */
    public static int getMaxRandomMoveAttempts() {
        return 4; // Try up to 4 random directions
    }
    
    /**
     * Gets the attack names for display purposes.
     * 
     * @param type The enemy type
     * @return Array of attack names corresponding to attack indices
     */
    public static String[] getAttackNames(EnemyType type) {
        return switch (type) {
            case SLIME -> new String[]{"Slime Strike", "Acid Splash"};
            case ORC -> new String[]{"Sword Slash", "Heavy Swing"};
            case SKELETON -> new String[]{"Bone Stab", "Bone Throw"};
            case WEREWOLF -> new String[]{"Claw Swipe", "Savage Bite"};
            case ARMORED_ORC -> new String[]{"Armored Slash", "Heavy Blow", "Shield Bash"};
            case ARMORED_SKELETON -> new String[]{"Skeletal Strike", "Bone Crack", "Critical Stab"};
            case ELITE_ORC -> new String[]{"Elite Slash", "Power Strike", "Berserker Rage"};
            case GREATSWORD_SKELETON -> new String[]{"Greatsword Swing", "Devastating Slash", "Execution Strike"};
            case ORC_RIDER -> new String[]{"Lance Strike", "Trample", "Charging Attack"};
            case WEREBEAR -> new String[]{"Bear Claw", "Crushing Maul", "Intimidating Roar"};
            case MEDUSA_OF_CHAOS -> new String[]{"Venomous Bite", "Petrifying Gaze", "Chaos Magic"};
        };
    }
}
