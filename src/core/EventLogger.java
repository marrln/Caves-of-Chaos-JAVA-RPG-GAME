package core;

import config.StyleConfig;
import java.awt.Color;
import player.AbstractPlayer;
import ui.GameUIManager;

public class EventLogger {
    private GameUIManager uiManager;
    public void setUIManager(GameUIManager uiManager) { this.uiManager = uiManager; }

    public void log(String message) { if (uiManager != null) uiManager.addMessage(message); }
    public void log(String message, Color color) { if (uiManager != null) uiManager.addMessage(message, color); }
    public void log(String message, String colorKey) { log(message, StyleConfig.getColor(colorKey)); }

    // Movement
    public void logFloorDescend(int newFloor) { log("You descend to floor " + newFloor + "."); }
    public void logFloorAscend(int newFloor) { log("You climb back to floor " + newFloor + "."); }

    // Rest & Restoration
    public void logRest(AbstractPlayer p, int hpRestored, int mpRestored) {
        if (hpRestored > 0)
            log("Rested: Restored " + hpRestored + " HP (" + p.getHp() + "/" + p.getMaxHp() + " HP)", "success");
        if (mpRestored > 0)
            log("Rested: Restored " + mpRestored + " MP (" + p.getMp() + "/" + p.getMaxMp() + " MP)", "success");
        if (hpRestored == 0 && mpRestored == 0)
            log("Rested: No effect, already at full stats.");
    }

    public void logCannotRestWhileChased() { log("You cannot rest while enemies are chasing you!", "danger"); }

    public void logPotionUsed(String potion, AbstractPlayer p, int hpRestored, int mpRestored) {
        if (hpRestored > 0)
            log("Used " + potion + ": Restored " + hpRestored + " HP (" + p.getHp() + "/" + p.getMaxHp() + " HP)", "success");
        if (mpRestored > 0)
            log("Used " + potion + ": Restored " + mpRestored + " MP (" + p.getMp() + "/" + p.getMaxMp() + " MP)", "success");
        if (hpRestored == 0 && mpRestored == 0)
            log("Used " + potion + ": No effect, already at full stats.", "danger");
    }

    // Combat
    public void logNotEnoughMp(String attack) { log("Not enough MP for " + attack + "!", "statLow"); }
    public void logSpellCast(String caster, String spell) { log(caster + " casts " + spell + "!"); }
    public void logSpellFizzle() { log("No targets in sight. The spell fizzles."); }
    public void logMeleeAttack(String attacker, String target, int dmg) { log(attacker + " attacks " + target + " for " + dmg + " damage!"); }
    public void logDamage(String target, int dmg, int hp, int maxHp) { log(target + " took " + dmg + " damage! (" + hp + "/" + maxHp + " HP)", "danger"); }
    public void logPlayerTakesDamage(String player, int dmg) { log(player + " takes " + dmg + " damage!", "danger"); }
    public void logEnemyDefeated(String enemy, int exp) { log(enemy + " has been defeated! You gained " + exp + " exp!", "accent"); }
    public void logPlayerDefeated(String player) { log(player + " has been defeated!", "deathRed"); }

    // Leveling
    public void logLevelUp(AbstractPlayer p) {
        log(p.getName() + " reached level " + p.getLevel() + "! (HP: " + p.getMaxHp() + ", MP: " + p.getMaxMp() + ")", "victoryGold");
    }

    public void logMultipleLevels(AbstractPlayer p, int levels) {
        log(p.getName() + " gained " + levels + " levels! Now level " + p.getLevel() + "!", "victoryGold");
    }

    public void logLevels(AbstractPlayer p, int levels) {
        if (levels == 1) logLevelUp(p);
        else if (levels > 1) logMultipleLevels(p, levels);
    }

    // Items
    public void logItemPickup(String item, int exp) { log("Picked up: " + item + " (+" + exp + " exp)", "accent"); }
    public void logWeaponUpgrade(String weapon, int dmgBonus, int exp) { log("Upgraded " + weapon + " (+" + dmgBonus + " dmg)! +" + exp + " exp", "accent"); }
    public void logWeaponDowngrade(String weapon, int exp) { log("You already have a better " + weapon + ". +" + exp + " exp", "accent"); }
    public void logInventoryFull() { log("Inventory is full!", "accent"); }
    public void logNothingHere() { log("You find nothing here..."); }
    public void logLegendaryItemFound(String item) { log("You found the legendary " + item + "!", "shardCyan"); }

    // Traps
    public void logTrapTriggered(String trigger, String damage) { log(trigger + " " + damage); }
    public void logTrapLethal(String trap, String player) { log("The " + trap + " was LETHAL! " + player + " has been defeated!", "deathRed"); }

    // Boss / Special
    public void logMedusaDefeated() { log("The Medusa of Chaos has been defeated! The evil presence lifts...", "victoryGold"); }
    public void logShardAppears() { log("A brilliant shard of light materializes where the beast fell!", "shardCyan"); }
    public void logShardAwaits() { log("The legendary Shard of Judgement awaits your claim!", "shardCyan"); }
    public void logShardSpawnFailed() { log("The Shard of Judgement failed to spawn, but you have still won!", "victoryGold"); }

    // System
    public void logGameRestartRequested() { log("Game restart requested - not yet implemented"); }
}
