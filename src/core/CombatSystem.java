package core;

import enemies.Enemy;
import java.util.List;
import player.AbstractPlayer;
import player.Duelist;
import player.Wizard;
import utils.Dice;
import utils.LineUtils;

public class CombatSystem {

    public static class CombatResult {
        public final boolean hit;
        public final int damage;
        public final boolean critical;
        public final boolean targetDied;
        public final String attackName;

        public CombatResult(boolean hit, int damage, boolean critical, boolean targetDied, String attackName) {
            this.hit = hit;
            this.damage = damage;
            this.critical = critical;
            this.targetDied = targetDied;
            this.attackName = attackName;
        }
    }

    public static boolean isAdjacent(int x1, int y1, int x2, int y2) {
        return LineUtils.isCardinallyAdjacent(x1, y1, x2, y2);
    }

    public static CombatResult playerAttack(AbstractPlayer player, int attackType, List<Enemy> enemies) {
        Enemy target = enemies.stream()
            .filter(e -> !e.isDead() && isAdjacent(player.getX(), player.getY(), e.getX(), e.getY()))
            .findFirst()
            .orElse(null);

        if (target == null) return null;

        String attackName = getPlayerAttackName(player, attackType);
        int damage = getPlayerAttackDamage(player, attackType);
        boolean isCritical = checkPlayerCriticalHit(attackType);

        if (isCritical) {
            damage = (damage * getPlayerCriticalMultiplier(player)) / 100;
        }

        boolean targetDied = target.takeDamage(damage);
        return new CombatResult(true, damage, isCritical, targetDied, attackName);
    }

    public static CombatResult enemyAttack(Enemy enemy, AbstractPlayer player) {
        if (!isAdjacent(enemy.getX(), enemy.getY(), player.getX(), player.getY())) return null;
        int damage = enemy.getAttackDamage();
        boolean playerDied = player.takeDamage(damage);
        return new CombatResult(true, damage, false, playerDied, "Attack");
    }

    public static boolean shouldEnemyNoticePlayer(Enemy enemy, int playerX, int playerY) {
        double distance = LineUtils.euclideanDistance(enemy.getX(), enemy.getY(), playerX, playerY);
        return distance <= 5.0; // TODO: move to enemy config
    }

    private static String getPlayerAttackName(AbstractPlayer player, int attackType) {
        if (player instanceof Duelist) return attackType == 1 ? "Quick Strike" : "Power Attack";
        if (player instanceof Wizard) return attackType == 1 ? "Fire Spell" : "Ice Spell";
        return "Attack";
    }

    private static int getPlayerAttackDamage(AbstractPlayer player, int attackType) {
        if (player instanceof Duelist) return attackType == 1 ? 10 : 15;
        if (player instanceof Wizard) return attackType == 1 ? 12 : 10;
        return 8;
    }

    private static boolean checkPlayerCriticalHit(int attackType) {
        int critChance = 10 + (attackType == 2 ? 5 : 0);
        return Dice.checkChance(critChance);
    }

    private static int getPlayerCriticalMultiplier(AbstractPlayer player) {
        return player instanceof Duelist ? 200 : 180;
    }
}
