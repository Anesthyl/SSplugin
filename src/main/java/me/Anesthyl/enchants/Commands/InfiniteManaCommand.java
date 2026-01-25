package me.Anesthyl.enchants.Commands;

import me.Anesthyl.enchants.spell.ManaManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Command: /infinitemana [player]
 * 
 * Toggles infinite mana for a player.
 * When enabled, mana will not be consumed when casting spells.
 */
public class InfiniteManaCommand implements CommandExecutor, TabCompleter {

    private final ManaManager manaManager;
    private final Set<UUID> infiniteManaPlayers = new HashSet<>();

    public InfiniteManaCommand(ManaManager manaManager) {
        this.manaManager = manaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // If no arguments, toggle for self
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cYou must specify a player from console.");
                return true;
            }

            toggleInfiniteMana(player, sender);
            return true;
        }

        // If argument provided, toggle for target player
        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[0]);
                return true;
            }

            toggleInfiniteMana(target, sender);
            return true;
        }

        sender.sendMessage("§cUsage: /infinitemana [player]");
        return true;
    }

    /**
     * Toggles infinite mana for a player.
     */
    private void toggleInfiniteMana(Player target, CommandSender sender) {
        UUID uuid = target.getUniqueId();
        
        if (infiniteManaPlayers.contains(uuid)) {
            // Disable infinite mana
            infiniteManaPlayers.remove(uuid);
            target.sendMessage("§c§lINFINITE MANA DISABLED! §cYour mana will now be consumed normally.");
            
            if (!sender.equals(target)) {
                sender.sendMessage("§cDisabled infinite mana for " + target.getName());
            }
        } else {
            // Enable infinite mana
            infiniteManaPlayers.add(uuid);
            // Fill mana to max
            manaManager.setMana(target, manaManager.getPlayerMana(target).getMaxMana());
            target.sendMessage("§b§lINFINITE MANA ENABLED! §bYou now have unlimited mana.");
            
            if (!sender.equals(target)) {
                sender.sendMessage("§bEnabled infinite mana for " + target.getName());
            }
        }
    }

    /**
     * Checks if a player has infinite mana enabled.
     */
    public boolean hasInfiniteMana(Player player) {
        return infiniteManaPlayers.contains(player.getUniqueId());
    }

    /**
     * Removes a player from infinite mana tracking (called on quit).
     */
    public void removePlayer(Player player) {
        infiniteManaPlayers.remove(player.getUniqueId());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    players.add(player.getName());
                }
            }
            return players;
        }
        return new ArrayList<>();
    }
}
