package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.DonaldJumpEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Listener for Donald Jump double-jump mechanic.
 * 
 * Detects when players attempt to toggle flight (double-tap jump)
 * and triggers the double-jump if they have the enchant on boots.
 */
public class DonaldJumpListener implements Listener {

    private final EnchantManager enchantManager;

    public DonaldJumpListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        
        // Don't interfere with creative/spectator flight
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        
        // Check boots for Donald Jump enchant
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null) return;
        
        Map<CustomEnchant, Integer> enchants = enchantManager.getItemEnchants(boots);
        
        for (var entry : enchants.entrySet()) {
            if (entry.getKey() instanceof DonaldJumpEnchant donaldJump) {
                donaldJump.handleDoubleJump(event, player);
                return;
            }
        }
    }
}
