package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

public class EnchantTableListener implements Listener {

    private final EnchantManager enchantManager;

    public EnchantTableListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        // Let the manager apply all eligible table enchants
        enchantManager.applyTableEnchants(event.getItem());
    }
}
