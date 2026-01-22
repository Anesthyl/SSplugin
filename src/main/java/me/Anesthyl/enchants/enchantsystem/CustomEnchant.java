package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Base class for all custom enchants.
 *
 * Responsibilities:
 * 1. Store NamespacedKey for PDC storage.
 * 2. Define hooks for combat, block-break, and table application.
 * 3. Provide max level, rarity, and table eligibility.
 */
public abstract class CustomEnchant {

    private final NamespacedKey key;
    private final String displayName;
    private final int maxLevel;

    public CustomEnchant(JavaPlugin plugin, String key, String displayName, int maxLevel) {
        this.key = new NamespacedKey(plugin, key); // PDC-safe key
        this.displayName = displayName;
        this.maxLevel = maxLevel;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    // ------------------------------
    // Table Enchant Hooks
    // ------------------------------

    /** Can this enchant appear on the enchanting table? */
    public abstract boolean canAppearOnTable();

    /**
     * Returns the level the table should attempt to apply.
     * Can return 0 if rarity check fails.
     */
    public abstract int getTableLevel();

    /**
     * Called when the enchant is applied via an enchant table.
     */
    public abstract void onTableEnchant(ItemStack item, int level);

    // ------------------------------
    // Combat Hooks
    // ------------------------------

    /**
     * Called when a player hits a target.
     */
    public abstract void onHit(org.bukkit.entity.Player attacker,
                               org.bukkit.entity.LivingEntity target,
                               int level);

    // ------------------------------
    // Block Hooks
    // ------------------------------

    /**
     * Called when a player breaks a block.
     */
    public abstract void onBlockBreak(org.bukkit.entity.Player player,
                                      org.bukkit.block.Block block,
                                      int level);

    /**
     * Can this enchant be applied to this item?
     */
    public abstract boolean canApply(ItemStack item);
}
