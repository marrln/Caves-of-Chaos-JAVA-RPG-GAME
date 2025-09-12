package player;

import config.AnimationConfig;
import core.Projectile;
import enemies.Enemy;
import java.util.List;
import utils.Dice;

/**
 * Wizard player class. Focuses on magical abilities and projectile spells.
 */
public class Wizard extends AbstractPlayer {
    
    /**
     * Creates a new Wizard at the specified position.
     * 
     * @param x The initial x position
     * @param y The initial y position
     */
    public Wizard(int x, int y) {
        super(x, y);
        this.name = "Wizard";
    }
    
    @Override
    protected void updateStatsForLevel() {
        PlayerConfig.PlayerLevelStats stats = PlayerConfig.getWizardStats(level);
        
        // Update stats while preserving current HP/MP percentages
        double hpPercent = maxHp > 0 ? (double) hp / maxHp : 1.0;
        double mpPercent = maxMp > 0 ? (double) mp / maxMp : 1.0;
        
        this.maxHp = stats.baseHp;
        this.maxMp = stats.baseMp;
        this.baseDamage = stats.baseDamage;
        this.expToNext = stats.expToNextLevel;
        
        // Restore HP/MP based on previous percentages (level up healing)
        this.hp = (int) (maxHp * hpPercent);
        this.mp = (int) (maxMp * mpPercent);
    }
    
    @Override
    protected PlayerConfig.AttackConfig getAttackConfig(int attackType) {
        return PlayerConfig.getWizardAttackConfig(attackType);
    }
    
    @Override
    public void attack(int attackType) {
        if (!canAttack(attackType)) {
            return;
        }
        
        PlayerConfig.AttackConfig attackConfig = getAttackConfig(attackType);
        
        // Consume mana
        mp -= attackConfig.mpCost;
        
        // Start attack animation (use proper animation duration, not cooldown)
        combatState.startAttack(attackType, AnimationConfig.getPlayerAnimationDuration("attack"));
        lastAttackTime = System.currentTimeMillis();
    }
    
    /**
     * Creates a projectile for wizard spells.
     * This should be called when the attack animation reaches the projectile launch point.
     * 
     * @param attackType The attack type (1 = Fire, 2 = Ice)
     * @param enemies List of enemies to target
     * @return The created projectile, or null if no valid target
     */
    public Projectile createProjectile(int attackType, List<Enemy> enemies) {
        // Find closest enemy
        Enemy closestEnemy = findClosestEnemy(enemies);
        if (closestEnemy == null) {
            return null; // No targets available
        }
        
        // Create appropriate projectile type
        Projectile.ProjectileType projectileType = attackType == 1 ? 
            Projectile.ProjectileType.FIRE_SPELL : Projectile.ProjectileType.ICE_SPELL;
        
        return new Projectile(projectileType, x, y, closestEnemy);
    }
    
    /**
     * Finds the closest living enemy to the wizard.
     * 
     * @param enemies List of all enemies
     * @return The closest enemy, or null if none found
     */
    private Enemy findClosestEnemy(List<Enemy> enemies) {
        Enemy closestEnemy = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (Enemy enemy : enemies) {
            if (!enemy.isDead()) {
                double distance = Math.sqrt(
                    (enemy.getX() - x) * (enemy.getX() - x) + 
                    (enemy.getY() - y) * (enemy.getY() - y)
                );
                
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestEnemy = enemy;
                }
            }
        }
        
        return closestEnemy;
    }
    
    /**
     * Gets the attack damage using the default attack (type 1).
     * 
     * @return The calculated attack damage
     */
    @Override
    public int getAttackDamage() {
        return calculateDamage(1); // Use basic spell (type 1)
    }
    
    /**
     * Calculates damage for a Wizard spell.
     * 
     * @param attackType The attack type
     * @return The calculated damage
     */
    public int calculateDamage(int attackType) {
        PlayerConfig.AttackConfig attackConfig = getAttackConfig(attackType);
        PlayerConfig.PlayerLevelStats stats = PlayerConfig.getWizardStats(level);
        
        // Check for critical hit
        int totalCritChance = stats.criticalChance + attackConfig.criticalBonus;
        boolean isCritical = Dice.checkChance(totalCritChance);
        
        return PlayerConfig.calculatePlayerDamage(baseDamage, attackConfig, isCritical, stats.criticalMultiplier);
    }
    
    @Override
    public void useItem(int slot) {
        // TODO: Implement inventory and item usage
    }
}
