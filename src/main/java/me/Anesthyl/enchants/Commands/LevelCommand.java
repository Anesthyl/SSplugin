package me.Anesthyl.enchants.Commands;

import me.Anesthyl.enchants.level.LevelManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command: /level
 * 
 * Shows player's current level, total XP, and progress to next level.
 */
public class LevelCommand implements CommandExecutor {

    private final LevelManager levelManager;

    public LevelCommand(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }

        // Show level info
        levelManager.showLevelInfo(player);
        return true;
    }
}
