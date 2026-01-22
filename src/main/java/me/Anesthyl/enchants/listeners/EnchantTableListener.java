package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import me.Anesthyl.enchants.util.EnchantUtil;
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
        for (CustomEnchant enchant : enchantManager.getEnchants()) {
            if (!enchant.canApply(event.getItem())) continue;

            // 25% chance to apply this custom enchant
            if (Math.random() < 0.25) {
                int level = 1 + (int) (Math.random() * enchant.getMaxLevel());
                EnchantUtil.applyEnchant(event.getItem(), enchant, level);
            }
        }
    }
}
//