package me.Anesthyl.enchants.enchantsystem;

import me.Anesthyl.enchants.Enchants;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

/**
 * Donald Jump Enchant
 *
 * Dev Notes:
 * - Boots-only enchant.
 * - Grants double jump while in mid-air.
 * - Can be combined with other boots enchants.
 * - Table rarity: Level 30 only, 1% chance.
 * - Uses PlayerToggleFlightEvent to detect jump attempts.
 * - Modular: Works with other custom enchants without conflicts.
 * - Display name is rainbow-colored.
 */
public class DonaldJumpEnchant extends CustomEnchant {

    private static final Random RANDOM = new Random();
    private final Enchants plugin;

    // Rainbow letters: Red, Gold, Yellow, Green, Aqua, Blue, Light Purple
    private static final String RAINBOW_NAME = "§cD§6o§en§aa§bl§9d §dJ§cu§6m§ep";

    public DonaldJumpEnchant(JavaPlugin plugin) {
        super(plugin, "donald_jump", RAINBOW_NAME, 3);
        this.plugin = (Enchants) plugin;
    }

    @Override
    public boolean canApply(ItemStack item) {
        return item != null && item.getType().toString().endsWith("_BOOTS");
    }

    @Override
    public void onHit(Player player, org.bukkit.entity.LivingEntity target, int level) {
        // Not used
    }

    @Override
    public void onBlockBreak(Player player, org.bukkit.block.Block block, int level) {
        // Not used
    }

    @Override
    public boolean canAppearOnTable() {
        return true;
    }

    @Override
    public int getTableLevel() {
        return RANDOM.nextDouble() <= 0.01 ? 1 : 0; // 1% chance
    }

    @Override
    public void onTableEnchant(ItemStack item, int level) {
        if (item == null || !item.hasItemMeta() || level <= 0) return;

        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer()
                .set(getKey(), org.bukkit.persistence.PersistentDataType.INTEGER, level);
        item.setItemMeta(meta);
    }

    // ===============================
    // Double Jump Logic (Listener)
    // ===============================

    /**
     * Called when a player tries to toggle flight (jump).
     * Should be registered in a PlayerToggleFlightEvent listener.
     */
    public void handleDoubleJump(PlayerToggleFlightEvent event, Player player) {
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null) return;

        int level = plugin.getEnchantManager().getItemEnchants(boots)
                .getOrDefault(this, 0);
        if (level <= 0) return;

        event.setCancelled(true); // Cancel flight toggle
        player.setAllowFlight(false);

        // Launch player upward
        player.setVelocity(player.getVelocity().setY(1.0)); // Adjust Y to tune jump height

        // Optional: Add small forward boost
        // Vector forward = player.getLocation().getDirection().multiply(0.2);
        // player.setVelocity(player.getVelocity().add(forward));

        // Play particle or sound effect
        player.getWorld().playEffect(player.getLocation(), org.bukkit.Effect.MOBSPAWNER_FLAMES, 0);
    }
}
