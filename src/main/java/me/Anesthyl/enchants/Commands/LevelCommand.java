package me.Anesthyl.enchants.Commands;

import me.Anesthyl.enchants.level.LevelManager;
import me.Anesthyl.enchants.level.SkillType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command: /level [skill]
 * 
 * Shows player's current level, total XP, and progress.
 * Without arguments: shows all skills
 * With skill argument: shows detailed info for that skill
 */
public class LevelCommand implements CommandExecutor, TabCompleter {

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

        // If no arguments, show all skills
        if (args.length == 0) {
            levelManager.showLevelInfo(player);
            return true;
        }

        // If skill specified, show detailed info for that skill
        String skillName = String.join(" ", args);
        SkillType skill = SkillType.fromString(skillName);
        
        if (skill == null) {
            player.sendMessage("§cInvalid skill! Valid skills:");
            for (SkillType s : SkillType.values()) {
                player.sendMessage("  §7- §e" + s.getDisplayName());
            }
            return true;
        }
        
        levelManager.showSkillInfo(player, skill);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Return all skill names for tab completion
            return Arrays.stream(SkillType.values())
                .map(SkillType::getDisplayName)
                .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
