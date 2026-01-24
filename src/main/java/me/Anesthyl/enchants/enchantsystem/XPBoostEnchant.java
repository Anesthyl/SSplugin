package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

/**
 * XP Boost Enchant
 *
 * Dev Notes:
 * - Helmet-only enchant.
 * - Increases XP gained from all sources.
 * - Level 1 = +25% XP, Level 2 = +50% XP, Level 3 = +100% XP
 * - Table rarity: 10% chance.
 * - LEGENDARY rarity
 */
public class XPBoostEnchant extends CustomEnchant {

    private static final Random RANDOM = new Random();

    public XPBoostEnchant(JavaPlugin plugin) {
        super(plugin, "xp_boost", "§dXP Boost", 3);
    }

    @Override
    public boolean canApply(ItemStack item) {
        return item != null && item.getType().toString().endsWith("_HELMET");
    }

    @Override
    public void onHit(Player player, org.bukkit.entity.LivingEntity target, int level) {
        // Not used - XP boost is passive
    }

    @Override
    public void onBlockBreak(Player player, org.bukkit.block.Block block, int level) {
        // Not used - XP boost is passive
    }

    @Override
    public boolean canAppearOnTable() {
        return true;
    }

    @Override
    public int getTableLevel() {
        // 10% chance, random level 1-3
        return RANDOM.nextDouble() <= 0.10 ? 1 + RANDOM.nextInt(getMaxLevel()) : 0;
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

    /**
     * Calculate XP multiplier based on level
     */
    public double getXPMultiplier(int level) {
        return switch (level) {
            case 1 -> 1.25; // +25%
            case 2 -> 1.50; // +50%
            case 3 -> 2.00; // +100%
            default -> 1.0;
        };
    }
}
