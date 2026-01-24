package me.Anesthyl.enchants.util;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import me.Anesthyl.enchants.enchantsystem.ExcavatorEnchant;
import me.Anesthyl.enchants.enchantsystem.VeinMinerEnchant;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * EnchantUtil
 *
 * Dev Notes:
 * - Central utility for applying and displaying custom enchants.
 * - Enforces global compatibility rules (Vein Miner ↔ Excavator).
 * - Responsible for:
 *   - Writing enchant data to PDC
 *   - Updating item lore to reflect current enchants
 * - Used by:
 *   - Commands
 *   - Enchanting table
 *   - Anvils
 */
public class EnchantUtil {

    /**
     * Applies a custom enchant to an item while enforcing rules
     * and updating lore.
     */
    public static void applyEnchant(
            ItemStack item,
            CustomEnchant enchant,
            int level,
            EnchantManager enchantManager
    ) {
        if (item == null || enchant == null || level <= 0) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        Map<CustomEnchant, Integer> existing =
                enchantManager.getItemEnchants(item);

        /*
         * MUTUAL EXCLUSIVITY RULES
         */
        if (enchant instanceof VeinMinerEnchant) {
            for (CustomEnchant e : existing.keySet()) {
                if (e instanceof ExcavatorEnchant) return;
            }
        }

        if (enchant instanceof ExcavatorEnchant) {
            for (CustomEnchant e : existing.keySet()) {
                if (e instanceof VeinMinerEnchant) return;
            }
        }

        // Apply enchant level to PDC
        meta.getPersistentDataContainer()
                .set(enchant.getKey(), PersistentDataType.INTEGER, level);

        item.setItemMeta(meta);

        // Rebuild lore after applying enchant
        updateLore(item, enchantManager);
    }

    /**
     * Rebuilds custom enchant lore from PersistentDataContainer.
     *
     * Dev Notes:
     * - Removes all old custom-enchant lore lines
     * - Re-adds current enchant list cleanly
     * - Does NOT touch vanilla enchant lore
     */
    public static void updateLore(ItemStack item, EnchantManager enchantManager) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.hasLore()
                ? new ArrayList<>(meta.getLore())
                : new ArrayList<>();

        // Get current custom enchants
        Map<CustomEnchant, Integer> enchants = enchantManager.getItemEnchants(item);

        // Build list of formatted enchant lines to identify and remove
        List<String> enchantLinePatterns = new ArrayList<>();
        for (CustomEnchant enchant : enchantManager.getAllEnchants()) {
            // Match lines that start with the enchant's display name (including color codes)
            String displayPrefix = enchant.getDisplayName().split(" ")[0]; // Get first word with color
            enchantLinePatterns.add(displayPrefix);
        }

        // Remove only custom enchant lore lines by matching display name patterns
        lore.removeIf(line -> {
            for (String pattern : enchantLinePatterns) {
                if (line.startsWith(pattern)) {
                    return true;
                }
            }
            return false;
        });

        // Re-add current custom enchants
        for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet()) {
            CustomEnchant enchant = entry.getKey();
            int level = entry.getValue();

            lore.add(formatEnchantLine(enchant, level));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    /**
     * Formats enchant display text.
     *
     * Example:
     * §bVein Miner II
     */
    private static String formatEnchantLine(CustomEnchant enchant, int level) {
        String roman = toRoman(level);
        return enchant.getDisplayName() + " " + roman;
    }

    /**
     * Converts integers to Roman numerals (1–10 safe).
     */
    public static String toRoman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(number);
        };
    }
}
