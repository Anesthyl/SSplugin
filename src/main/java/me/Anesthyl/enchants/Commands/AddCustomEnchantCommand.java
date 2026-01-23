package me.Anesthyl.enchants.Commands;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import me.Anesthyl.enchants.util.EnchantUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

/**
 * Command: /addcustomenchant <enchant> [level]
 *
 * Dev Notes:
 * - Applies any registered custom enchant to the item in-hand.
 * - Optional level parameter; defaults to 1.
 * - Only usable by players.
 */
public class AddCustomEnchantCommand implements CommandExecutor {

    private final EnchantManager enchantManager;

    public AddCustomEnchantCommand(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.RED + "Usage: /addcustomenchant <enchant> [level]");
            return true;
        }

        String enchantName = args[0].toLowerCase(Locale.ROOT);
        int level = 1;

        if (args.length >= 2) {
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Invalid level, must be a number.");
                return true;
            }
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must be holding an item in your main hand.");
            return true;
        }

        CustomEnchant enchant = enchantManager.getEnchants().stream()
                .filter(e -> e.getKey().getKey().equalsIgnoreCase(enchantName))
                .findFirst().orElse(null);

        if (enchant == null) {
            player.sendMessage(ChatColor.RED + "Enchant not found: " + enchantName);
            return true;
        }

        if (!enchant.canApply(item)) {
            player.sendMessage(ChatColor.RED + "This enchant cannot be applied to this item.");
            return true;
        }

        EnchantUtil.applyEnchant(item, enchant, level);
        player.sendMessage(ChatColor.GREEN + "Applied " + enchant.getDisplayName() + " " + level + " to your item!");
        return true;
    }
}
