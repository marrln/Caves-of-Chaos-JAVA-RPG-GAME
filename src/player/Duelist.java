package player;

import config.AnimationConfig;
import config.PlayerConfig;
import utils.Dice;

/**
 * Duelist player class. Specializes in physical attacks and agility.
 */
public class Duelist extends AbstractPlayer {

    public Duelist(int x, int y) {
        super(x, y);
        this.name = "Duelist";
    }
    
    @Override
    protected void updateStatsForLevel() {
        PlayerConfig.PlayerLevelStats stats = PlayerConfig.getDuelistStats(level);
        
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
        return PlayerConfig.getDuelistAttackConfig(attackType);
    }
    
    @Override
    public void attack(int attackType) {
        if (!canAttack(attackType)) {
            return;
        }
        
        PlayerConfig.AttackConfig attackConfig = getAttackConfig(attackType);
        
        // Consume mana (0 for Duelist attacks)
        mp -= attackConfig.mpCost;
        
        // Start attack animation (use proper animation duration, not cooldown)
        combatState.startAttack(attackType, AnimationConfig.getPlayerAnimationDuration("attack"));
        lastAttackTime = System.currentTimeMillis();
    }
    
    @Override
    public int getAttackDamage() {
        return calculateDamage(1); // Use basic attack (type 1)
    }
    
    public int calculateDamage(int attackType) {
        PlayerConfig.AttackConfig attackConfig = getAttackConfig(attackType);
        PlayerConfig.PlayerLevelStats stats = PlayerConfig.getDuelistStats(level);
        
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