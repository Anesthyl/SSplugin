package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

/**
 * AnvilListener
 *
 * Handles custom enchantment combination via anvils.
 *
 * Behavior:
 * - Combines levels of the same custom enchant on left + right item.
 * - Caps level at enchant.getMaxLevel().
 * - Preserves all other enchants on the left item.
 * - Fully version-safe: uses PersistentDataContainer.
 *
 * Dev Notes:
 * - PrepareAnvilEvent is called whenever the anvil preview slot updates.
 * - `left` is the first item in the anvil (primary item), `right` is the second (sacrificial).
 * - Clone the left item to create a new result rather than modifying original ItemStack directly.
 * - PersistentDataContainer is used for custom enchant storage (no reliance on Bukkit Enchantment enum).
 * - This system allows future enchants to automatically inherit stacking behavior.
 */
public class AnvilListener implements Listener {

    private final EnchantManager enchantManager;

    public AnvilListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    @EventHandler
    public void onAnvilCombine(PrepareAnvilEvent event) {
        ItemStack left = event.getInventory().getItem(0);
        ItemStack right = event.getInventory().getItem(1);

        // Null check to prevent errors
        if (left == null || right == null) return;

        // Clone left item to create a result
        ItemStack result = left.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return;

        // Iterate over all custom enchants on the right item
        for (Map.Entry<CustomEnchant, Integer> entry : enchantManager.getItemEnchants(right).entrySet()) {
            CustomEnchant enchant = entry.getKey();
            int rightLevel = entry.getValue();

            // Check if left item already has this enchant
            Integer currentLevel = meta.getPersistentDataContainer()
                    .get(enchant.getKey(), PersistentDataType.INTEGER);

            int newLevel = rightLevel;

            if (currentLevel != null) {
                // Combine levels, capped at the max allowed level
                newLevel = Math.min(enchant.getMaxLevel(), currentLevel + rightLevel);
            }

            // Apply new level to the result item
            meta.getPersistentDataContainer()
                    .set(enchant.getKey(), PersistentDataType.INTEGER, newLevel);
        }

        // Update result with new meta and set it as the anvil output
        result.setItemMeta(meta);
        event.setResult(result);
    }
}
