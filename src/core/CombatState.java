package core;

import core.CombatState.ActionType;

public class CombatState {

    public enum State { IDLE, MOVING, ATTACKING, HURT, DYING }
    public enum ActionType { MOVE, ATTACK, USE_ITEM, REST }

    private State currentState;
    private long stateStartTime, stateDuration;
    private int attackType;
    private boolean damageDealt;

    public CombatState() { setState(State.IDLE, 0); }

    public State getCurrentState() { return currentState; }
    public int getAttackType() { return attackType; }

    public boolean isStateFinished() { return System.currentTimeMillis() >= stateStartTime + stateDuration; }

    public boolean shouldDealDamage(int damageTimingPercent) {
        if (currentState != State.ATTACKING || damageDealt) return false;

        long elapsed = System.currentTimeMillis() - stateStartTime;
        double progress = (double) elapsed / stateDuration;

        if (progress >= damageTimingPercent / 100.0) {
            damageDealt = true;
            return true; 
        }
        return false;
    }

    public boolean canPerformAction(ActionType action) {
        return switch (currentState) {
            case IDLE -> true;
            case MOVING -> action == ActionType.MOVE || action == ActionType.ATTACK;
            default -> false;
        };
    }

    public void setState(State newState, long duration) {
        currentState = newState;
        stateStartTime = System.currentTimeMillis();
        stateDuration = duration;
        if (newState != State.ATTACKING) attackType = -1;
        damageDealt = false; // always reset for any new state
    }

    public void startAttack(int attackType, long duration) { 
        this.attackType = attackType; 
        setState(State.ATTACKING, duration); 
    }

    public void finishState() { stateStartTime = stateDuration = 0; }
    public void update() { if (isStateFinished() && currentState != State.IDLE) setState(State.IDLE, 0); }
}
