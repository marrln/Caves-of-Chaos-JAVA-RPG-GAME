package player;

import config.PlayerConfig;

public class Duelist extends AbstractPlayer {
    public Duelist(int x, int y) {
        super(x, y);
        this.name = "Duelist";
    }

    @Override
    protected PlayerConfig.PlayerLevelStats getLevelStats(int level) {
        return PlayerConfig.getDuelistStats(level);
    }
}
