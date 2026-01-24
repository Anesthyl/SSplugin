package me.Anesthyl.enchants.guidebook;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Gives new players the legendary guidebook on first join.
 */
public class GuidebookListener implements Listener {
    private final JavaPlugin plugin;
    private final Guidebook guidebook;

    public GuidebookListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.guidebook = new Guidebook(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if player has joined before
        if (!player.hasPlayedBefore()) {
            // Give the player the guidebook
            ItemStack book = guidebook.createGuidebook();
            player.getInventory().addItem(book);

            // Send welcome message
            player.sendMessage(Component.empty());
            player.sendMessage(
                Component.text("═══════════════════════════════", NamedTextColor.DARK_PURPLE)
            );
            player.sendMessage(
                Component.text("  Welcome to the ", NamedTextColor.GRAY)
                    .append(Component.text("Ancient Order", NamedTextColor.GOLD))
            );
            player.sendMessage(
                Component.text("  You have been gifted the ", NamedTextColor.GRAY)
                    .append(Component.text("Legends of the Arcane", NamedTextColor.LIGHT_PURPLE))
            );
            player.sendMessage(
                Component.text("  Read it to discover legendary enchantments and spells!", NamedTextColor.GRAY)
            );
            player.sendMessage(
                Component.text("═══════════════════════════════", NamedTextColor.DARK_PURPLE)
            );
            player.sendMessage(Component.empty());
        }
    }
}
