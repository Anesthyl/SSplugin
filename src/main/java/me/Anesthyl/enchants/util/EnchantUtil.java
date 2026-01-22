package me.Anesthyl.enchants.util;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class EnchantUtil {

    // Apply a custom enchant to an item
    public static void applyEnchant(ItemStack item, CustomEnchant enchant, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Store enchant level in PersistentDataContainer
        meta.getPersistentDataContainer().set(
                enchant.getKey(),
                PersistentDataType.INTEGER,
                level
        );

        // Add enchant to lore
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        lore.add(enchant.getDisplayName() + " " + toRoman(level));
        meta.setLore(lore);

        item.setItemMeta(meta);
    }

    // Convert integer level to Roman numeral for display
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

//Apply enchants to items
//
//Automatically store level in item NBT
//
//Show the enchant name and level in lore