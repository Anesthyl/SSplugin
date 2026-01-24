package me.Anesthyl.enchants.Commands;

import me.Anesthyl.enchants.backpack.BackpackManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Command: /backpack
 *
 * Gives the player a backpack item (OP only)
 */
public class BackpackCommand implements CommandExecutor {

    private final BackpackManager backpackManager;

    public BackpackCommand(BackpackManager backpackManager) {
        this.backpackManager = backpackManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.isOp()) {
            player.sendMessage("§cYou must be an operator to use this command.");
            return true;
        }

        ItemStack backpack = backpackManager.createBackpack();
        player.getInventory().addItem(backpack);
        player.sendMessage("§aYou received a backpack!");

        return true;
    }
}
