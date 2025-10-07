package enemies;

import config.AnimationUtil;
import config.EnemyConfig;
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
    protected int expReward;                // Experience reward
    protected EnemyConfig.EnemyStats stats; // Config stats
    
    // ====== COMBAT ======
    protected CombatState combatState = new CombatState();

    // ====== MOVEMENT ======
    /** Base movement cooldown in milliseconds (divided by movementSpeed stat) */
    private static final int BASE_MOVEMENT_COOLDOWN = 800;

    // ====== DIRECTION ======
    protected int facingDirection = 1; // Default facing right

    // ====== TIMING & AI ======
    protected long lastMoveTime = 0, lastAttackTime = 0;
    protected boolean hasNoticedPlayer = false;
    protected Random random = new Random();

    @Override public int getFacingDirection() { return facingDirection; }
    @Override public void setFacingDirection(int dir) { this.facingDirection = ((dir % 4) + 4) % 4; }

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
        expReward = stats.expReward;
    }

    // ====== BASIC GETTERS/SETTERS ======
    @Override public int getX() { return x; }
    @Override public int getY() { return y; }
    @Override
    public void setPosition(int x, int y) {
        int dx = x - this.x;
        if (dx != 0) {
            setFacingDirection(dx > 0 ? 1 : 3); // 1=E, 3=W
        }
        this.x = x;
        this.y = y;
        lastMoveTime = System.currentTimeMillis();
    }
    @Override public int getHp() { return hp; }
    @Override public int getMaxHp() { return maxHp; }
    @Override public void setHp(int hp) { this.hp = Math.max(0, Math.min(hp, maxHp)); }
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

        if (hp == 0) {
            combatState.setState(CombatState.State.DYING, AnimationUtil.getEnemyAnimationDuration("death", type.getSpritePrefix()));
            return true;
        } 
        combatState.setState(CombatState.State.HURT, AnimationUtil.getEnemyAnimationDuration("hurt", type.getSpritePrefix()));
        return false;
    }

    @Override
    public int getAttackDamage() {
        int attackType = combatState.getAttackType(); 
        int index = attackType - 1;

        return Dice.calculateDamage(
            stats.attackDice[index],      // number of dice
            stats.attackDiceSides[index], // sides per die
            stats.attackModifiers[index]  // modifier
        );
    }

    private boolean isAdjacent(int px, int py) {
        return LineUtils.isCardinallyAdjacent(x, y, px, py);
    }

    private boolean canMove() {
        long now = System.currentTimeMillis();
        return combatState.canPerformAction(CombatState.ActionType.MOVE) && now - lastMoveTime >= (BASE_MOVEMENT_COOLDOWN / stats.movementSpeed);
    }

    private boolean canAttack() {
        long now = System.currentTimeMillis();
        return combatState.canPerformAction(CombatState.ActionType.ATTACK) && now - lastAttackTime >= stats.attackCooldown;
    }

    private void attemptAttack() {
        if (!canAttack()) return;
        int atkType = selectAttackType();
        combatState.startAttack(atkType, AnimationUtil.getEnemyAnimationDuration("attack", type.getSpritePrefix(), atkType));
        lastAttackTime = System.currentTimeMillis();
    }

    private int selectAttackType() {
        int roll = random.nextInt(100), cumulative = 0;
        for (int i = 0; i < stats.attackChances.length; i++) { // use 0-based index
            cumulative += stats.attackChances[i];
            if (roll < cumulative) return i + 1; // return 1-based for getAttackDamage()
        }
        return 1; // fallback
    }

    // ====== MOVEMENT ======
    private boolean executeMovement(CollisionManager.Position pos) {
        if (!canMove() || collisionManager == null || pos == null) return false;
        setPosition(pos.x, pos.y);
        // MOVING state persists until tile reached
        if (combatState.getCurrentState() != CombatState.State.MOVING)
            combatState.setState(CombatState.State.MOVING, AnimationUtil.getEnemyAnimationDuration("walk", type.getSpritePrefix()));
        return true;
    }

    private void performBrownianMovement() {
        executeMovement(collisionManager != null ? collisionManager.findRandomMove(this, 4, random) : null);
    }

    protected void moveToward(int px, int py) {
        CollisionManager.Position next = collisionManager != null ? collisionManager.findSmartMoveToward(this, px, py) : null;
        if (next == null) return;

        int dx = next.x - x;
        int dy = next.y - y;
        if (dx > 0) setFacingDirection(1);          // East
        else if (dx < 0) setFacingDirection(3);     // West
        else if (dy < 0) setFacingDirection(0);     // North
        else if (dy > 0) setFacingDirection(2);     // South

        executeMovement(next);
    }

    private boolean isCurrentlyMoving() {
        long now = System.currentTimeMillis();
        return now - lastMoveTime < (BASE_MOVEMENT_COOLDOWN / stats.movementSpeed);
    }

    // ====== AI LOOP ======
    @Override
    public void update(int px, int py) {
        combatState.update();
        if (isDead()) return;
        
        // Handle attack
        if (combatState.getCurrentState() == CombatState.State.ATTACKING) {
            if (combatState.shouldDealDamage(AnimationUtil.getDamageTimingPercent())) dealDamageToPlayer(px, py);
            return;
        }

        // Handle movement
        if (combatState.canPerformAction(CombatState.ActionType.MOVE)) {
            double dist = Math.hypot(px - x, py - y);
            
            // Notice player if within notice radius
            if (dist <= stats.noticeRadius && !hasNoticedPlayer) {
                hasNoticedPlayer = true;
                if (combatLogger != null) combatLogger.accept(getNoticeMessage());
            }
            
            // Lose track of player if they get too far away
            if (hasNoticedPlayer && dist > stats.noticeRadius * 1.5) {
                hasNoticedPlayer = false;
                if (combatLogger != null) combatLogger.accept(name + " seems to have lost your tracks!");
            }

            if (hasNoticedPlayer) {
                if (isAdjacent(px, py)) attemptAttack();
                else moveToward(px, py);
            } else performBrownianMovement();

            // Finish MOVING when tile reached
            if (combatState.getCurrentState() == CombatState.State.MOVING && !isCurrentlyMoving()) {
                combatState.finishState();
            }
        }
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

    // ====== ABSTRACT METHODS ======
    protected abstract String getNoticeMessage(); // Each enemy provides its own notice message that is logged when it first notices the player
}