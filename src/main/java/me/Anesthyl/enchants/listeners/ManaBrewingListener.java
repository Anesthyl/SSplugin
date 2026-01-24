package me.Anesthyl.enchants.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;

/**
 * Handles custom mana potion brewing recipe.
 * Recipe: Awkward Potion + Glowstone Dust = Mana Potion
 * (Changed from Prismarine Crystals as they aren't accepted by vanilla brewing stands)
 */
public class ManaBrewingListener implements Listener {
    private final JavaPlugin plugin;

    public ManaBrewingListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBrew(BrewEvent event) {
        BrewerInventory inventory = event.getContents();
        ItemStack ingredient = inventory.getIngredient();

        // Check if ingredient is glowstone dust
        if (ingredient == null || ingredient.getType() != Material.GLOWSTONE_DUST) {
            return;
        }

        // Check all three potion slots for water bottles (we'll convert them to mana potions)
        boolean hasManaRecipe = false;
        for (int i = 0; i < 3; i++) {
            ItemStack potion = inventory.getItem(i);

            if (potion != null && potion.getType() == Material.POTION) {
                if (potion.getItemMeta() instanceof PotionMeta meta) {
                    // Check if it's a water bottle
                    if (meta.getBasePotionType() == PotionType.WATER) {
                        hasManaRecipe = true;
                        break;
                    }
                }
            }
        }

        if (!hasManaRecipe) {
            return;
        }

        // Cancel the normal brewing and replace with mana potions
        event.setCancelled(true);

        // Schedule the replacement for after the event
        org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
            // Consume one ingredient
            if (ingredient.getAmount() > 1) {
                ingredient.setAmount(ingredient.getAmount() - 1);
            } else {
                inventory.setIngredient(null);
            }

            // Replace water bottles with mana potions
            for (int i = 0; i < 3; i++) {
                ItemStack potion = inventory.getItem(i);
                if (potion != null && potion.getType() == Material.POTION) {
                    if (potion.getItemMeta() instanceof PotionMeta meta) {
                        if (meta.getBasePotionType() == PotionType.WATER) {
                            ItemStack manaPotion = ManaPotionListener.createManaPotion();
                            inventory.setItem(i, manaPotion);
                        }
                    }
                }
            }
        });
    }
}
