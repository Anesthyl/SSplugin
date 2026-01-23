package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import me.Anesthyl.enchants.util.EnchantUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * AnvilListener
 *
 * Dev Notes:
 * - Mimics vanilla enchant combination rules for custom enchants.
 * - I + I → II
 * - I + II → II
 * - II + II → III
 * - Caps at enchant.getMaxLevel().
 * - Respects canApply() to prevent illegal transfers.
 * - Enforces global compatibility rules via EnchantUtil.
 * - Preserves all vanilla enchants and unrelated custom enchants.
 * - Uses PrepareAnvilEvent (preview-safe, non-destructive).
 */
public class AnvilListener implements Listener {

    private final EnchantManager enchantManager;

    public AnvilListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack left = event.getInventory().getItem(0);
        ItemStack right = event.getInventory().getItem(1);

        if (left == null || right == null) return;

        // Clone left item as the base result
        ItemStack result = left.clone();

        Map<CustomEnchant, Integer> leftEnchants =
                enchantManager.getItemEnchants(left);
        Map<CustomEnchant, Integer> rightEnchants =
                enchantManager.getItemEnchants(right);

        boolean changed = false;

        for (Map.Entry<CustomEnchant, Integer> entry : rightEnchants.entrySet()) {
            CustomEnchant enchant = entry.getKey();
            int rightLevel = entry.getValue();

            // Ensure enchant is valid for this item
            if (!enchant.canApply(result)) continue;

            int leftLevel = leftEnchants.getOrDefault(enchant, 0);
            int newLevel;

            /*
             * VANILLA COMBINATION LOGIC
             *
             * Same level → +1
             * Different levels → higher wins
             */
            if (leftLevel == rightLevel) {
                newLevel = leftLevel + 1;
            } else {
                newLevel = Math.max(leftLevel, rightLevel);
            }

            // Cap at enchant max level
            newLevel = Math.min(newLevel, enchant.getMaxLevel());

            // Only apply if this is an upgrade
            if (newLevel > leftLevel) {
                EnchantUtil.applyEnchant(
                        result,
                        enchant,
                        newLevel,
                        enchantManager
                );
                changed = true;
            }
        }

        // Only set output if something actually changed
        if (changed) {
            event.setResult(result);
        }
    }
}
