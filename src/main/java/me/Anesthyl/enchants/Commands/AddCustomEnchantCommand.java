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
 * /addcustomenchant <enchant> [level]
 *
 * Dev Notes:
 * - OP-only command.
 * - Applies any registered custom enchant.
 * - Level defaults to 1 and is capped at enchant max.
 * - All compatibility rules are enforced via EnchantUtil.
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

        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "You must be an operator to use this command.");
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
                player.sendMessage(ChatColor.RED + "Level must be a number.");
                return true;
            }
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must be holding an item.");
            return true;
        }

        CustomEnchant enchant = enchantManager.getEnchant(enchantName).orElse(null);
        if (enchant == null) {
            player.sendMessage(ChatColor.RED + "Unknown enchant: " + enchantName);
            return true;
        }

        if (!enchant.canApply(item)) {
            player.sendMessage(ChatColor.RED + "That enchant cannot be applied to this item.");
            return true;
        }

        // Cap level safely
        level = Math.min(level, enchant.getMaxLevel());

        EnchantUtil.applyEnchant(
                item,
                enchant,
                level,
                enchantManager
        );

        player.sendMessage(ChatColor.GREEN + "Applied "
                + enchant.getDisplayName() + " "
                + level + "!");
        return true;
    }
}
