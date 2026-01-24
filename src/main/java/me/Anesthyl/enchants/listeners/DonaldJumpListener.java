package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.DonaldJumpEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener for Donald Jump multi-jump mechanic.
 *
 * Uses flight detection to register air jumps (spacebar double-tap).
 * Level 1 = 1 extra jump, Level 2 = 2 extra jumps, Level 3 = 3 extra jumps
 */
public class DonaldJumpListener implements Listener {

    private final EnchantManager enchantManager;
    private final Map<UUID, Integer> jumpCounts = new HashMap<>();

    public DonaldJumpListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    /**
     * Enable flight when player is in air with Donald Jump boots.
     * This allows detection of spacebar double-tap.
     */
    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

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

        // Find Donald Jump enchant and get level
        int donaldJumpLevel = 0;
        for (var entry : enchants.entrySet()) {
            if (entry.getKey() instanceof DonaldJumpEnchant) {
                donaldJumpLevel = entry.getValue();
                break;
            }
        }

        if (donaldJumpLevel == 0) {
            player.setAllowFlight(false);
            jumpCounts.remove(playerId);
            return;
        }

        // Reset jump count when on ground
        if (player.isOnGround()) {
            jumpCounts.remove(playerId);
            player.setAllowFlight(false);
            return;
        }

        // Enable flight in air (allows double-tap spacebar detection)
        int currentJumps = jumpCounts.getOrDefault(playerId, 0);
        int maxJumps = donaldJumpLevel;

        // Only allow flight if jumps remaining
        if (currentJumps < maxJumps) {
            player.setAllowFlight(true);
        } else {
            player.setAllowFlight(false);
        }
    }

    /**
     * Detect double-tap spacebar (flight toggle) and perform air jump.
     */
    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Only handle survival/adventure mode
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        // Check if player has Donald Jump boots
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null) return;

        Map<CustomEnchant, Integer> enchants = enchantManager.getItemEnchants(boots);

        // Find Donald Jump enchant and get level
        int donaldJumpLevel = 0;
        for (var entry : enchants.entrySet()) {
            if (entry.getKey() instanceof DonaldJumpEnchant) {
                donaldJumpLevel = entry.getValue();
                break;
            }
        }

        if (donaldJumpLevel == 0) return;

        // Must be in air
        if (player.isOnGround()) return;

        // Cancel actual flight
        event.setCancelled(true);

        // Check jump count
        int currentJumps = jumpCounts.getOrDefault(playerId, 0);
        int maxJumps = donaldJumpLevel;

        if (currentJumps >= maxJumps) return;

        // Perform air jump
        jumpCounts.put(playerId, currentJumps + 1);

        // Apply jump boost
        Vector velocity = player.getVelocity();
        velocity.setY(0.6); // Strong upward boost

        // Add forward momentum in look direction
        Vector direction = player.getLocation().getDirection();
        Vector horizontalMomentum = direction.clone().setY(0).normalize().multiply(0.2);
        velocity.add(horizontalMomentum);

        player.setVelocity(velocity);

        // Disable flight if max jumps reached
        if (currentJumps + 1 >= maxJumps) {
            player.setAllowFlight(false);
        }

        // Play sound effect with pitch variation
        float pitch = 1.5f + (currentJumps * 0.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, pitch);
    }
}
