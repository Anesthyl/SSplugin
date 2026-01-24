package me.Anesthyl.enchants.enchantsystem;

import me.Anesthyl.enchants.Enchants;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

/**
 * Shiny Enchant
 *
 * Dev Notes:
 * - Can apply to any armor piece (boots, chestplate, helmet, leggings).
 * - Piglins treat wearer as gold armor: non-hostile.
 * - Table rarity: Level 30 only, 5% chance.
 * - Display name is rainbow-colored.
 * - Modular: Works with other custom enchants without interfering.
 */
public class ShinyEnchant extends CustomEnchant implements Listener {

    private static final Random RANDOM = new Random();
    private final Enchants plugin;

    // Rainbow letters: Gold, Yellow, Green, Aqua, Light Purple
    private static final String RAINBOW_NAME = "§6S§eh§ai§bn§dy";

    public ShinyEnchant(JavaPlugin plugin) {
        super(plugin, "shiny", RAINBOW_NAME, 1);
        this.plugin = (Enchants) plugin;
    }

    @Override
    public boolean canApply(ItemStack item) {
        if (item == null) return false;
        return item.getType().toString().endsWith("_BOOTS") ||
                item.getType().toString().endsWith("_LEGGINGS") ||
                item.getType().toString().endsWith("_CHESTPLATE") ||
                item.getType().toString().endsWith("_HELMET");
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
        return RANDOM.nextDouble() <= 0.05 ? 1 : 0; // 5% chance at level 30
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
    // Piglin Interaction
    // ===============================

    /**
     * Call this in a global listener to make Piglins ignore the player.
     */
    public void handlePiglin(EntityTargetLivingEntityEvent event) {
        if (!(event.getTarget() instanceof Player player)) return;

        ItemStack[] armor = player.getInventory().getArmorContents();
        boolean hasShiny = false;

        for (ItemStack piece : armor) {
            if (piece == null) continue;
            int level = plugin.getEnchantManager().getItemEnchants(piece)
                    .getOrDefault(this, 0);
            if (level > 0) {
                hasShiny = true;
                break;
            }
        }

        if (hasShiny) {
            // Piglin ignores the player
            event.setCancelled(true);
        }
    }
}
