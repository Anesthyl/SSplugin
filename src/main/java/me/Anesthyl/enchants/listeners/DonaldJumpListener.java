package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.DonaldJumpEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Listener for Donald Jump double-jump mechanic.
 * 
 * Enables flight when player is in the air with boots,
 * then triggers double-jump when they attempt to toggle flight.
 */
public class DonaldJumpListener implements Listener {

    private final EnchantManager enchantManager;

    public DonaldJumpListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    /**
     * Enable flight when player is in the air with Donald Jump boots.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Skip if in creative/spectator
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }
        
        // Check if player has Donald Jump boots
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null) {
            player.setAllowFlight(false);
            return;
        }
        
        Map<CustomEnchant, Integer> enchants = enchantManager.getItemEnchants(boots);
        boolean hasDonaldJump = enchants.keySet().stream()
                .anyMatch(e -> e instanceof DonaldJumpEnchant);
        
        if (hasDonaldJump) {
            // Enable flight only when in the air
            if (!player.isOnGround() && !player.isFlying()) {
                player.setAllowFlight(true);
            }
            // Disable when on ground
            else if (player.isOnGround()) {
                player.setAllowFlight(false);
            }
        } else {
            player.setAllowFlight(false);
        }
    }

    /**
     * Trigger double-jump when player tries to fly.
     */
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
