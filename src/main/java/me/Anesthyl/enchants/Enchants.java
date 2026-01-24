package me.Anesthyl.enchants;

import me.Anesthyl.enchants.Commands.AddCustomEnchantCommand;
import me.Anesthyl.enchants.Commands.BackpackCommand;
import me.Anesthyl.enchants.Commands.HealCommand;
import me.Anesthyl.enchants.Commands.LevelCommand;
import me.Anesthyl.enchants.Commands.StatsCommand;
import me.Anesthyl.enchants.achievements.AchievementManager;
import me.Anesthyl.enchants.backpack.BackpackListener;
import me.Anesthyl.enchants.backpack.BackpackManager;
import me.Anesthyl.enchants.enchantsystem.*;
import me.Anesthyl.enchants.level.LevelManager;
import me.Anesthyl.enchants.listeners.AnvilListener;
import me.Anesthyl.enchants.listeners.BlockBreakListener;
import me.Anesthyl.enchants.listeners.CombatListener;
import me.Anesthyl.enchants.listeners.DonaldJumpListener;
import me.Anesthyl.enchants.listeners.EnchantTableListener;
import me.Anesthyl.enchants.listeners.GrindstoneListener;
import me.Anesthyl.enchants.listeners.ShinyListener;
import me.Anesthyl.enchants.listeners.SkillListener;
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
 * - Initializes EnchantManager, StatManager, and LevelManager.
 * - Registers all custom enchants (combat, mining, movement, armor).
 * - Registers listeners for combat, block breaking, and enchant table events.
 * - Tracks player levels and XP per activity.
 * - Registers a creative/test command: /addcustomenchant <enchant> [level]
 *   Allows manually adding any custom enchant for testing.
 */
public class Enchants extends JavaPlugin implements Listener {

    private EnchantManager enchantManager;
    private StatManager statManager;
    private LevelManager levelManager;
    private BackpackManager backpackManager;
    private AchievementManager achievementManager;

    @Override
    public void onEnable() {
        getLogger().info("Enchants Plugin Enabled");

        // 1️⃣ Initialize the EnchantManager
        enchantManager = new EnchantManager();

        // 2️⃣ Initialize the StatManager
        statManager = new StatManager(this, enchantManager);

        // 3️⃣ Initialize the LevelManager
        levelManager = new LevelManager(this);

        // 4️⃣ Initialize the BackpackManager
        backpackManager = new BackpackManager(this);

        // 5️⃣ Initialize the AchievementManager
        achievementManager = new AchievementManager(this, enchantManager);

        // 6️⃣ Register all custom enchants
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
        enchantManager.registerEnchant(new XPBoostEnchant(this));             // XP Boost

        // 7️⃣ Register listeners (pass managers for XP and stats)
        getServer().getPluginManager().registerEvents(
                new CombatListener(enchantManager, levelManager), this
        );
        getServer().getPluginManager().registerEvents(
                new EnchantTableListener(enchantManager), this
        );
        getServer().getPluginManager().registerEvents(
                new BlockBreakListener(enchantManager, levelManager), this
        );
        getServer().getPluginManager().registerEvents(
                new AnvilListener(enchantManager), this
        );
        getServer().getPluginManager().registerEvents(
                new GrindstoneListener(enchantManager), this
        );
        getServer().getPluginManager().registerEvents(
                new me.Anesthyl.enchants.listeners.LavaWalkerListener(enchantManager), this
        );
        getServer().getPluginManager().registerEvents(
                new DonaldJumpListener(enchantManager), this
        );
        getServer().getPluginManager().registerEvents(
                new ShinyListener(enchantManager), this
        );
        getServer().getPluginManager().registerEvents(
                new BackpackListener(backpackManager), this
        );
        getServer().getPluginManager().registerEvents(
                new SkillListener(levelManager), this
        );

        // 8️⃣ Register player join/quit listener for StatManager & LevelManager cleanup
        getServer().getPluginManager().registerEvents(this, this);

        // 9️⃣ Register commands
        AddCustomEnchantCommand addEnchantCmd = new AddCustomEnchantCommand(enchantManager);
        getCommand("addenchant").setExecutor(addEnchantCmd);
        LevelCommand levelCmd = new LevelCommand(levelManager);
        getCommand("level").setExecutor(levelCmd);
        getCommand("level").setTabCompleter(levelCmd);
        getCommand("stats").setExecutor(
                new StatsCommand(statManager)
        );
        getCommand("heal").setExecutor(
                new HealCommand()
        );
        getCommand("backpack").setExecutor(
                new BackpackCommand(backpackManager)
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

    /**
     * Getter for LevelManager
     */
    public LevelManager getLevelManager() {
        return levelManager;
    }

    /**
     * Getter for AchievementManager
     */
    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    // ==============================
    // Player Join/Quit Event Hooks
    // ==============================

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Initialize stats for the player
        statManager.recalculateStats(player);
        // Load/initialize level data
        levelManager.getPlayerLevel(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Clean up stats when player leaves
        statManager.removePlayer(player);
        // Save and clean up level data
        levelManager.removePlayer(player);
    }
}