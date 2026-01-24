package me.Anesthyl.enchants.Commands;

import me.Anesthyl.enchants.warp.WarpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Command to teleport to a personal warp.
 */
public class WarpCommand implements CommandExecutor, TabCompleter {
    private final WarpManager warpManager;

    public WarpCommand(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length == 0) {
            // List all warps for the player
            Set<String> warps = warpManager.getPlayerWarps(player.getUniqueId());

            if (warps.isEmpty()) {
                player.sendMessage(
                    Component.text("You don't have any warps set. Use ", NamedTextColor.GRAY)
                        .append(Component.text("/setwarp <name>", NamedTextColor.GOLD))
                        .append(Component.text(" to create one.", NamedTextColor.GRAY))
                );
            } else {
                player.sendMessage(
                    Component.text("═══ Your Warps ═══", NamedTextColor.AQUA)
                );
                for (String warp : warps) {
                    player.sendMessage(
                        Component.text("  • ", NamedTextColor.GRAY)
                            .append(Component.text(warp, NamedTextColor.GOLD))
                    );
                }
                player.sendMessage(
                    Component.text("Use ", NamedTextColor.GRAY)
                        .append(Component.text("/warp <name>", NamedTextColor.GOLD))
                        .append(Component.text(" to teleport", NamedTextColor.GRAY))
                );
            }
            return true;
        }

        String warpName = args[0];
        Location warpLocation = warpManager.getWarp(player.getUniqueId(), warpName);

        if (warpLocation == null) {
            player.sendMessage(
                Component.text("Warp '", NamedTextColor.RED)
                    .append(Component.text(warpName, NamedTextColor.GOLD))
                    .append(Component.text("' not found!", NamedTextColor.RED))
            );
            return true;
        }

        // Teleport the player
        player.teleport(warpLocation);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        player.sendMessage(
            Component.text("✦ ", NamedTextColor.AQUA)
                .append(Component.text("Warped to ", NamedTextColor.GRAY))
                .append(Component.text(warpName, NamedTextColor.GOLD))
                .append(Component.text("!", NamedTextColor.GRAY))
        );

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
