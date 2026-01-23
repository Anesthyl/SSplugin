package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import me.Anesthyl.enchants.level.LevelManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class BlockBreakListener implements Listener {

    private final EnchantManager enchantManager;
    private final LevelManager levelManager;

    public BlockBreakListener(EnchantManager enchantManager, LevelManager levelManager) {
        this.enchantManager = enchantManager;
        this.levelManager = levelManager;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();
        ItemStack tool = player.getInventory().getItemInMainHand();
        
        // Grant XP based on block type
        if (blockType.toString().endsWith("_ORE") || blockType.toString().contains("ANCIENT_DEBRIS")) {
            levelManager.addMiningXP(player);
        } else if (blockType.toString().endsWith("_LOG") || blockType.toString().contains("WOOD")) {
            levelManager.addChoppingXP(player);
        }
        
        // Trigger custom enchants
        Map<CustomEnchant, Integer> enchants = enchantManager.getItemEnchants(tool);
        for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet()) {
            entry.getKey().onBlockBreak(player, event.getBlock(), entry.getValue());
        }
    }
}
