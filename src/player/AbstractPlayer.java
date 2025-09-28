package player;

import config.AnimationConfig;
import config.Config;
import config.PlayerConfig;
import core.CombatState;
import items.Inventory;
import items.Weapon;
import utils.CollisionManager;

public abstract class AbstractPlayer implements CollisionManager.Positionable {

    // ====== CORE STATS ======
    protected int x, y;             // Position
    protected int hp, maxHp;        // Health
    protected int mp, maxMp;        // Mana
    protected int level = 1;        // Level
    protected int exp = 0;          // Experience
    protected int expToNext;        // Exp needed for next level
    protected int baseDamage;       // Base attack damage
    protected String name;          // Character name

    // ====== INVENTORY & EQUIPMENT ======
    protected Inventory inventory = new Inventory();
    protected Weapon equippedWeapon;

    // ====== COMBAT ======
    protected CombatState combatState = new CombatState();
    protected long[] lastAttackTimes = new long[3]; // Index 0 unused, 1 and 2 for attack types

    // ====== FOR EFFECT TRACKING ======
    private long healingEffectEndTime = 0;
    private long manaEffectEndTime = 0;
    private static final int EFFECT_DURATION = 800; // milliseconds

    // ====== DIRECTION ======
    /**
     * 0=N, 1=E (right), 2=S, 3=W (left)
     * Default facing is right (1). Used for rendering mirroring.
     */
    protected int facingDirection = 1;

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
        int dx = x - this.x;
        // Only update facing if moving left or right
        if (dx != 0) {
            setFacingDirection(dx > 0 ? 1 : 3); // 1=E, 3=W
        }
        this.x = x;
        this.y = y;
    }

    public boolean tryMoveTo(int newX, int newY) {
        if (collisionManager != null && collisionManager.canMoveTo(this, newX, newY)) {
            setPosition(newX, newY);
            return true;
        } else if (collisionManager == null) {
            setPosition(newX, newY);
            return true;
        }
        return false;
    }

    public boolean tryMoveDirection(int direction) {
        CollisionManager.Position offset = CollisionManager.getDirectionOffset(direction);
        boolean moved = (offset.x != 0 || offset.y != 0) && tryMoveTo(x + offset.x, y + offset.y);
        if (moved) setFacingDirection(direction);
        return moved;
    }

    @Override
    public int getFacingDirection() { return facingDirection; }

    @Override
    public void setFacingDirection(int dir) {
        this.facingDirection = ((dir % 4) + 4) % 4; // ensure 0-3
    }

    // ====== STATS & PROGRESSION ======
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getMp() { return mp; }
    public int getMaxMp() { return maxMp; }
    public int getLevel() { return level; }
    public int getExp() { return exp; }
    public int getExpToNext() { return expToNext; }
    public int getBaseDamage() { return baseDamage; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // ====== INVENTORY & EQUIPMENT ======
    public Inventory getInventory() { return inventory; }
    public Weapon getEquippedWeapon() { return equippedWeapon; }

    public boolean equipWeapon(Weapon weapon) {
        if (weapon != null && weapon.canUse(this)) {
            equippedWeapon = weapon;
            return true;
        }
        return false;
    }

    public Weapon unequipWeapon() {
        Weapon weapon = equippedWeapon;
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

    public void triggerHealingEffect() { healingEffectEndTime = System.currentTimeMillis() + EFFECT_DURATION; }
    public boolean isHealingEffectActive() { return System.currentTimeMillis() < healingEffectEndTime; }
    public void triggerManaEffect() { manaEffectEndTime = System.currentTimeMillis() + EFFECT_DURATION; }
    public boolean isManaEffectActive() { return System.currentTimeMillis() < manaEffectEndTime; }

    public void addExperience(int gained) {
        exp += gained;
        while (exp >= expToNext) levelUp();
    }

    private void levelUp() {
        exp -= expToNext;
        level++;
        updateStatsForLevel();
    }

    // ====== COMBAT ======
    public CombatState getCombatState() { return combatState; }

    public boolean takeDamage(int dmg) {
        hp = Math.max(0, hp - dmg);
        combatState.setState(CombatState.State.HURT, AnimationConfig.getPlayerAnimationDuration("hurt"));
        return hp == 0;
    }

    public boolean canAttack(int attackType) {
        if (!combatState.canPerformAction(CombatState.ActionType.ATTACK)) return false;
        PlayerConfig.AttackConfig atk = getAttackConfig(attackType);
        long now = System.currentTimeMillis();
        return (now - lastAttackTimes[attackType]) >= atk.cooldown && mp >= atk.mpCost;
    }

    public long getRemainingCooldown(int attackType) {
        PlayerConfig.AttackConfig atk = getAttackConfig(attackType);
        long now = System.currentTimeMillis();
        long elapsed = now - lastAttackTimes[attackType];
        return Math.max(0, atk.cooldown - elapsed);
    }

    public String getCooldownDisplay(int attackType) {
        long remaining = getRemainingCooldown(attackType);
        if (remaining <= 0) return "Ready";
        return String.format("%.1fs", remaining / 1000.0);
    }

    public void updateCombat() { combatState.update(); }

    // ====== RESTING ======
    public void rest() {
        hp = Math.min(maxHp, hp + (int)(maxHp * getRestPercent("restHpPercent")));
        mp = Math.min(maxMp, mp + (int)(maxMp * getRestPercent("restMpPercent")));
    }

    private double getRestPercent(String key) {
        String val = Config.getSetting(key);
        return Double.parseDouble(val.trim());
    }

    // ====== ABSTRACT METHODS ======
    public abstract void attack(int attackType);
    public abstract void useItem(int slot);
    protected abstract void updateStatsForLevel();
    public abstract int getAttackDamage(int attackType);
    protected abstract PlayerConfig.AttackConfig getAttackConfig(int attackType);
}
