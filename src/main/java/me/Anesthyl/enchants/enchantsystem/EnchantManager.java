package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class EnchantManager {

    private final Map<String, CustomEnchant> enchants = new HashMap<>();

    // Register a new enchant
    public void registerEnchant(CustomEnchant enchant) {
        enchants.put(enchant.getKey().getKey(), enchant);
    }

    // Get all registered enchants
    public Collection<CustomEnchant> getEnchants() {
        return enchants.values();
    }

    // Get enchants applied on an item
    public Map<CustomEnchant, Integer> getItemEnchants(ItemStack item) {
        Map<CustomEnchant, Integer> found = new HashMap<>();
        if (!item.hasItemMeta()) return found;

        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();

        for (CustomEnchant enchant : enchants.values()) {
            Integer level = pdc.get(enchant.getKey(), PersistentDataType.INTEGER);
            if (level != null) {
                found.put(enchant, level);
            }
        }
        return found;
    }
}
//A way to register enchants
//
//A way to check what enchants an item has
//
//Ready for listeners and applying effects