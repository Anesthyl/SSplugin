package me.Anesthyl.enchants.backpack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles backpack opening, closing, and item management
 */
public class BackpackListener implements Listener {

    private final BackpackManager backpackManager;
    private final Map<UUID, ItemStack> openBackpacks = new HashMap<>();

    public BackpackListener(BackpackManager backpackManager) {
        this.backpackManager = backpackManager;
    }

    /**
     * Open backpack when right-clicked
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!backpackManager.isBackpack(item)) return;

        event.setCancelled(true);

        // Load backpack contents
        ItemStack[] contents = backpackManager.loadInventory(item);

        // Create GUI
        Inventory gui = Bukkit.createInventory(null, 27, "§6Backpack");
        gui.setContents(contents);

        // Track which backpack is open
        openBackpacks.put(player.getUniqueId(), item);

        // Open GUI
        player.openInventory(gui);
    }

    /**
     * Save backpack contents when closed
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals("§6Backpack")) return;

        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();

        ItemStack backpack = openBackpacks.get(playerId);
        if (backpack == null) return;

        // Save contents to backpack
        ItemStack[] contents = event.getInventory().getContents();
        backpackManager.saveInventory(backpack, contents);

        // Cleanup
        openBackpacks.remove(playerId);
    }

    /**
     * Prevent putting backpacks inside backpacks
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("§6Backpack")) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();

        // Prevent backpack inception
        if (backpackManager.isBackpack(cursor) || backpackManager.isBackpack(current)) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage("§cYou cannot put a backpack inside a backpack!");
        }
    }
}
