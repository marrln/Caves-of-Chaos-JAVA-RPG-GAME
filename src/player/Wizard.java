package player;

import config.AnimationConfig;
import config.PlayerConfig;
import core.Projectile;
import enemies.Enemy;
import java.util.List;
import utils.Dice;

/**
 * Wizard player class. Focuses on magical abilities and projectile spells.
 */
public class Wizard extends AbstractPlayer {
    
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
        lastAttackTimes[attackType] = System.currentTimeMillis();
    }
    
    public Projectile createProjectile(int attackType, List<Enemy> enemies, map.FogOfWar fogOfWar) {
        // Find closest visible enemy
        Enemy closestEnemy = findClosestVisibleEnemy(enemies, fogOfWar);
        if (closestEnemy == null) {
            return null; // No visible targets available
        }
        
        // Create appropriate projectile type
        Projectile.ProjectileType projectileType = attackType == 1 ? 
            Projectile.ProjectileType.FIRE_SPELL : Projectile.ProjectileType.ICE_SPELL;
        
        return new Projectile(projectileType, x, y, closestEnemy);
    }

    private Enemy findClosestVisibleEnemy(List<Enemy> enemies, map.FogOfWar fogOfWar) {
        Enemy closestEnemy = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && isEnemyVisible(enemy, fogOfWar)) {
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

    // NOTE: The Wizard can see all enemies if fog of war is disabled
    // NOTE: Can only perform spell attacks on visible enemies, else the spell fizzles
    private boolean isEnemyVisible(Enemy enemy, map.FogOfWar fogOfWar) {
        // If fog of war is null, assume everything is visible
        if (fogOfWar == null) {
            return true;
        }
        
        // Check if the enemy's position is visible
        return fogOfWar.isVisible(enemy.getX(), enemy.getY());
    }
    
    @Override
    public int getAttackDamage(int attackType) { return calculateDamage(attackType); }
    
    public int calculateDamage(int attackType) {
        PlayerConfig.AttackConfig attackConfig = getAttackConfig(attackType);
        PlayerConfig.PlayerLevelStats stats = PlayerConfig.getWizardStats(level);
        
        // Calculate base damage including weapon bonus
        int weaponBonus = (equippedWeapon != null) ? equippedWeapon.getDamageBonus() : 0;
        int totalBaseDamage = baseDamage + weaponBonus;
        
        // Check for critical hit
        int totalCritChance = stats.criticalChance + attackConfig.criticalBonus;
        boolean isCritical = Dice.checkChance(totalCritChance);
        
        return PlayerConfig.calculatePlayerDamage(totalBaseDamage, attackConfig, isCritical, stats.criticalMultiplier);
    }
    
    @Override
    public void useItem(int slot) { inventory.useItem(slot, this); }
}
