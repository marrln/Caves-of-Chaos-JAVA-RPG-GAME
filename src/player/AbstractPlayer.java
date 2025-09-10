package player;

import config.Config;

/**
 * Abstract base class for all player characters.
 */
public abstract class AbstractPlayer {
    protected int x, y;       // Position in the map
    protected int hp, maxHp;  // Health
    protected int mp, maxMp;  // Mana (0 for non-magical classes)
    protected String name;    // Character name
    protected int level;      // Character level
    
    public AbstractPlayer(int x, int y) {
        this.x = x;
        this.y = y;
        this.level = 1;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getHp() {
        return hp;
    }
    
    public int getMaxHp() {
        return maxHp;
    }

    public int getMp() {
        return mp;
    }
    
    public int getMaxMp() {
        return maxMp;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void rest(){
        double restHpPercent = getRestHpPercentFromConfig();
        double restMpPercent = getRestMpPercentFromConfig();
        
        hp = Math.min(maxHp, hp + (int)(maxHp * restHpPercent));
        mp = Math.min(maxMp, mp + (int)(maxMp * restMpPercent)); 
    }

    private double getRestHpPercentFromConfig() {
        String hpPercentStr = Config.getSetting("restHpPercent");
        return hpPercentStr != null ? Double.parseDouble(hpPercentStr.trim()) : 0.05;
    }

    private double getRestMpPercentFromConfig() {
        String mpPercentStr = Config.getSetting("restMpPercent");
        return mpPercentStr != null ? Double.parseDouble(mpPercentStr.trim()) : 0.05;
    }

    public abstract void attack(int attackType);
    public abstract void useItem(int slot);
}
