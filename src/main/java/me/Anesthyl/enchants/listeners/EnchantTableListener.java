package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import me.Anesthyl.enchants.util.EnchantUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

/**
 * Handles custom enchant application from enchanting tables.
 *
 * Dev Notes:
 * - Each enchant is checked for applicability on the item.
 * - Rarity and table level handled inside the enchant's getTableLevel().
 * - Level stacking with anvils works because we use PersistentDataContainer.
 */
public class EnchantTableListener implements Listener {

    private final EnchantManager enchantManager;

    public EnchantTableListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        for (CustomEnchant enchant : enchantManager.getEnchants()) {
            if (!enchant.canApply(event.getItem())) continue;

            int level = enchant.getTableLevel();
            if (level <= 0) continue; // failed rarity check

            EnchantUtil.applyEnchant(event.getItem(), enchant, level);
        }
    }
}
