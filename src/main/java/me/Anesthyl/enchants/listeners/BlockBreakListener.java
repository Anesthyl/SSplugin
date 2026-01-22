package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
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
            entry.getKey().onBlockBreak(event.getPlayer(), event.getBlock(), entry.getValue());
        }
    }
}
