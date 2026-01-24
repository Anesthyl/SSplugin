package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

/**
 * Manages all custom enchants.
 * Responsibilities:
 * - Register and track enchants.
 * - Query item enchants.
 * - Apply table enchants.
 * - Helper methods for listeners.
 */
public class EnchantManager {

    private final Map<String, CustomEnchant> enchants = new HashMap<>();

    // ------------------------------
    // Registration
    // ------------------------------

    public void registerEnchant(CustomEnchant enchant) {
        enchants.put(enchant.getKey().getKey(), enchant); // store by string key
    }

    public Collection<CustomEnchant> getEnchants() {
        return enchants.values();
    }

    public Collection<CustomEnchant> getAllEnchants() {
        return enchants.values();
    }

    public Optional<CustomEnchant> getEnchant(String key) {
        return Optional.ofNullable(enchants.get(key));
    }

    // ------------------------------
    // Item Queries
    // ------------------------------

    public Map<CustomEnchant, Integer> getItemEnchants(ItemStack item) {
        Map<CustomEnchant, Integer> found = new HashMap<>();
        if (item == null || !item.hasItemMeta()) return found;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();

        for (CustomEnchant enchant : enchants.values()) {
            Integer level = pdc.get(enchant.getKey(), PersistentDataType.INTEGER);
            if (level != null && level > 0) {
                found.put(enchant, level);
            }
        }
        return found;
    }

    public boolean hasEnchant(ItemStack item, CustomEnchant enchant) {
        return getItemEnchants(item).containsKey(enchant);
    }

    public int getEnchantLevel(ItemStack item, CustomEnchant enchant) {
        return getItemEnchants(item).getOrDefault(enchant, 0);
    }

    // ------------------------------
    // Table Application
    // ------------------------------

    public void applyTableEnchants(ItemStack item) {
        for (CustomEnchant enchant : enchants.values()) {
            if (!enchant.canApply(item)) continue;
            if (!enchant.canAppearOnTable()) continue;

            int level = enchant.getTableLevel();
            if (level <= 0) continue;

            enchant.onTableEnchant(item, level);
        }
    }
}
