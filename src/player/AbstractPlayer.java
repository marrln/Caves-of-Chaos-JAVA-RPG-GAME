package player;

import config.AnimationConfig;
import config.Config;
import core.CombatState;
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

    // ====== COMBAT ======
    protected CombatState combatState = new CombatState();
    protected long lastAttackTime = 0;

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
    public void setPosition(int x, int y) { this.x = x; this.y = y; }

    public boolean tryMoveTo(int newX, int newY) {
        if (collisionManager.canMoveTo(this, newX, newY)) {
            setPosition(newX, newY);
            return true;
        }
        return false;
    }

    public boolean tryMoveDirection(int direction) {
        CollisionManager.Position offset = CollisionManager.getDirectionOffset(direction);
        return (offset.x != 0 || offset.y != 0) && tryMoveTo(x + offset.x, y + offset.y);
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
        return (now - lastAttackTime) >= atk.cooldown && mp >= atk.mpCost;
    }

    public void updateCombat() { combatState.update(); }

    // ====== RESTING ======
    public void rest() {
        hp = Math.min(maxHp, hp + (int)(maxHp * getRestPercent("restHpPercent", 0.05)));
        mp = Math.min(maxMp, mp + (int)(maxMp * getRestPercent("restMpPercent", 0.05)));
    }

    private double getRestPercent(String key, double def) {
        String val = Config.getSetting(key);
        return val != null ? Double.parseDouble(val.trim()) : def;
    }

    // ====== ABSTRACT METHODS ======
    public abstract void attack(int attackType);
    public abstract void useItem(int slot);
    protected abstract void updateStatsForLevel();
    public abstract int getAttackDamage();
    protected abstract PlayerConfig.AttackConfig getAttackConfig(int attackType);
}
