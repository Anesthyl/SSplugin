package me.Anesthyl.enchants.Commands;

import me.Anesthyl.enchants.warp.WarpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Command to delete a personal warp.
 */
public class DelWarpCommand implements CommandExecutor, TabCompleter {
    private final WarpManager warpManager;

    public DelWarpCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(
                Component.text("Usage: /delwarp <name>", NamedTextColor.RED)
            );
            return true;
        }

        String warpName = args[0];
        boolean success = warpManager.deleteWarp(player.getUniqueId(), warpName);

        if (success) {
            player.sendMessage(
                Component.text("✦ ", NamedTextColor.AQUA)
                    .append(Component.text("Warp ", NamedTextColor.GRAY))
                    .append(Component.text(warpName, NamedTextColor.GOLD))
                    .append(Component.text(" deleted!", NamedTextColor.GRAY))
            );
        } else {
            player.sendMessage(
                Component.text("Warp '", NamedTextColor.RED)
                    .append(Component.text(warpName, NamedTextColor.GOLD))
                    .append(Component.text("' not found!", NamedTextColor.RED))
            );
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            // Tab complete with the player's warp names
            Set<String> warps = warpManager.getPlayerWarps(player.getUniqueId());
            List<String> completions = new ArrayList<>(warps);

            // Filter by what the player has typed
            String partial = args[0].toLowerCase();
            completions.removeIf(warp -> !warp.toLowerCase().startsWith(partial));

            return completions;
        }

        return new ArrayList<>();
    }
}
