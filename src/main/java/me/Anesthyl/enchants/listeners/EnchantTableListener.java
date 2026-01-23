package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import me.Anesthyl.enchants.util.EnchantUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for enchanting table events.
 *
 * Dev Notes:
 * - This listener handles applying custom enchants to items when using an enchanting table.
 * - Supports both single-level and multi-level enchants.
 * - Honors each enchant's rarity logic via getTableLevel().
 * - Prevents overwriting other custom enchants on the same item.
 * - Automatically stores the level in PersistentDataContainer and updates lore via EnchantUtil.
 * - Ready for future enchants without modification.
 */
public class EnchantTableListener implements Listener {

    private final EnchantManager enchantManager;

    public EnchantTableListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        ItemStack item = event.getItem();

        for (CustomEnchant enchant : enchantManager.getEnchants()) {

            // Skip if the enchant cannot be applied to this item type
            if (!enchant.canApply(item)) continue;

            // Determine the level to apply using the enchant's own table logic
            int level = enchant.getTableLevel();

            // Skip if the enchant does not appear this time
            if (level <= 0) continue;

            // Apply the enchant to the item without overwriting other enchants
            EnchantUtil.applyEnchant(item, enchant, level);
        }
    }
}
