package me.Anesthyl.enchants.Commands;

import me.Anesthyl.enchants.guidebook.Guidebook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Command to give players the guidebook.
 */
public class GuidebookCommand implements CommandExecutor {
    private final Guidebook guidebook;

    public GuidebookCommand(JavaPlugin plugin) {
        this.guidebook = new Guidebook(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        // Give the guidebook
        ItemStack book = guidebook.createGuidebook();
        player.getInventory().addItem(book);

        player.sendMessage(
            Component.text("✦ ", NamedTextColor.AQUA)
                .append(Component.text("You have been given the ", NamedTextColor.GRAY))
                .append(Component.text("Legends of the Arcane", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(" guidebook!", NamedTextColor.GRAY))
        );

        return true;
    }
}
