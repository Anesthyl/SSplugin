package me.Anesthyl.enchants;

import me.Anesthyl.enchants.enchantsystem.*;
import me.Anesthyl.enchants.listeners.BlockBreakListener;
import me.Anesthyl.enchants.listeners.CombatListener;
import me.Anesthyl.enchants.listeners.EnchantTableListener;
import org.bukkit.plugin.java.JavaPlugin;

public class Enchants extends JavaPlugin {

    private EnchantManager enchantManager;

    @Override
    public void onEnable() {

        // 1️⃣ Initialize enchant manager
        enchantManager = new EnchantManager();

        // 2️⃣ Register all custom enchants
        enchantManager.registerEnchant(new LifestealEnchant(this));
        enchantManager.registerEnchant(new ExplosiveStrikeEnchant(this));
        enchantManager.registerEnchant(new SmeltersDelightEnchant(this));

        // 3️⃣ Register combat listener (handles on-hit effects)
        getServer().getPluginManager().registerEvents(
                new CombatListener(enchantManager),
                this
        );

        // 4️⃣ Register enchanting table listener (random table drops)
        getServer().getPluginManager().registerEvents(
                new EnchantTableListener(enchantManager),
                this
        );

        // 5️⃣ Register block break listener (for Smelter's Delight)
        getServer().getPluginManager().registerEvents(
                new BlockBreakListener(enchantManager),
                this
        );

        getLogger().info("Enchants plugin enabled with all custom enchants!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Enchants plugin disabled.");
    }

    // Optional getter for the manager if needed elsewhere
    public EnchantManager getEnchantManager() {
        return enchantManager;
    }
}
