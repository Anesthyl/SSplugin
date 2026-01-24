package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.DonaldJumpEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener for Donald Jump multi-jump mechanic.
 *
 * Detects space bar presses while in air by checking velocity changes.
 * Level 1 = 1 extra jump, Level 2 = 2 extra jumps, Level 3 = 3 extra jumps
 */
public class DonaldJumpListener implements Listener {

    private final EnchantManager enchantManager;
    private final Map<UUID, Integer> jumpCounts = new HashMap<>();
    private final Map<UUID, Long> lastJumpTime = new HashMap<>();
    private final Map<UUID, Double> lastYVelocity = new HashMap<>();
    private final Map<UUID, Integer> airTicks = new HashMap<>();

    private static final long JUMP_COOLDOWN = 250; // 250ms cooldown to prevent spam
    private static final int MIN_AIR_TICKS = 3; // Must be in air for at least 3 ticks

    public DonaldJumpListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    /**
     * Detect jump attempts by velocity changes (no flight system needed).
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Skip if in creative/spectator
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

        // Reset jump count when on ground
        if (player.isOnGround()) {
            jumpCounts.remove(playerId);
            lastYVelocity.put(playerId, 0.0);
            airTicks.put(playerId, 0);
            return;
        }

        // Track air time
        int currentAirTicks = airTicks.getOrDefault(playerId, 0) + 1;
        airTicks.put(playerId, currentAirTicks);

        // Must be in air for minimum ticks to prevent ground jump detection
        if (currentAirTicks < MIN_AIR_TICKS) {
            lastYVelocity.put(playerId, player.getVelocity().getY());
            return;
        }

        // Player is in air - detect jump attempt by velocity spike
        double currentY = player.getVelocity().getY();
        double previousY = lastYVelocity.getOrDefault(playerId, 0.0);

        // Improved jump detection:
        // 1. Must have upward velocity
        // 2. Previous velocity must be negative (falling)
        // 3. Significant positive change in velocity
        // 4. Not affected by levitation or slow falling
        boolean hasLevitation = player.hasPotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION);
        boolean hasSlowFalling = player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING);

        boolean jumpAttempt = !hasLevitation && !hasSlowFalling
                && currentY > 0.15
                && previousY < -0.05
                && (currentY - previousY) > 0.3;

        lastYVelocity.put(playerId, currentY);

        if (!jumpAttempt) return;

        // Check cooldown to prevent spam
        long now = System.currentTimeMillis();
        long lastJump = lastJumpTime.getOrDefault(playerId, 0L);
        if (now - lastJump < JUMP_COOLDOWN) return;

        // Check jump count
        int currentJumps = jumpCounts.getOrDefault(playerId, 0);
        int maxJumps = donaldJumpLevel;

        if (currentJumps >= maxJumps) return;

        // Perform air jump
        lastJumpTime.put(playerId, now);
        jumpCounts.put(playerId, currentJumps + 1);

        // Apply jump boost with scaling based on level
        Vector velocity = player.getVelocity();

        // Base jump strength - consistent upward boost
        double jumpStrength = 0.55;
        velocity.setY(jumpStrength);

        // Add forward momentum based on movement direction
        Vector direction = player.getLocation().getDirection();
        Vector horizontalDirection = direction.clone().setY(0).normalize();

        // Only add momentum if player is moving forward
        if (player.getVelocity().length() > 0.1) {
            velocity.add(horizontalDirection.multiply(0.25));
        }

        player.setVelocity(velocity);

        // Play sound effect with pitch variation based on jump count
        float pitch = 1.5f + (currentJumps * 0.2f);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, pitch);
    }
}
