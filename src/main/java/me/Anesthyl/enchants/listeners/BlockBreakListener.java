package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import me.Anesthyl.enchants.enchantsystem.XPBoostEnchant;
import me.Anesthyl.enchants.level.LevelManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
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
    private final LevelManager levelManager;

    public BlockBreakListener(EnchantManager enchantManager, LevelManager levelManager) {
        this.enchantManager = enchantManager;
        this.levelManager = levelManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Ignore cancelled events (other plugins, protections, etc.)
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (tool == null || tool.getType().isAir()) return;

        // Check for XP Boost helmet enchant
        double xpMultiplier = getXPBoostMultiplier(player);

        // Grant XP based on block type (with boost if applicable)
        if (blockType.toString().endsWith("_ORE") || blockType.toString().contains("ANCIENT_DEBRIS")) {
            // Mining skill - breaking ores
            levelManager.addMiningXP(player, xpMultiplier);
        } else if (blockType.toString().contains("STONE") || blockType.toString().contains("COBBLESTONE") || 
                   blockType.toString().contains("ANDESITE") || blockType.toString().contains("DIORITE") || 
                   blockType.toString().contains("GRANITE") || blockType == Material.NETHERRACK || 
                   blockType == Material.END_STONE) {
            // Mining skill - breaking stone
            int amount = (int) (5 * xpMultiplier); // Less XP than ores
            levelManager.addMiningXP(player, amount);
        } else if (blockType.toString().endsWith("_LOG") || blockType.toString().contains("WOOD") || 
                   blockType.toString().contains("STRIPPED")) {
            // Wood Cutting skill - chopping wood
            levelManager.addWoodCuttingXP(player, xpMultiplier);
        }

        // Fetch all custom enchants on the tool
        Map<CustomEnchant, Integer> enchants = enchantManager.getItemEnchants(tool);
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

            enchant.onBlockBreak(player, event.getBlock(), level);
        }
    }

    /**
     * Get XP multiplier from helmet enchant
     */
    private double getXPBoostMultiplier(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null) return 1.0;

        Map<CustomEnchant, Integer> enchants = enchantManager.getItemEnchants(helmet);
        for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet()) {
            if (entry.getKey() instanceof XPBoostEnchant xpBoost) {
                return xpBoost.getXPMultiplier(entry.getValue());
            }
        }
        return 1.0;
    }
}
