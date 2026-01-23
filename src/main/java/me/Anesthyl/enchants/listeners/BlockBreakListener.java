package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * BlockBreakListener
 *
 * Dev Notes:
 * - Central dispatcher for all custom enchant block-break behavior.
 * - DOES NOT cancel the BlockBreakEvent globally.
 * - Each enchant is responsible for:
 *   - Deciding whether it applies
 *   - Cancelling vanilla drops if necessary (block.setDropItems(false))
 *   - Handling its own drop logic
 *
 * Design Philosophy:
 * - Avoids enchant conflicts by not hardcoding logic here.
 * - Allows multiple enchants to react to the same break safely.
 * - Mirrors vanilla flow: block breaks → enchants react.
 */
public class BlockBreakListener implements Listener {

    private final EnchantManager enchantManager;

    public BlockBreakListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Ignore cancelled events (other plugins, protections, etc.)
        if (event.isCancelled()) return;

        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        if (tool == null || tool.getType().isAir()) return;

        // Fetch all custom enchants on the tool
        Map<CustomEnchant, Integer> enchants =
                enchantManager.getItemEnchants(tool);

        if (enchants.isEmpty()) return;

        /*
         * IMPORTANT:
         * We do NOT cancel the event here.
         *
         * Each enchant decides:
         * - Whether to suppress vanilla drops
         * - Whether to replace drops
         * - Whether to modify the world
         */
        for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet()) {
            CustomEnchant enchant = entry.getKey();
            int level = entry.getValue();

            enchant.onBlockBreak(
                    event.getPlayer(),
                    event.getBlock(),
                    level
            );
        }
    }
}
