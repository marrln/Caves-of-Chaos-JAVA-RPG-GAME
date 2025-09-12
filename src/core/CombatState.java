package core;

/**
 * Represents the current state of an entity during combat.
 * This includes animation states and timing information that affects
 * what actions the entity can perform.
 */
public class CombatState {
    
    /**
     * The different states an entity can be in during combat.
     */
    public enum State {
        IDLE,           // Can perform any action
        MOVING,         // Currently moving (can be interrupted)
        ATTACKING,      // Performing an attack (damage dealt at end)
        HURT,           // Taking damage (cannot perform any actions)
        DYING           // Death animation playing
    }
    
    private State currentState;
    private long stateStartTime;      // When the current state began
    private long stateDuration;       // How long the state should last
    private int attackType;           // Which attack is being performed (if attacking)
    private boolean damageDealt;      // Whether attack damage has been dealt yet
    
    /**
     * Creates a new combat state in IDLE.
     */
    public CombatState() {
        this.currentState = State.IDLE;
        this.stateStartTime = System.currentTimeMillis();
        this.stateDuration = 0;
        this.attackType = -1;
        this.damageDealt = false;
    }
    
    /**
     * Gets the current state.
     * 
     * @return The current combat state
     */
    public State getCurrentState() {
        return currentState;
    }
    
    /**
     * Gets the attack type currently being performed.
     * 
     * @return The attack type, or -1 if not attacking
     */
    public int getAttackType() {
        return attackType;
    }
    
    /**
     * Checks if the current state has finished.
     * 
     * @return true if the state duration has elapsed
     */
    public boolean isStateFinished() {
        return System.currentTimeMillis() >= stateStartTime + stateDuration;
    }
    
    /**
     * Checks if damage should be dealt now during an attack.
     * This is called during the attack animation to determine the right timing.
     * 
     * @param damageTimingPercent The percentage through the animation when damage occurs (0-100)
     * @return true if damage should be dealt now
     */
    public boolean shouldDealDamage(int damageTimingPercent) {
        if (currentState != State.ATTACKING || damageDealt) {
            return false;
        }
        
        long elapsed = System.currentTimeMillis() - stateStartTime;
        double progress = (double) elapsed / stateDuration;
        double targetProgress = damageTimingPercent / 100.0;
        
        if (progress >= targetProgress) {
            damageDealt = true;
            return true;
        }
        
        return false;
    }
    
    /**
     * Checks if the entity can perform the specified action.
     * 
     * @param action The action to check
     * @return true if the action is allowed in the current state
     */
    public boolean canPerformAction(ActionType action) {
        return switch (currentState) {
            case IDLE -> true;                    // Can do anything when idle
            case MOVING -> action == ActionType.MOVE || action == ActionType.ATTACK; // Can change direction or start attack
            case ATTACKING -> false;              // Cannot interrupt attacks
            case HURT -> false;                   // Cannot do anything when hurt
            case DYING -> false;                  // Cannot do anything when dying
        };
    }
    
    /**
     * Transitions to a new state with the specified duration.
     * 
     * @param newState The state to transition to
     * @param duration The duration of the new state in milliseconds
     */
    public void setState(State newState, long duration) {
        this.currentState = newState;
        this.stateStartTime = System.currentTimeMillis();
        this.stateDuration = duration;
        this.damageDealt = false;
        
        if (newState != State.ATTACKING) {
            this.attackType = -1;
        }
    }
    
    /**
     * Starts an attack state with the specified attack type and duration.
     * 
     * @param attackType The type of attack being performed
     * @param duration The duration of the attack animation
     */
    public void startAttack(int attackType, long duration) {
        this.currentState = State.ATTACKING;
        this.stateStartTime = System.currentTimeMillis();
        this.stateDuration = duration;
        this.attackType = attackType;
        this.damageDealt = false;
    }
    
    /**
     * Forces the state to finish immediately.
     * Used for interrupting states or when external events occur.
     */
    public void finishState() {
        this.stateStartTime = 0;
        this.stateDuration = 0;
    }
    
    /**
     * Updates the combat state, transitioning to IDLE if the current state has finished.
     */
    public void update() {
        if (isStateFinished() && currentState != State.IDLE) {
            setState(State.IDLE, 0);
        }
    }
    
    /**
     * Different types of actions an entity can attempt to perform.
     */
    public enum ActionType {
        MOVE,
        ATTACK,
        USE_ITEM,
        REST
    }
}
