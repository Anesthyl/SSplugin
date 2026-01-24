package me.Anesthyl.enchants.Commands;

import me.Anesthyl.enchants.warp.WarpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Command to set a personal warp at the player's current location.
 */
public class SetWarpCommand implements CommandExecutor, TabCompleter {
    private final WarpManager warpManager;

    public SetWarpCommand(WarpManager warpManager) {
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
                Component.text("Usage: /setwarp <name>", NamedTextColor.RED)
            );
            return true;
        }

        String warpName = args[0];

        // Validate warp name (alphanumeric and underscores only)
        if (!warpName.matches("[a-zA-Z0-9_]+")) {
            player.sendMessage(
                Component.text("Warp name can only contain letters, numbers, and underscores!", NamedTextColor.RED)
            );
            return true;
        }

        Location location = player.getLocation();
        warpManager.setWarp(player.getUniqueId(), warpName, location);

        player.sendMessage(
            Component.text("✦ ", NamedTextColor.AQUA)
                .append(Component.text("Warp ", NamedTextColor.GRAY))
                .append(Component.text(warpName, NamedTextColor.GOLD))
                .append(Component.text(" set at your current location!", NamedTextColor.GRAY))
        );

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // No tab completion for warp names (they're custom)
        return new ArrayList<>();
    }
}
