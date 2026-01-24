package me.Anesthyl.enchants.level;

import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Manages all player levels and experience.
 * 
 * Responsibilities:
 * - Track XP and levels per player
 * - Provide level thresholds (configurable)
 * - Grant XP for activities (mining, combat, etc.)
 * - Persist data via PersistentDataContainer
 */
public class LevelManager {
    
    private final JavaPlugin plugin;
    private final NamespacedKey levelKey;
    private final NamespacedKey xpKey;
    private final Map<String, PlayerLevel> playerLevels = new HashMap<>();
    private final Map<Integer, Long> levelThresholds = new TreeMap<>();
    
    // XP values per activity
    private final int XP_MINE_BLOCK = 10;
    private final int XP_CHOP_LOG = 15;
    private final int XP_KILL_MOB = 50;
    private final int XP_KILL_PLAYER = 100;
    private final int XP_TAKE_DAMAGE = 5; // per 0.5 hearts
    
    // Max levels and scaling
    private static final int MAX_LEVEL = 100;
    private static final int BASE_XP_PER_LEVEL = 100;
    
    public LevelManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.levelKey = new NamespacedKey(plugin, "player_level");
        this.xpKey = new NamespacedKey(plugin, "player_xp");
        
        // Generate level thresholds: exponential scaling
        generateThresholds(MAX_LEVEL);
    }
    
    /**
     * Generate level thresholds with exponential scaling.
     * Level 1 = 0 XP, Level 2 = 100 XP, Level 3 = 250 XP, etc.
     */
    private void generateThresholds(int maxLevel) {
        long totalXP = 0;
        levelThresholds.put(1, 0L); // Level 1 starts at 0
        
        for (int i = 2; i <= maxLevel; i++) {
            // Each level requires more XP (slightly exponential)
            long xpForThisLevel = BASE_XP_PER_LEVEL + (i - 2) * 50;
            totalXP += xpForThisLevel;
            levelThresholds.put(i, totalXP);
        }
    }
    
    /**
     * Get or load a player's level data.
     */
    public PlayerLevel getPlayerLevel(Player player) {
        String uuid = player.getUniqueId().toString();
        
        if (playerLevels.containsKey(uuid)) {
            return playerLevels.get(uuid);
        }
        
        // Load from PDC or create new
        PlayerLevel pLevel = loadFromPDC(player);
        playerLevels.put(uuid, pLevel);
        return pLevel;
    }
    
    /**
     * Load player level data from persistent storage.
     */
    private PlayerLevel loadFromPDC(Player player) {
        if (player.getPersistentDataContainer().has(levelKey, PersistentDataType.INTEGER) &&
            player.getPersistentDataContainer().has(xpKey, PersistentDataType.LONG)) {
            
            int level = player.getPersistentDataContainer().get(levelKey, PersistentDataType.INTEGER);
            long xp = player.getPersistentDataContainer().get(xpKey, PersistentDataType.LONG);
            return new PlayerLevel(level, xp);
        }
        
        return new PlayerLevel(); // Default: Level 1, 0 XP
    }
    
    /**
     * Save player level data to persistent storage.
     */
    private void saveToPDC(Player player, PlayerLevel pLevel) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(levelKey, PersistentDataType.INTEGER, pLevel.getLevel());
        pdc.set(xpKey, PersistentDataType.LONG, pLevel.getTotalXP());
    }
    
    /**
     * Remove player from cache (called on quit).
     */
    public void removePlayer(Player player) {
        String uuid = player.getUniqueId().toString();
        PlayerLevel pLevel = playerLevels.remove(uuid);
        
        // Save to PDC before removing
        if (pLevel != null) {
            saveToPDC(player, pLevel);
        }
    }
    
    /**
     * Add XP to a player.
     * Handles level-up detection and notifications.
     */
    public void addXP(Player player, int amount) {
        PlayerLevel pLevel = getPlayerLevel(player);

        boolean leveledUp = pLevel.addXP(amount, levelThresholds);

        // Level up notification
        if (leveledUp) {
            int newLevel = pLevel.getLevel();
            player.sendMessage("§6§l⭐ LEVEL UP! §6You are now §lLevel " + newLevel + "§6!");
            // TODO: Award stat bonuses on level up
        }

        // Save progress
        saveToPDC(player, pLevel);
    }
    
    /**
     * Activity XP methods
     */
    public void addMiningXP(Player player) {
        addXP(player, XP_MINE_BLOCK);
    }

    public void addMiningXP(Player player, double multiplier) {
        addXP(player, (int) (XP_MINE_BLOCK * multiplier));
    }

    public void addChoppingXP(Player player) {
        addXP(player, XP_CHOP_LOG);
    }

    public void addChoppingXP(Player player, double multiplier) {
        addXP(player, (int) (XP_CHOP_LOG * multiplier));
    }
    
    public void addMobKillXP(Player player) {
        addXP(player, XP_KILL_MOB);
    }
    
    public void addPlayerKillXP(Player player) {
        addXP(player, XP_KILL_PLAYER);
    }
    
    public void addDamageXP(Player player) {
        addXP(player, XP_TAKE_DAMAGE);
    }
    
    /**
     * Get player's current level.
     */
    public int getLevel(Player player) {
        return getPlayerLevel(player).getLevel();
    }
    
    /**
     * Get player's total XP.
     */
    public long getTotalXP(Player player) {
        return getPlayerLevel(player).getTotalXP();
    }
    
    /**
     * Get XP needed for next level.
     */
    public long getXPToNextLevel(Player player) {
        return getPlayerLevel(player).getXPForNextLevel(levelThresholds);
    }
    
    /**
     * Get progress to next level (0-100).
     */
    public double getProgressToNextLevel(Player player) {
        return getPlayerLevel(player).getProgressToNextLevel(levelThresholds);
    }
    
    /**
     * Get all level thresholds (read-only).
     */
    public Map<Integer, Long> getLevelThresholds() {
        return Collections.unmodifiableMap(levelThresholds);
    }
    
    /**
     * Get max level.
     */
    public int getMaxLevel() {
        return MAX_LEVEL;
    }
    
    /**
     * Send level info to player.
     */
    public void showLevelInfo(Player player) {
        PlayerLevel pLevel = getPlayerLevel(player);
        int level = pLevel.getLevel();
        long xp = pLevel.getTotalXP();
        long xpForNext = pLevel.getXPForNextLevel(levelThresholds);
        double progress = pLevel.getProgressToNextLevel(levelThresholds);
        
        player.sendMessage("§f═══════════════════════════");
        player.sendMessage("§eLevel: §6" + level + "/" + MAX_LEVEL);
        player.sendMessage("§eTotal XP: §6" + xp);
        if (xpForNext > 0) {
            player.sendMessage("§eXP to Next: §6" + xpForNext);
            player.sendMessage("§eProgress: §a" + String.format("%.1f", progress) + "%");
        } else {
            player.sendMessage("§eStatus: §6MAX LEVEL");
        }
        player.sendMessage("§f═══════════════════════════");
    }
}
