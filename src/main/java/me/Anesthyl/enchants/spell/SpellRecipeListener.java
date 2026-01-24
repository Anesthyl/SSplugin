package me.Anesthyl.enchants.spell;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Handles registration of spell book crafting recipe.
 */
public class SpellRecipeListener implements Listener {
    private final JavaPlugin plugin;
    private final SpellManager spellManager;

    public SpellRecipeListener(JavaPlugin plugin, SpellManager spellManager) {
        this.plugin = plugin;
        this.spellManager = spellManager;
        registerRecipes();
    }

    private void registerRecipes() {
        // Spell Book Recipe:
        // [Nether Wart] [Blaze Powder] [Wind Charge]
        // [Blaze Powder] [Book] [Blaze Powder]
        // [Wind Charge] [Ender Pearl] [Nether Wart]

        ItemStack spellBook = spellManager.createSpellBook();
        NamespacedKey key = new NamespacedKey(plugin, "spell_book");

        ShapedRecipe recipe = new ShapedRecipe(key, spellBook);
        recipe.shape("NBW", "BPB", "WEN");
        recipe.setIngredient('N', Material.NETHER_WART);
        recipe.setIngredient('B', Material.BLAZE_POWDER);
        recipe.setIngredient('W', Material.WIND_CHARGE);
        recipe.setIngredient('E', Material.ENDER_PEARL);
        recipe.setIngredient('P', Material.BOOK);

        // Make discoverable in recipe book
        recipe.setGroup("spell_system");

        plugin.getServer().addRecipe(recipe);
        plugin.getLogger().info("Registered Spell Book crafting recipe");
    }
}
