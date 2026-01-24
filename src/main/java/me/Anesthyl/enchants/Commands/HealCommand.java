package me.Anesthyl.enchants.Commands;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command: /heal [player]
 *
 * Heals the player (or target player) to full health and saturation.
 */
public class HealCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // If no arguments, heal self
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cYou must specify a player to heal from console.");
                return true;
            }

            healPlayer(player);
            player.sendMessage("§a§lHEALED! §aYou have been healed to full health.");
            return true;
        }

        // If argument provided, heal target player
        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[0]);
                return true;
            }

            healPlayer(target);
            target.sendMessage("§a§lHEALED! §aYou have been healed to full health.");

            if (!sender.equals(target)) {
                sender.sendMessage("§aHealed " + target.getName() + " to full health.");
            }

            return true;
        }

        sender.sendMessage("§cUsage: /heal [player]");
        return true;
    }

    /**
     * Heals a player to max health and saturation
     */
    private void healPlayer(Player player) {
        // Get max health attribute
        double maxHealth = 20.0;
        if (player.getAttribute(Attribute.MAX_HEALTH) != null) {
            maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        }

        // Heal to max
        player.setHealth(maxHealth);

        // Fill hunger
        player.setFoodLevel(20);
        player.setSaturation(20.0f);

        // Clear fire
        player.setFireTicks(0);
    }
}
