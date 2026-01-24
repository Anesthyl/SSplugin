package me.Anesthyl.enchants.backpack;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Base64;

/**
 * Manages backpack creation, recipes, and data storage
 */
public class BackpackManager {

    private final JavaPlugin plugin;
    private final NamespacedKey backpackKey;

    public BackpackManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.backpackKey = new NamespacedKey(plugin, "backpack");
        registerRecipe();
    }

    /**
     * Create a backpack item
     */
    public ItemStack createBackpack() {
        ItemStack backpack = new ItemStack(Material.LEATHER_HORSE_ARMOR);
        ItemMeta meta = backpack.getItemMeta();

        if (meta != null) {
            meta.setDisplayName("§6Backpack");
            meta.setLore(Arrays.asList(
                "§7Right-click to open",
                "§727 slot storage"
            ));

            // Mark as backpack using PDC
            meta.getPersistentDataContainer().set(
                backpackKey,
                PersistentDataType.STRING,
                "backpack"
            );

            backpack.setItemMeta(meta);
        }

        return backpack;
    }

    /**
     * Check if an item is a backpack
     */
    public boolean isBackpack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        return meta.getPersistentDataContainer()
                .has(backpackKey, PersistentDataType.STRING);
    }

    /**
     * Save inventory contents to backpack item
     */
    public void saveInventory(ItemStack backpack, ItemStack[] contents) {
        if (!isBackpack(backpack)) return;

        ItemMeta meta = backpack.getItemMeta();
        if (meta == null) return;

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(contents.length);
            for (ItemStack item : contents) {
                dataOutput.writeObject(item);
            }

            dataOutput.close();
            String encoded = Base64.getEncoder().encodeToString(outputStream.toByteArray());

            meta.getPersistentDataContainer().set(
                new NamespacedKey(plugin, "backpack_data"),
                PersistentDataType.STRING,
                encoded
            );

            backpack.setItemMeta(meta);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to save backpack contents: " + e.getMessage());
        }
    }

    /**
     * Load inventory contents from backpack item
     */
    public ItemStack[] loadInventory(ItemStack backpack) {
        if (!isBackpack(backpack)) return new ItemStack[27];

        ItemMeta meta = backpack.getItemMeta();
        if (meta == null) return new ItemStack[27];

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey dataKey = new NamespacedKey(plugin, "backpack_data");

        if (!pdc.has(dataKey, PersistentDataType.STRING)) {
            return new ItemStack[27]; // Empty backpack
        }

        try {
            String encoded = pdc.get(dataKey, PersistentDataType.STRING);
            if (encoded == null) return new ItemStack[27];

            byte[] decoded = Base64.getDecoder().decode(encoded);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(decoded);
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            int size = dataInput.readInt();
            ItemStack[] contents = new ItemStack[size];

            for (int i = 0; i < size; i++) {
                contents[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return contents;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load backpack contents: " + e.getMessage());
            return new ItemStack[27];
        }
    }

    /**
     * Register the backpack crafting recipe
     */
    private void registerRecipe() {
        ItemStack backpack = createBackpack();
        NamespacedKey recipeKey = new NamespacedKey(plugin, "backpack_recipe");

        ShapedRecipe recipe = new ShapedRecipe(recipeKey, backpack);
        recipe.shape(
            "LLL",
            "LCL",
            "LLL"
        );

        recipe.setIngredient('L', Material.LEATHER);
        recipe.setIngredient('C', Material.CHEST);

        // Make discoverable in recipe book
        recipe.setGroup("backpacks");

        Bukkit.addRecipe(recipe);
        plugin.getLogger().info("Backpack recipe registered!");
    }
}
