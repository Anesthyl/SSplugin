package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import me.Anesthyl.enchants.enchantsystem.SmeltersDelightEnchant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class BlockBreakListener implements Listener {

    private final EnchantManager enchantManager;

    public BlockBreakListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
        Map<CustomEnchant, Integer> enchants = enchantManager.getItemEnchants(tool);

        for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet()) {
            CustomEnchant enchant = entry.getKey();
            int level = entry.getValue();

            if (enchant instanceof SmeltersDelightEnchant smelter) {
                // Only cancel normal drops if block is smeltable
                if (smelter.getSmeltedBlock(event.getBlock().getType()) != null) {
                    event.setDropItems(false); // Cancel normal drops
                    smelter.onBlockBreak(event.getPlayer(), event.getBlock(), level);
                }
            }
        }
    }
}
