package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GrindstoneListener
 *
 * Dev Notes:
 * - Allows players to remove custom enchants via grindstone
 * - Removes ALL enchants (vanilla + custom) and repairs item
 * - Grants XP based on enchantment levels removed
 * - Updates lore to remove custom enchant lines
 */
public class GrindstoneListener implements Listener {

    private final EnchantManager enchantManager;

    public GrindstoneListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    @EventHandler
    public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
        GrindstoneInventory inventory = event.getInventory();
        ItemStack input = inventory.getItem(0);
        ItemStack second = inventory.getItem(1);

        // Only process single item disenchanting
        if (input == null || (second != null && second.getType() != Material.AIR)) {
            return;
        }

        // Check if item has custom enchants
        Map<CustomEnchant, Integer> customEnchants = enchantManager.getItemEnchants(input);

        if (customEnchants.isEmpty() && !input.hasItemMeta()) {
            return;
        }

        // Clone the input item for result
        ItemStack result = input.clone();
        ItemMeta meta = result.getItemMeta();

        if (meta == null) return;

        // Remove all custom enchants from PDC
        for (CustomEnchant enchant : customEnchants.keySet()) {
            meta.getPersistentDataContainer().remove(enchant.getKey());
        }

        // Remove all vanilla enchants
        for (org.bukkit.enchantments.Enchantment enchant : meta.getEnchants().keySet()) {
            meta.removeEnchant(enchant);
        }

        // Clean up lore - remove custom enchant lines
        if (meta.hasLore()) {
            List<String> lore = new ArrayList<>(meta.getLore());

            // Build list of enchant display names to remove
            List<String> enchantPrefixes = new ArrayList<>();
            for (CustomEnchant enchant : enchantManager.getAllEnchants()) {
                String displayPrefix = enchant.getDisplayName().split(" ")[0];
                enchantPrefixes.add(displayPrefix);
            }

            lore.removeIf(line -> {
                for (String prefix : enchantPrefixes) {
                    if (line.startsWith(prefix)) {
                        return true;
                    }
                }
                return false;
            });

            meta.setLore(lore);
        }

        // Repair the item fully
        if (result.getType().getMaxDurability() > 0 && meta instanceof Damageable) {
            ((Damageable) meta).setDamage(0);
        }

        result.setItemMeta(meta);
        event.setResult(result);
    }

    @EventHandler
    public void onGrindstoneClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof GrindstoneInventory)) return;

        // Check if clicking the result slot
        if (event.getSlot() != 2) return;

        GrindstoneInventory grindstone = (GrindstoneInventory) event.getInventory();
        ItemStack input = grindstone.getItem(0);

        if (input == null) return;

        // Calculate XP to drop based on custom enchants
        Map<CustomEnchant, Integer> customEnchants = enchantManager.getItemEnchants(input);
        int totalLevels = 0;

        for (Integer level : customEnchants.values()) {
            totalLevels += level;
        }

        // Add vanilla enchant levels
        if (input.hasItemMeta() && input.getItemMeta() != null) {
            for (Integer level : input.getItemMeta().getEnchants().values()) {
                totalLevels += level;
            }
        }

        // Drop XP orbs at grindstone location (handled by vanilla for normal enchants,
        // but we ensure custom enchants also grant XP)
        if (totalLevels > 0 && event.getWhoClicked() instanceof org.bukkit.entity.Player) {
            org.bukkit.entity.Player player = (org.bukkit.entity.Player) event.getWhoClicked();
            // Give XP based on enchant levels (vanilla formula: random between 1-level xp per enchant)
            int xpToGive = Math.min(totalLevels * 2, 100); // Cap at 100 XP
            player.giveExp(xpToGive);
        }
    }
}
