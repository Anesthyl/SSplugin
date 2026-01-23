package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import me.Anesthyl.enchants.enchantsystem.ShinyEnchant;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Listener for Shiny enchant piglin interaction.
 * 
 * Makes piglins ignore players wearing armor with the Shiny enchant.
 */
public class ShinyListener implements Listener {

    private final EnchantManager enchantManager;

    public ShinyListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    @EventHandler
    public void onPiglinTarget(EntityTargetLivingEntityEvent event) {
        // Only handle piglin targeting
        if (event.getEntity().getType() != EntityType.PIGLIN && 
            event.getEntity().getType() != EntityType.PIGLIN_BRUTE) {
            return;
        }
        
        // Only handle targeting players
        if (!(event.getTarget() instanceof Player player)) {
            return;
        }
        
        // Check all armor pieces for Shiny enchant
        ItemStack[] armor = player.getInventory().getArmorContents();
        boolean hasShiny = false;
        
        for (ItemStack piece : armor) {
            if (piece == null) continue;
            
            Map<CustomEnchant, Integer> enchants = enchantManager.getItemEnchants(piece);
            for (var entry : enchants.entrySet()) {
                if (entry.getKey() instanceof ShinyEnchant shiny && entry.getValue() > 0) {
                    shiny.handlePiglin(event);
                    hasShiny = true;
                    break;
                }
            }
            
            if (hasShiny) break;
        }
    }
}
