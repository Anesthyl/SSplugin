package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * Explosive Strike Enchant
 *
 * - Causes a small explosion effect when hitting an entity.
 * - Sword only.
 * - Table rarity: 15%.
 * - Level scales blast radius.
 */
public class ExplosiveStrikeEnchant extends CustomEnchant {

    private static final Random RANDOM = new Random();

    public ExplosiveStrikeEnchant(JavaPlugin plugin) {
        super(plugin, "explosive_strike", "§cExplosive Strike", 3);
    }

    @Override
    public boolean canApply(ItemStack item) {
        return item != null && item.getType().toString().endsWith("_SWORD");
    }

    @Override
    public void onHit(Player attacker, Entity target, int level) {
        Location loc = target.getLocation();
        double radius = 1.0 + (0.5 * level);

        // Simple knockback explosion (no terrain damage)
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
            if (entity instanceof Player || entity instanceof org.bukkit.entity.LivingEntity) {
                Vector knockback = entity.getLocation().toVector().subtract(loc.toVector()).normalize().multiply(0.5 * level);
                entity.setVelocity(knockback);
            }
        }
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
        return RANDOM.nextDouble() <= 0.15 ? 1 + RANDOM.nextInt(getMaxLevel()) : 0;
    }

    @Override
    public void onTableEnchant(ItemStack item, int level) {
        item.getItemMeta().getPersistentDataContainer()
                .set(getKey(), org.bukkit.persistence.PersistentDataType.INTEGER, level);
    }
}
