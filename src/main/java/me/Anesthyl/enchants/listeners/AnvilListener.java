package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import me.Anesthyl.enchants.util.EnchantUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        // Get enchants from both items
        Map<CustomEnchant, Integer> leftEnchants = enchantManager.getItemEnchants(left);
        Map<CustomEnchant, Integer> rightEnchants = enchantManager.getItemEnchants(right);
        
        // Skip if right item has no custom enchants
        if (rightEnchants.isEmpty()) return;

        // Clone left item to create a result
        ItemStack result = left.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return;

        // Combine all enchants from both items
        Map<CustomEnchant, Integer> combinedEnchants = new HashMap<>(leftEnchants);
        
        for (Map.Entry<CustomEnchant, Integer> entry : rightEnchants.entrySet()) {
            CustomEnchant enchant = entry.getKey();
            int rightLevel = entry.getValue();
            
            // Get existing level from left item (if any)
            int leftLevel = combinedEnchants.getOrDefault(enchant, 0);
            
            // Combine levels: if same enchant and same level, increase by 1 (like vanilla)
            // Otherwise, take the higher level
            int newLevel;
            if (leftLevel == rightLevel && leftLevel > 0) {
                // Same enchant, same level -> increase by 1 (vanilla behavior)
                newLevel = Math.min(enchant.getMaxLevel(), leftLevel + 1);
            } else {
                // Different levels -> take the higher one
                newLevel = Math.max(leftLevel, rightLevel);
            }
            combinedEnchants.put(enchant, newLevel);
        }

        // Clear existing custom enchant lore
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(line -> {
            for (CustomEnchant e : enchantManager.getEnchants()) {
                if (line.startsWith(e.getDisplayName())) {
                    return true;
                }
            }
            return false;
        });

        // Apply all combined enchants with updated lore
        for (Map.Entry<CustomEnchant, Integer> entry : combinedEnchants.entrySet()) {
            CustomEnchant enchant = entry.getKey();
            int level = entry.getValue();
            
            // Store in PDC
            meta.getPersistentDataContainer()
                    .set(enchant.getKey(), PersistentDataType.INTEGER, level);
            
            // Add to lore
            lore.add(enchant.getDisplayName() + " " + EnchantUtil.toRoman(level));
        }

        // Update result with new meta and lore
        meta.setLore(lore);
        result.setItemMeta(meta);
        event.setResult(result);
    }
}
