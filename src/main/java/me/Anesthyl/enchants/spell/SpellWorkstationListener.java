package me.Anesthyl.enchants.spell;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles lectern interactions for the spell workstation.
 */
public class SpellWorkstationListener implements Listener {
    private final JavaPlugin plugin;
    private final SpellManager spellManager;
    private final SpellGUI spellGUI;

    public SpellWorkstationListener(JavaPlugin plugin, SpellManager spellManager, SpellGUI spellGUI) {
        this.plugin = plugin;
        this.spellManager = spellManager;
        this.spellGUI = spellGUI;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLecternInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.LECTERN) return;

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Check if player is holding a spell book
        if (spellManager.isSpellBook(itemInHand)) {
            // Cancel the default placing behavior
            event.setCancelled(true);

            // Open spell GUI with the book in hand
            spellGUI.openGUI(player, itemInHand);
            player.sendMessage(Component.text("Opening Spell Workstation...")
                    .color(NamedTextColor.LIGHT_PURPLE));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        // Cleanup (removePlayer checks if player was in spell GUI)
        spellGUI.removePlayer(player);
    }
}
