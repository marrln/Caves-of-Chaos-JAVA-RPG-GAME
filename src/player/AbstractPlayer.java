package player;

import config.AnimationUtil;
import config.Config;
import core.CombatState;
import items.Inventory;
import items.Weapon;
import utils.CollisionManager;

/**
 * Base player class containing shared logic for all player types (Duelist, Wizard, etc.).
 * Handles movement, stats progression, combat state, equipment, and basic attacks.
 */
public abstract class AbstractPlayer implements CollisionManager.Positionable {
    
    // ===== XP & LEVELING CONFIGURATION =====
    private static final int[] LEVEL_XP_THRESHOLDS = {0, 700, 1500, 2700, 6500, 14000};
    private static final int MAX_LEVEL = LEVEL_XP_THRESHOLDS.length;

    // ===== STATS DATA STRUCTURES =====
    
    /**
     * Contains all stat information for a player at a specific level.
     */
    public static class PlayerLevelStats {
        public final int maxHp, maxMp, expToNextLevel;
        public final AttackConfig[] attacks;

        public PlayerLevelStats(int maxHp, int maxMp, int expToNextLevel, AttackConfig[] attacks) {
            this.maxHp = maxHp;
            this.maxMp = maxMp;
            this.expToNextLevel = expToNextLevel;
            this.attacks = attacks;
        }
    }

    /**
     * Configuration for a single attack/ability.
     */
    public static class AttackConfig {
        public final String logicalName, displayName;
        public final int diceCount, diceSides, diceBonus, mpCost, cooldown;

        public AttackConfig(String logicalName, String displayName, int diceCount, int diceSides, 
                          int diceBonus, int mpCost, int cooldown) {
            this.logicalName = logicalName;
            this.displayName = displayName;
            this.diceCount = diceCount;
            this.diceSides = diceSides;
            this.diceBonus = diceBonus;
            this.mpCost = mpCost;
            this.cooldown = cooldown;
        }
    }
    
    // ====== CORE STATS ======
    protected int x, y;             // Position
    protected int hp, maxHp;        // Health
    protected int mp, maxMp;        // Mana
    protected int level = 1;        // Level
    protected int exp = 0;          // Experience
    protected int expToNext;        // Exp needed for next level
    protected String name;          // Character name

    // ====== INVENTORY & EQUIPMENT ======
    protected Inventory inventory = new Inventory();
    protected Weapon equippedWeapon;

    // ====== COMBAT ======
    protected CombatState combatState = new CombatState();
    protected long[] lastAttackTimes = new long[3]; // Index 0 unused, 1 and 2 for attack types
    protected long lastRestTime = 0;
    private static final long REST_COOLDOWN = 15000; // 15 seconds in milliseconds

    // ====== EFFECT TRACKING ======
    private long healingEffectEndTime = 0;
    private long manaEffectEndTime = 0;
    private long levelUpEffectEndTime = 0;
    private static final int EFFECT_DURATION = 800; // milliseconds

    // ====== DIRECTION ======
    protected int facingDirection = 1; // 0=N,1=E,2=S,3=W

    // ====== COLLISION ======
    private static CollisionManager collisionManager;
    public static void setCollisionManager(CollisionManager manager) { collisionManager = manager; }

    // ====== CONSTRUCTOR ======
    public AbstractPlayer(int x, int y) {
        this.x = x;
        this.y = y;
        updateStatsForLevel();
    }

    // ====== POSITION ======
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean tryMoveTo(int newX, int newY) {
        // Update facing direction BEFORE collision check for more responsive feel
        int dx = newX - this.x;
        if (dx != 0) setFacingDirection(dx > 0 ? 1 : 3); // 1=E, 3=W
        
        // Check collision and actually move if allowed
        if (collisionManager != null && !collisionManager.canMoveTo(this, newX, newY)) return false;
        setPosition(newX, newY);
        combatState.setState(CombatState.State.MOVING, AnimationUtil.getPlayerAnimationDuration("walk", getPlayerClass()));
        return true;
    }

    public boolean tryMoveDirection(int direction) {
        CollisionManager.Position offset = CollisionManager.getDirectionOffset(direction);
        boolean moved = (offset.x != 0 || offset.y != 0) && tryMoveTo(x + offset.x, y + offset.y);
        if (moved) setFacingDirection(direction);
        return moved;
    }

    @Override public int getFacingDirection() { return facingDirection; }
    @Override public void setFacingDirection(int dir) { this.facingDirection = ((dir % 4) + 4) % 4; }

    // ====== STATS & PROGRESSION ======
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getMp() { return mp; }
    public int getMaxMp() { return maxMp; }
    public int getLevel() { return level; }
    public int getMaxLevel() { return MAX_LEVEL; }
    public int getExp() { return exp; }
    public int getExpToNext() { return expToNext; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    /**
     * Gets the player class name for sprite lookups (e.g., "Duelist", "Wizard").
     * <p>This is different from {@link #getName()} which returns the custom character name.</p>
     * 
     * @return simple class name (e.g., "Duelist" for player.Duelist)
     */
    public String getPlayerClass() {
        return this.getClass().getSimpleName();
    }

    // ====== INVENTORY & EQUIPMENT ======
    public Inventory getInventory() { return inventory; }
    public Weapon getEquippedWeapon() { return equippedWeapon; }

    public boolean equipWeapon(Weapon weapon) {
        if (weapon != null && weapon.canUse(this)) {
            // Remove old weapon effects if any
            if (equippedWeapon != null) {
                equippedWeapon.removeEquipEffect(this);
            }
            
            equippedWeapon = weapon;
            
            // Apply new weapon effects
            weapon.applyEquipEffect(this);
            
            return true;
        }
        return false;
    }

    public Weapon unequipWeapon() {
        Weapon weapon = equippedWeapon;
        
        // Remove weapon effects if any
        if (weapon != null) {
            weapon.removeEquipEffect(this);
        }
        
        equippedWeapon = null;
        return weapon;
    }

    public int restoreHp(int amount) {
        int oldHp = hp;
        hp = Math.min(maxHp, hp + amount);
        return hp - oldHp;
    }

    public int restoreMp(int amount) {
        int oldMp = mp;
        mp = Math.min(maxMp, mp + amount);
        return mp - oldMp;
    }
    
    public void addMaxHp(int amount) {
        maxHp += amount;
        // Ensure current HP doesn't exceed new max
        if (hp > maxHp) {
            hp = maxHp;
        }
    }
    
    public void addMaxMp(int amount) {
        maxMp += amount;
        // Ensure current MP doesn't exceed new max
        if (mp > maxMp) {
            mp = maxMp;
        }
    }

    public void triggerHealingEffect() { healingEffectEndTime = System.currentTimeMillis() + EFFECT_DURATION; }
    public boolean isHealingEffectActive() { return System.currentTimeMillis() < healingEffectEndTime; }
    public void triggerManaEffect() { manaEffectEndTime = System.currentTimeMillis() + EFFECT_DURATION; }
    public boolean isManaEffectActive() { return System.currentTimeMillis() < manaEffectEndTime; }
    public void triggerLevelUpEffect() { levelUpEffectEndTime = System.currentTimeMillis() + EFFECT_DURATION; }
    public boolean isLevelUpEffectActive() { return System.currentTimeMillis() < levelUpEffectEndTime; }

    public int addExperience(int gained) {
        int maxLevel = getMaxLevel();
        
        // Don't add XP if already at max level
        if (level >= maxLevel) {
            return 0;
        }
        
        exp += gained;
        int levelsGained = 0;
        
        // Level up while we have enough XP AND haven't reached max level
        while (exp >= expToNext && level < maxLevel) {
            levelUp();
            levelsGained++;
        }
        
        // If we just reached max level, reset XP for clean display
        if (level >= maxLevel) {
            exp = 0;
        }
        
        return levelsGained;
    }

    private void levelUp() {
        exp -= expToNext;
        level++;
        updateStatsForLevel();
        triggerLevelUpEffect();
    }

    // ====== COMBAT ======
    public CombatState getCombatState() { return combatState; }

    public boolean takeDamage(int dmg) {
        hp = Math.max(0, hp - dmg);
        combatState.setState(CombatState.State.HURT, AnimationUtil.getPlayerAnimationDuration("hurt", getPlayerClass()));
        return hp == 0;
    }

    public boolean canAttack(int attackType) {
        if (!combatState.canPerformAction(CombatState.ActionType.ATTACK)) return false;
        AttackConfig atk = getAttackConfig(attackType);
        long now = System.currentTimeMillis();
        return (now - lastAttackTimes[attackType]) >= atk.cooldown && mp >= atk.mpCost;
    }

    public long getRemainingCooldown(int attackType) {
        AttackConfig atk = getAttackConfig(attackType);
        long now = System.currentTimeMillis();
        long elapsed = now - lastAttackTimes[attackType];
        return Math.max(0, atk.cooldown - elapsed);
    }

    public String getCooldownDisplay(int attackType) {
        long remaining = getRemainingCooldown(attackType);
        return (remaining <= 0) ? "Ready" : String.format("%.1fs", remaining / 1000.0);
    }
    
    public String getAttackDisplayName(int attackType) {
        AttackConfig atk = getAttackConfig(attackType);
        return atk.displayName;
    }

    public void updateCombat() { combatState.update(); }

    // ====== RESTING ======
    public boolean canRest() {
        long now = System.currentTimeMillis();
        return (now - lastRestTime) >= REST_COOLDOWN;
    }

    public long getRemainingRestCooldown() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRestTime;
        return Math.max(0, REST_COOLDOWN - elapsed);
    }

    public String getRestCooldownDisplay() {
        long remaining = getRemainingRestCooldown();
        return (remaining <= 0) ? "Ready" : String.format("%.1fs", remaining / 1000.0);
    }

    public void rest() {
        if (!canRest()) return;
        hp = Math.min(maxHp, hp + (int)(maxHp * getRestPercent("restHpPercent")));
        mp = Math.min(maxMp, mp + (int)(maxMp * getRestPercent("restMpPercent")));
        lastRestTime = System.currentTimeMillis();
    }

    private double getRestPercent(String key) {
        String val = Config.getSetting(key);
        return Double.parseDouble(val.trim());
    }

    // ====== ATTACK HANDLING ======
    public int getAttackDamage(int attackType) {
        AttackConfig atk = getAttackConfig(attackType);
        return utils.Dice.rolldice(atk.diceCount, atk.diceSides, atk.diceBonus);
    }

    public int getTotalAttackDamage(int attackType) {
        int dmg = getAttackDamage(attackType);
        if (equippedWeapon != null) dmg += equippedWeapon.getDamageBonus();
        return dmg;
    }

    public void attack(int attackType) {
        if (!canAttack(attackType)) return;
        AttackConfig atk = getAttackConfig(attackType);

        mp -= atk.mpCost;
        combatState.startAttack(attackType, AnimationUtil.getPlayerAnimationDuration("attack", getPlayerClass(), attackType));
        lastAttackTimes[attackType] = System.currentTimeMillis();
    }

    public void useItem(int slot) {
        inventory.useItem(slot, this);
    }

    // ====== ABSTRACT METHODS ======
    protected abstract PlayerLevelStats getLevelStats(int level);

    protected void updateStatsForLevel() {
        PlayerLevelStats stats = getLevelStats(level);
        
        // Check if this is initialization (player has no stats yet)
        boolean isInitializing = (maxHp == 0 && maxMp == 0);
        
        if (isInitializing) {
            // First time initialization: start at full stats
            this.maxHp = stats.maxHp;
            this.maxMp = stats.maxMp;
            this.hp = maxHp;
            this.mp = maxMp;
            this.expToNext = stats.expToNextLevel;
        } else {
            // Level up: calculate stat increases
            int hpIncrease = stats.maxHp - maxHp;
            int mpIncrease = stats.maxMp - maxMp;
            
            // Update max stats
            this.maxHp = stats.maxHp;
            this.maxMp = stats.maxMp;
            this.expToNext = stats.expToNextLevel;
            
            // Reward player with half of the gained stats (prevents full heal exploit)
            this.hp = Math.min(maxHp, hp + (hpIncrease / 2));
            this.mp = Math.min(maxMp, mp + (mpIncrease / 2));
        }
    }

    protected AttackConfig getAttackConfig(int attackType) {
        PlayerLevelStats stats = getLevelStats(level);
        int idx = Math.max(0, Math.min(attackType - 1, stats.attacks.length - 1));
        return stats.attacks[idx];
    }

    // ====== XP HELPER METHODS ======
    
    /**
     * Calculates XP needed to reach the next level from current level.
     * @param currentLevel The current player level
     * @return XP needed for next level, or 0 if at max level
     */
    protected int getExpToNextLevel(int currentLevel) {
        int idx = Math.max(0, Math.min(currentLevel - 1, LEVEL_XP_THRESHOLDS.length - 1));
        if (idx + 1 < LEVEL_XP_THRESHOLDS.length) {
            return LEVEL_XP_THRESHOLDS[idx + 1] - LEVEL_XP_THRESHOLDS[idx];
        }
        return 0; // Max level reached
    }
}
