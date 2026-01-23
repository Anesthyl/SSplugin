package me.Anesthyl.enchants.Commands;

import me.Anesthyl.enchants.stat.StatManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command: /stats
 * 
 * Shows player's current stat bonuses from enchants.
 */
public class StatsCommand implements CommandExecutor {

    private final StatManager statManager;

    public StatsCommand(StatManager statManager) {
        this.statManager = statManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        // Show stats info
        statManager.showStats(player);
        return true;
    }
}
