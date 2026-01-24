package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import me.Anesthyl.enchants.enchantsystem.LavaWalkerEnchant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Listener for Lava Walker effects.
 *
 * Dev Notes:
 * - Detects player movement.
 * - Calls LavaWalkerEnchant.onPlayerMove() if boots have the enchant.
 * - Works for multiple players simultaneously.
 */
public class LavaWalkerListener implements Listener {

    private final EnchantManager enchantManager;

    public LavaWalkerListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Null check for getTo() in edge cases
        if (event.getTo() == null) return;

        // Only trigger on actual block movement, not head turns
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        ItemStack boots = event.getPlayer().getInventory().getBoots();
        if (boots == null) return;

        // Get all custom enchants on boots
        Map<me.Anesthyl.enchants.enchantsystem.CustomEnchant, Integer> enchants =
                enchantManager.getItemEnchants(boots);

        for (var entry : enchants.entrySet()) {
            if (entry.getKey() instanceof LavaWalkerEnchant lavaWalker) {
                lavaWalker.onPlayerMove(event, entry.getValue());
            }
        }
    }
}