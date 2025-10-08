package enemies;

import config.AnimationUtil;
import config.EnemyConfig;
import core.CombatState;
import java.util.Random;
import java.util.function.Consumer;
import utils.CollisionSystem;
import utils.GeometryHelpers;
import utils.PathfindingAlgorithms;
import utils.Positionable;

/**
 * Base class for all enemies.
 * Handles stats, combat, AI, and movement logic.
 */
public abstract class AbstractEnemy implements Enemy, Positionable {

    // ====== CORE STATS ======
    protected int x, y;
    protected EnemyType type;
    protected String name;
    protected int hp, maxHp;
    protected int expReward;
    protected EnemyConfig.EnemyStats stats;

    // ====== COMBAT ======
    protected CombatState combatState = new CombatState();

    // ====== MOVEMENT ======
    private static final int BASE_MOVE_COOLDOWN = 800; // ms
    protected int facingDirection = 1; // Default facing east
    protected long lastMoveTime = 0, lastAttackTime = 0;
    protected boolean hasNoticedPlayer = false;
    protected Random random = new Random();

    // ====== DAMAGE TRACKING ======
    private int pendingPlayerDamage = 0;

    // ====== GLOBAL SYSTEM HOOKS ======
    private static CollisionSystem collisionManager;
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
        expReward = stats.expReward;
    }

    // ====== GETTERS/SETTERS ======
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override public void setPosition(int newX, int newY) {
        updateFacing(newX - x, newY - y);
        x = newX;
        y = newY;
        lastMoveTime = System.currentTimeMillis();
    }
    @Override public int getHp() { return hp; }
    @Override public int getMaxHp() { return maxHp; }
    @Override public void setHp(int value) { hp = Math.max(0, Math.min(value, maxHp)); }
    @Override public String getName() { return name; }
    @Override public boolean isDead() { return hp <= 0; }
    @Override public int getExpReward() { return expReward; }
    public EnemyType getType() { return type; }
    public CombatState getCombatState() { return combatState; }
    @Override public boolean hasNoticedPlayer() { return hasNoticedPlayer; }
    public int getAttackType() { return combatState.getAttackType(); }

    // ====== COMBAT ======
    @Override
    public boolean takeDamage(int damage) {
        if (isDead()) return false;
        hp = Math.max(0, hp - damage);

        CombatState.State newState = (hp == 0) ? CombatState.State.DYING : CombatState.State.HURT;
        int duration = AnimationUtil.getEnemyAnimationDuration((hp==0?"death":"hurt"), type.getSpritePrefix());
        combatState.setState(newState, duration);
        return hp == 0;
    }

    @Override
    public int getAttackDamage() {
        int idx = combatState.getAttackType() - 1;
        return utils.Dice.calculateDamage(stats.attackDice[idx], stats.attackDiceSides[idx], stats.attackModifiers[idx]);
    }

    private boolean isAdjacent(int px, int py) { return GeometryHelpers.isCardinallyAdjacent(x, y, px, py); }

    // ====== MOVEMENT ======
    private void updateFacing(int dx, int dy) {
        if (dx > 0) facingDirection = 1; // E
        else if (dx < 0) facingDirection = 3; // W
        else if (dy < 0) facingDirection = 0; // N
        else if (dy > 0) facingDirection = 2; // S
    }

    private boolean canMove() { return combatState.canPerformAction(CombatState.ActionType.MOVE) &&
            System.currentTimeMillis() - lastMoveTime >= BASE_MOVE_COOLDOWN / stats.movementSpeed; }

    private boolean canAttack() { return combatState.canPerformAction(CombatState.ActionType.ATTACK) &&
            System.currentTimeMillis() - lastAttackTime >= stats.attackCooldown; }

    private void attemptAttack() {
        if (!canAttack()) return;
        int atkType = selectAttackType();
        combatState.startAttack(atkType, AnimationUtil.getEnemyAnimationDuration("attack", type.getSpritePrefix(), atkType));
        lastAttackTime = System.currentTimeMillis();
    }

    private int selectAttackType() {
        int roll = random.nextInt(100), cumulative = 0;
        for (int i = 0; i < stats.attackChances.length; i++) {
            cumulative += stats.attackChances[i];
            if (roll < cumulative) return i + 1;
        }
        return 1;
    }

    protected void moveToward(int px, int py) {
        if (collisionManager == null) return;
        GeometryHelpers.Position next = PathfindingAlgorithms.findSmartMoveToward(x, y, px, py, 
            (nx, ny) -> collisionManager.canMoveTo(this, nx, ny));
        if (next != null) executeMovement(next);
    }

    private void performBrownianMovement() { 
        if (collisionManager == null) return;
        GeometryHelpers.Position next = PathfindingAlgorithms.findRandomMove(x, y, 4, random, 
            (nx, ny) -> collisionManager.canMoveTo(this, nx, ny));
        executeMovement(next);
    }

    private boolean executeMovement(GeometryHelpers.Position pos) {
        if (!canMove() || pos == null) return false;
        setPosition(pos.x, pos.y);
        if (combatState.getCurrentState() != CombatState.State.MOVING)
            combatState.setState(CombatState.State.MOVING, AnimationUtil.getEnemyAnimationDuration("walk", type.getSpritePrefix()));
        return true;
    }

    private boolean isCurrentlyMoving() { return System.currentTimeMillis() - lastMoveTime < BASE_MOVE_COOLDOWN / stats.movementSpeed; }

    // ====== AI LOOP ======
    @Override
    public void update(int px, int py) {
        combatState.update();
        if (isDead()) return;

        // Attacking
        if (combatState.getCurrentState() == CombatState.State.ATTACKING) {
            if (combatState.shouldDealDamage(AnimationUtil.getDamageTimingPercent())) dealDamageToPlayer(px, py);
            return;
        }

        // Movement & noticing player
        double dist = GeometryHelpers.getEuclideanDistance(x, y, px, py);
        handlePlayerAwareness(dist);

        if (hasNoticedPlayer) {
            if (isAdjacent(px, py)) attemptAttack();
            else moveToward(px, py);
        } else performBrownianMovement();

        if (combatState.getCurrentState() == CombatState.State.MOVING && !isCurrentlyMoving())
            combatState.finishState();
    }

    private void handlePlayerAwareness(double dist) {
        if (dist <= stats.noticeRadius && !hasNoticedPlayer) {
            hasNoticedPlayer = true;
            logNotice(getNoticeMessage());
        } else if (hasNoticedPlayer && dist > stats.noticeRadius * 1.5) {
            hasNoticedPlayer = false;
            logNotice(name + " seems to have lost your tracks!");
        }
    }

    // ====== DAMAGE TO PLAYER ======
    private void dealDamageToPlayer(int px, int py) {
        if (!isAdjacent(px, py)) return;
        pendingPlayerDamage = getAttackDamage();
        logNotice(getName() + " attacks for " + pendingPlayerDamage + " damage!");
    }

    public int getPendingPlayerDamage() { int dmg = pendingPlayerDamage; pendingPlayerDamage = 0; return dmg; }
    public boolean hasPendingPlayerDamage() { return pendingPlayerDamage > 0; }

    // ====== POSITIONABLE INTERFACE ======
    @Override
    public int getFacingDirection() { return facingDirection; }
    
    @Override
    public void setFacingDirection(int dir) { this.facingDirection = dir; }

    // ====== STATIC SYSTEM HOOKS ======
    public static void setCollisionManager(CollisionSystem manager) { collisionManager = manager; }
    public static void setCombatLogger(Consumer<String> logger) { combatLogger = logger; }
    private void logNotice(String msg) { if (combatLogger != null) combatLogger.accept(msg); }

    // ====== ABSTRACT ======
    protected abstract String getNoticeMessage();
}
