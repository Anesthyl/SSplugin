package me.Anesthyl.enchants.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

/**
 * Listens for players obtaining items and unlocks custom recipes in their recipe book.
 */
public class RecipeDiscoveryListener implements Listener {
    private final JavaPlugin plugin;
    private final NamespacedKey spellBookRecipe;
    private final NamespacedKey backpackRecipe;

    public RecipeDiscoveryListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.spellBookRecipe = new NamespacedKey(plugin, "spell_book");
        this.backpackRecipe = new NamespacedKey(plugin, "backpack_recipe");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        checkAndUnlockRecipes(player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        // Check recipes after a short delay to ensure inventory is updated
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            checkAndUnlockRecipes(player);
        }, 1L);
    }

    private void checkAndUnlockRecipes(Player player) {
        Set<Material> playerMaterials = getPlayerMaterials(player);

        // Check Spell Book Recipe ingredients
        if (!player.hasDiscoveredRecipe(spellBookRecipe)) {
            if (playerMaterials.contains(Material.NETHER_WART) ||
                playerMaterials.contains(Material.BLAZE_POWDER) ||
                playerMaterials.contains(Material.WIND_CHARGE) ||
                playerMaterials.contains(Material.ENDER_PEARL) ||
                playerMaterials.contains(Material.BOOK)) {
                
                player.discoverRecipe(spellBookRecipe);
            }
        }

        // Check Backpack Recipe ingredients
        if (!player.hasDiscoveredRecipe(backpackRecipe)) {
            if (playerMaterials.contains(Material.LEATHER) ||
                playerMaterials.contains(Material.CHEST)) {
                
                player.discoverRecipe(backpackRecipe);
            }
        }
    }

    private Set<Material> getPlayerMaterials(Player player) {
        Set<Material> materials = new HashSet<>();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                materials.add(item.getType());
            }
        }
        return materials;
    }
}
