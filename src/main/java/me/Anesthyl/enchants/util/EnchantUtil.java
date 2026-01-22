package me.Anesthyl.enchants.util;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * EnchantUtil
 *
 * Utility class for handling custom enchantments.
 *
 * Dev Notes:
 * - Stores enchant levels in PersistentDataContainer (version-safe).
 * - Updates item lore to reflect current enchant levels.
 * - Handles anvil combining by replacing existing levels of the same enchant.
 * - Preserves other custom enchants and vanilla enchantments.
 */
public class EnchantUtil {

    /**
     * Apply a custom enchant to an item.
     *
     * - If the item already has the enchant, it replaces the level (useful for anvil stacking).
     * - Updates lore to show the enchant name and Roman numeral level.
     *
     * @param item    ItemStack to apply enchant to
     * @param enchant CustomEnchant to apply
     * @param level   Level of enchant
     */
    public static void applyEnchant(ItemStack item, CustomEnchant enchant, int level) {
        if (item == null || enchant == null || level <= 0) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // PersistentDataContainer: store the level of this enchant
        meta.getPersistentDataContainer().set(enchant.getKey(), PersistentDataType.INTEGER, level);

        // Update lore safely
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        boolean replaced = false;

        // Check if this enchant already exists in lore
        for (int i = 0; i < lore.size(); i++) {
            String line = lore.get(i);
            if (line.startsWith(enchant.getDisplayName())) {
                lore.set(i, enchant.getDisplayName() + " " + toRoman(level));
                replaced = true;
                break;
            }
        }

        // If not already in lore, add it
        if (!replaced) {
            lore.add(enchant.getDisplayName() + " " + toRoman(level));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Get the level of a specific custom enchant on an item.
     *
     * @param item    ItemStack to check
     * @param enchant CustomEnchant to check
     * @return Level, or 0 if not present
     */
    public static int getEnchantLevel(ItemStack item, CustomEnchant enchant) {
        if (item == null || enchant == null || !item.hasItemMeta()) return 0;

        Integer level = item.getItemMeta().getPersistentDataContainer()
                .get(enchant.getKey(), PersistentDataType.INTEGER);
        return level == null ? 0 : level;
    }

    /**
     * Check if an item has a specific custom enchant.
     *
     * @param item    ItemStack to check
     * @param enchant CustomEnchant to check
     * @return true if present
     */
    public static boolean hasEnchant(ItemStack item, CustomEnchant enchant) {
        return getEnchantLevel(item, enchant) > 0;
    }

    /**
     * Remove a custom enchant from an item.
     * Updates lore and PersistentDataContainer.
     *
     * @param item    ItemStack to modify
     * @param enchant CustomEnchant to remove
     */
    public static void removeEnchant(ItemStack item, CustomEnchant enchant) {
        if (item == null || enchant == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer().remove(enchant.getKey());

        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>(meta.getLore());
            lore.removeIf(line -> line.startsWith(enchant.getDisplayName()));
            meta.setLore(lore);
        }

        item.setItemMeta(meta);
    }

    /**
     * Get all custom enchants and their levels from an item.
     *
     * @param item                ItemStack to check
     * @param registeredEnchants  Map of all registered enchants (from EnchantManager)
     * @return Map of CustomEnchant -> level
     */
    public static Map<CustomEnchant, Integer> getEnchants(ItemStack item, Map<String, CustomEnchant> registeredEnchants) {
        Map<CustomEnchant, Integer> found = new java.util.HashMap<>();
        if (item == null || !item.hasItemMeta()) return found;

        ItemMeta meta = item.getItemMeta();
        registeredEnchants.forEach((key, enchant) -> {
            Integer level = meta.getPersistentDataContainer().get(enchant.getKey(), PersistentDataType.INTEGER);
            if (level != null) {
                found.put(enchant, level);
            }
        });

        return found;
    }

    /**
     * Converts an integer level to a Roman numeral for lore display.
     * Supports levels I-V, defaults to numeric value beyond V.
     */
    private static String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(number);
        };
    }
}
