package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.Random;

/**
 * Explosive Strike Enchant
 *
 * Dev Notes:
 * - Sword-only enchant.
 * - Causes a small knockback explosion on hit.
 * - Blast radius scales with level.
 * - Compatible with other custom enchants and vanilla mechanics.
 * - Table rarity: 15% chance, level random up to max.
 */
public class ExplosiveStrikeEnchant extends CustomEnchant {

    private static final Random RANDOM = new Random();

    public ExplosiveStrikeEnchant(JavaPlugin plugin) {
        super(plugin, "explosive_strike", "§cExplosive Strike", 3); // Max level 3
    }

    /**
     * Only swords can have this enchant.
     */
    @Override
    public boolean canApply(ItemStack item) {
        return item != null && item.getType().toString().endsWith("_SWORD");
    }

    /**
     * Triggered on hitting a LivingEntity.
     * Applies knockback to nearby entities in a small radius.
     *
     * @param attacker Player dealing the hit
     * @param target   Target entity
     * @param level    Enchant level
     */
    @Override
    public void onHit(Player attacker, LivingEntity target, int level) {
        if (attacker == null || target == null) return;

        Location loc = target.getLocation();
        double radius = 1.0 + (0.5 * level); // Scales with level: Level 1 = 1.5, Level 2 = 2.0, Level 3 = 2.5
        double damage = 1.0 + (level * 0.5); // Level 1 = 1.5, Level 2 = 2.0, Level 3 = 2.5 hearts

        // Apply knockback and damage to nearby entities
        for (LivingEntity entity : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)
                .stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .toList()) {

            // Skip the attacker (don't damage yourself)
            if (entity.equals(attacker)) continue;

            // Calculate knockback direction and strength
            Vector knockback = entity.getLocation().toVector()
                    .subtract(loc.toVector())
                    .normalize()
                    .multiply(0.5 * level); // Stronger knockback at higher levels

            entity.setVelocity(knockback);

            // Apply damage (scales with level)
            entity.damage(damage);
        }
    }

    /**
     * Not used for block breaking.
     */
    @Override
    public void onBlockBreak(Player player, org.bukkit.block.Block block, int level) {
        // Not applicable
    }

    /**
     * Can appear on the enchanting table.
     */
    @Override
    public boolean canAppearOnTable() {
        return true;
    }

    /**
     * Determines the level given by the table.
     * 15% chance to get a random level up to max.
     */
    @Override
    public int getTableLevel() {
        return RANDOM.nextDouble() <= 0.15 ? 1 + RANDOM.nextInt(getMaxLevel()) : 0;
    }

    /**
     * Apply the enchant to an item from the table.
     * Uses PersistentDataContainer for version-safe storage.
     *
     * @param item  Item to enchant
     * @param level Level to apply
     */
    @Override
    public void onTableEnchant(ItemStack item, int level) {
        if (item == null || !item.hasItemMeta() || level <= 0) return;

        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer()
                .set(getKey(), org.bukkit.persistence.PersistentDataType.INTEGER, level);
        item.setItemMeta(meta);
    }
}
