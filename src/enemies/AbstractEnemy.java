package enemies;

import config.AnimationConfig;
import core.CombatState;
import java.util.Random;
import java.util.function.Consumer;
import utils.CollisionManager;
import utils.Dice;
import utils.LineUtils;

public abstract class AbstractEnemy implements Enemy, CollisionManager.Positionable {
    
    // ====== CORE STATS ======
    protected int x, y;                     // Position
    protected EnemyType type;               // Enemy classification
    protected String name;                  // Display name
    protected int hp, maxHp;                // Health
    protected int attackDamage;             // Base attack damage
    protected int expReward;                // Experience reward
    protected EnemyConfig.EnemyStats stats; // Config stats
    
    // ====== COMBAT ======
    protected CombatState combatState = new CombatState();

    // ====== TIMING & AI ======
    protected long lastMoveTime = 0, lastAttackTime = 0;
    protected boolean hasNoticedPlayer = false;
    protected Random random = new Random();

    // ====== DAMAGE TRACKING ======
    private int pendingPlayerDamage = 0;

    // ====== GLOBAL SYSTEM HOOKS ======
    private static CollisionManager collisionManager;
    private static Consumer<String> combatLogger;

    // ====== CONSTRUCTOR ======
    public AbstractEnemy(int x, int y, EnemyType type) {
        this.x = x;
        this.y = y;
        this.type = type;

        stats = EnemyConfig.getStats(type);
        name = type.getDisplayName();
        maxHp = stats.baseHp;
        hp = maxHp;
        attackDamage = stats.baseDamage;
        expReward = stats.expReward;
    }

    // ====== BASIC GETTERS/SETTERS ======
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public void setPosition(int x, int y) { this.x = x; this.y = y; }
    @Override public int getHp() { return hp; }
    @Override public int getMaxHp() { return maxHp; }
    @Override public void setHp(int hp) { this.hp = Math.max(0, Math.min(hp, maxHp)); }
    @Override public String getName() { return name; }
    @Override public boolean isDead() { return hp <= 0; }
    @Override public int getExpReward() { return expReward; }

    public EnemyType getType() { return type; }
    public CombatState getCombatState() { return combatState; }
    public boolean hasNoticedPlayer() { return hasNoticedPlayer; }
    public int getAttackType() { return combatState.getAttackType(); }

    // ====== COMBAT ======
    @Override
    public boolean takeDamage(int damage) {
        if (isDead()) return false;
        hp = Math.max(0, hp - damage);

        if (hp == 0) {
            combatState.setState(CombatState.State.DYING, AnimationConfig.getEnemyAnimationDuration("death"));
            return true;
        } 
        combatState.setState(CombatState.State.HURT, AnimationConfig.getEnemyAnimationDuration("hurt"));
        return false;
    }

    @Override
    public int getAttackDamage() {
        int type = combatState.getAttackType();
        if (type < 0 || type >= stats.attackDamageMultipliers.length) type = 0;
        int base = (stats.baseDamage * stats.attackDamageMultipliers[type]) / 100;
        return Dice.calculateDamage(base, stats.diceSides, stats.variationPercent);
    }

    private boolean isAdjacent(int px, int py) {
        return LineUtils.isCardinallyAdjacent(x, y, px, py);
    }

    private boolean canMove() {
        long now = System.currentTimeMillis();
        return combatState.canPerformAction(CombatState.ActionType.MOVE) && now - lastMoveTime >= (EnemyConfig.getBaseMovementCooldown() / stats.movementSpeed);
    }

    private boolean canAttack() {
        long now = System.currentTimeMillis();
        return combatState.canPerformAction(CombatState.ActionType.ATTACK) && now - lastAttackTime >= stats.attackCooldown;
    }

    private void attemptAttack() {
        if (!canAttack()) return;
        int atkType = selectAttackType();
        combatState.startAttack(atkType, AnimationConfig.getEnemyAnimationDuration("attack"));
        lastAttackTime = System.currentTimeMillis();
    }

    private int selectAttackType() {
        int roll = random.nextInt(100), cumulative = 0;
        for (int i = 0; i < stats.attackChances.length; i++) {
            cumulative += stats.attackChances[i];
            if (roll < cumulative) return i;
        }
        return 0; // Fallback
    }

    // ====== MOVEMENT ======
    private boolean executeMovement(CollisionManager.Position pos) {
        if (!canMove() || collisionManager == null || pos == null) return false;
        x = pos.x; y = pos.y; lastMoveTime = System.currentTimeMillis();
        return true;
    }

    private void performBrownianMovement() {
        executeMovement(collisionManager != null ? collisionManager.findRandomMove(this, EnemyConfig.getMaxRandomMoveAttempts(), random) : null);
    }

    protected void moveToward(int px, int py) {
        executeMovement(collisionManager != null ? collisionManager.findSmartMoveToward(this, px, py) : null);
    }

    // ====== AI LOOP ======
    @Override
    public void update(int px, int py) {
        combatState.update();
        if (isDead()) return;

        if (combatState.getCurrentState() == CombatState.State.ATTACKING) {
            if (combatState.shouldDealDamage(AnimationConfig.getDamageTimingPercent())) dealDamageToPlayer(px, py);
            return;
        }

        if (!combatState.canPerformAction(CombatState.ActionType.MOVE)) return;

        double dist = Math.hypot(px - x, py - y);
        if (dist <= stats.noticeRadius) hasNoticedPlayer = true;

        if (hasNoticedPlayer) {
            if (isAdjacent(px, py)) attemptAttack();
            else moveToward(px, py);
        } else performBrownianMovement();
    }

    // ====== DAMAGE TO PLAYER ======
    private void dealDamageToPlayer(int px, int py) {
        if (!isAdjacent(px, py)) return;
        int dmg = getAttackDamage();
        if (combatLogger != null) combatLogger.accept(getName() + " attacks for " + dmg + " damage!");
        pendingPlayerDamage = dmg;
    }

    public int getPendingPlayerDamage() {
        int dmg = pendingPlayerDamage;
        pendingPlayerDamage = 0;
        return dmg;
    }

    public boolean hasPendingPlayerDamage() { return pendingPlayerDamage > 0; }

    // ====== STATIC SETTERS ======
    public static void setCollisionManager(CollisionManager manager) { collisionManager = manager; }
    public static void setCombatLogger(Consumer<String> logger) { combatLogger = logger; }
}