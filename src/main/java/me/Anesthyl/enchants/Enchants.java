package me.Anesthyl.enchants;

import me.Anesthyl.enchants.Commands.AddCustomEnchantCommand;
import me.Anesthyl.enchants.enchantsystem.*;
import me.Anesthyl.enchants.listeners.BlockBreakListener;
import me.Anesthyl.enchants.listeners.CombatListener;
import me.Anesthyl.enchants.listeners.EnchantTableListener;
import me.Anesthyl.enchants.stat.StatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for Enchants.
 *
 * Dev Notes:
 * - Initializes EnchantManager and StatManager.
 * - Registers all custom enchants (combat, mining, movement, armor).
 * - Registers listeners for combat, block breaking, and enchant table events.
 * - Registers a creative/test command: /addcustomenchant <enchant> [level]
 *   Allows manually adding any custom enchant for testing.
 */
public class Enchants extends JavaPlugin implements Listener {

    private EnchantManager enchantManager;
    private StatManager statManager;

    @Override
    public void onEnable() {
        getLogger().info("Enchants Plugin Enabled");

        // 1️⃣ Initialize the EnchantManager
        enchantManager = new EnchantManager();

        // 2️⃣ Initialize the StatManager
        statManager = new StatManager(this, enchantManager);

        // 3️⃣ Register all custom enchants
        // Combat Enchants
        enchantManager.registerEnchant(new LifestealEnchant(this));           // Lifesteal
        enchantManager.registerEnchant(new ExplosiveStrikeEnchant(this));     // Explosive Strike

        // Mining Enchants
        enchantManager.registerEnchant(new SmeltersDelightEnchant(this));     // Smelter's Delight
        enchantManager.registerEnchant(new VeinMinerEnchant(this));           // Vein Miner
        enchantManager.registerEnchant(new ExcavatorEnchant(this));           // Excavator

        // Movement Enchants
        enchantManager.registerEnchant(new LavaWalkerEnchant(this));          // Lava Walker
        enchantManager.registerEnchant(new DonaldJumpEnchant(this));          // Donald Jump

        // Armor Enchants
        enchantManager.registerEnchant(new ShinyEnchant(this));               // Shiny (Gold-like behavior)

        // 4️⃣ Register listeners
        getServer().getPluginManager().registerEvents(
                new CombatListener(enchantManager), this
        );
        getServer().getPluginManager().registerEvents(
                new EnchantTableListener(enchantManager), this
        );
        getServer().getPluginManager().registerEvents(
                new BlockBreakListener(enchantManager), this
        );

        // 5️⃣ Register player join/quit listener for StatManager cleanup
        getServer().getPluginManager().registerEvents(this, this);

        // 6️⃣ Register creative/test command for manual enchant application
        getCommand("addcustomenchant").setExecutor(
                new AddCustomEnchantCommand(enchantManager)
        );

        getLogger().info("All custom enchants and commands registered!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Enchants Plugin Disabled");
    }

    /**
     * Getter for EnchantManager
     */
    public EnchantManager getEnchantManager() {
        return enchantManager;
    }

    /**
     * Getter for StatManager
     */
    public StatManager getStatManager() {
        return statManager;
    }

    // ==============================
    // Stat Manager Event Hooks
    // ==============================

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Initialize stats for the player
        statManager.recalculateStats(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Clean up stats when player leaves
        statManager.removePlayer(player);
    }}