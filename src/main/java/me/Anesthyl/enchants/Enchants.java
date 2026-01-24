package me.Anesthyl.enchants;

import me.Anesthyl.enchants.Commands.AddCustomEnchantCommand;
import me.Anesthyl.enchants.Commands.BackpackCommand;
import me.Anesthyl.enchants.Commands.DelWarpCommand;
import me.Anesthyl.enchants.Commands.GuidebookCommand;
import me.Anesthyl.enchants.Commands.HealCommand;
import me.Anesthyl.enchants.Commands.LevelCommand;
import me.Anesthyl.enchants.Commands.SetWarpCommand;
import me.Anesthyl.enchants.Commands.StatsCommand;
import me.Anesthyl.enchants.Commands.WarpCommand;
import me.Anesthyl.enchants.achievements.AchievementManager;
import me.Anesthyl.enchants.backpack.BackpackListener;
import me.Anesthyl.enchants.backpack.BackpackManager;
import me.Anesthyl.enchants.enchantsystem.*;
import me.Anesthyl.enchants.guidebook.GuidebookListener;
import me.Anesthyl.enchants.level.LevelManager;
import me.Anesthyl.enchants.listeners.AnvilListener;
import me.Anesthyl.enchants.listeners.BlockBreakListener;
import me.Anesthyl.enchants.listeners.CombatListener;
import me.Anesthyl.enchants.listeners.DonaldJumpListener;
import me.Anesthyl.enchants.listeners.EnchantTableListener;
import me.Anesthyl.enchants.listeners.GrindstoneListener;
import me.Anesthyl.enchants.listeners.ManaBrewingListener;
import me.Anesthyl.enchants.listeners.ManaPotionListener;
import me.Anesthyl.enchants.listeners.RecipeDiscoveryListener;
import me.Anesthyl.enchants.listeners.ShinyListener;
import me.Anesthyl.enchants.spell.ManaManager;
import me.Anesthyl.enchants.spell.SpellCastListener;
import me.Anesthyl.enchants.spell.SpellGUI;
import me.Anesthyl.enchants.spell.SpellManager;
import me.Anesthyl.enchants.spell.SpellRecipeListener;
import me.Anesthyl.enchants.spell.SpellWorkstationListener;
import me.Anesthyl.enchants.stat.StatManager;
import me.Anesthyl.enchants.warp.WarpManager;
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
    private SpellManager spellManager;
    private SpellGUI spellGUI;
    private ManaManager manaManager;
    private WarpManager warpManager;

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

        // 6️⃣ Initialize the Spell System
        spellManager = new SpellManager(this);
        manaManager = new ManaManager(this);
        spellGUI = new SpellGUI(spellManager, levelManager);
        new SpellRecipeListener(this, spellManager);

        // 6.5️⃣ Initialize the Warp System
        warpManager = new WarpManager(this);

        // 7️⃣ Register all custom enchants
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

        // 8️⃣ Register listeners (pass managers for XP and stats)
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
                new SpellWorkstationListener(this, spellManager, spellGUI), this
        );
        getServer().getPluginManager().registerEvents(spellGUI, this);
        getServer().getPluginManager().registerEvents(
                new SpellCastListener(this, spellManager, manaManager), this
        );
        getServer().getPluginManager().registerEvents(
                new RecipeDiscoveryListener(this), this
        );
        getServer().getPluginManager().registerEvents(
                new ManaPotionListener(manaManager), this
        );
        getServer().getPluginManager().registerEvents(
                new ManaBrewingListener(this), this
        );
        getServer().getPluginManager().registerEvents(
                new GuidebookListener(this), this
        );

        // 9️⃣ Register player join/quit listener for StatManager & LevelManager cleanup
        getServer().getPluginManager().registerEvents(this, this);

        // 🔟 Register commands
        AddCustomEnchantCommand addEnchantCmd = new AddCustomEnchantCommand(enchantManager);
        getCommand("addenchant").setExecutor(addEnchantCmd);
        getCommand("addenchant").setTabCompleter(addEnchantCmd);
        getCommand("level").setExecutor(
                new LevelCommand(levelManager)
        );
        getCommand("stats").setExecutor(
                new StatsCommand(statManager)
        );
        getCommand("heal").setExecutor(
                new HealCommand()
        );
        getCommand("backpack").setExecutor(
                new BackpackCommand(backpackManager)
        );
        getCommand("guidebook").setExecutor(
                new GuidebookCommand(this)
        );

        // Warp commands
        WarpCommand warpCmd = new WarpCommand(warpManager);
        getCommand("warp").setExecutor(warpCmd);
        getCommand("warp").setTabCompleter(warpCmd);

        SetWarpCommand setWarpCmd = new SetWarpCommand(warpManager);
        getCommand("setwarp").setExecutor(setWarpCmd);
        getCommand("setwarp").setTabCompleter(setWarpCmd);

        DelWarpCommand delWarpCmd = new DelWarpCommand(warpManager);
        getCommand("delwarp").setExecutor(delWarpCmd);
        getCommand("delwarp").setTabCompleter(delWarpCmd);

        getLogger().info("All custom enchants, spells, and commands registered!");
    }

    @Override
    public void onDisable() {
        // Shutdown mana manager
        if (manaManager != null) {
            manaManager.shutdown();
        }
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

    /**
     * Getter for SpellManager
     */
    public SpellManager getSpellManager() {
        return spellManager;
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
        // Initialize mana bar
        manaManager.getPlayerMana(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Clean up stats when player leaves
        statManager.removePlayer(player);
        // Save and clean up level data
        levelManager.removePlayer(player);
        // Clean up mana data
        manaManager.removePlayer(player);
    }
}