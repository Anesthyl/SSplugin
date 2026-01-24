package me.Anesthyl.enchants.level;

import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Manages all player levels and experience across multiple skills.
 * 
 * Responsibilities:
 * - Track XP and levels per player per skill
 * - Provide level thresholds (configurable)
 * - Grant XP for activities based on skill type
 * - Persist data via PersistentDataContainer
 */
public class LevelManager {
    
    private final JavaPlugin plugin;
    private final Map<SkillType, NamespacedKey> skillLevelKeys;
    private final Map<SkillType, NamespacedKey> skillXpKeys;
    private final Map<String, PlayerLevel> playerLevels = new HashMap<>();
    private final Map<Integer, Long> levelThresholds = new TreeMap<>();
    
    // Max levels and scaling
    private static final int MAX_LEVEL = 100;
    private static final int BASE_XP_PER_LEVEL = 100;
    
    public LevelManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.skillLevelKeys = new EnumMap<>(SkillType.class);
        this.skillXpKeys = new EnumMap<>(SkillType.class);
        
        // Create namespaced keys for each skill
        for (SkillType skill : SkillType.values()) {
            String keyName = skill.name().toLowerCase();
            skillLevelKeys.put(skill, new NamespacedKey(plugin, keyName + "_level"));
            skillXpKeys.put(skill, new NamespacedKey(plugin, keyName + "_xp"));
        }
        
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
        PlayerLevel pLevel = new PlayerLevel();
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        
        // Load each skill's data
        for (SkillType skill : SkillType.values()) {
            NamespacedKey levelKey = skillLevelKeys.get(skill);
            NamespacedKey xpKey = skillXpKeys.get(skill);
            
            if (pdc.has(levelKey, PersistentDataType.INTEGER) && 
                pdc.has(xpKey, PersistentDataType.LONG)) {
                
                int level = pdc.get(levelKey, PersistentDataType.INTEGER);
                long xp = pdc.get(xpKey, PersistentDataType.LONG);
                pLevel.setSkillData(skill, level, xp);
            }
        }
        
        return pLevel;
    }
    
    /**
     * Save player level data to persistent storage.
     */
    private void saveToPDC(Player player, PlayerLevel pLevel) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        
        // Save each skill's data
        for (SkillType skill : SkillType.values()) {
            NamespacedKey levelKey = skillLevelKeys.get(skill);
            NamespacedKey xpKey = skillXpKeys.get(skill);
            
            pdc.set(levelKey, PersistentDataType.INTEGER, pLevel.getLevel(skill));
            pdc.set(xpKey, PersistentDataType.LONG, pLevel.getTotalXP(skill));
        }
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
     * Add XP to a player for a specific skill.
     * Handles level-up detection and notifications.
     */
    public void addXP(Player player, SkillType skill, int amount) {
        PlayerLevel pLevel = getPlayerLevel(player);

        boolean leveledUp = pLevel.addXP(skill, amount, levelThresholds);

        // Level up notification
        if (leveledUp) {
            int newLevel = pLevel.getLevel(skill);
            player.sendMessage("§6§l⭐ LEVEL UP! §6" + skill.getFormattedName() + " §6is now §lLevel " + newLevel + "§6!");
            // TODO: Award stat bonuses on level up
        }

        // Save progress
        saveToPDC(player, pLevel);
    }
    
    /**
     * Add XP with a multiplier.
     */
    public void addXP(Player player, SkillType skill, int amount, double multiplier) {
        addXP(player, skill, (int) (amount * multiplier));
    }
    
    /**
     * Convenient skill-specific XP methods
     */
    public void addMiningXP(Player player, int amount) {
        addXP(player, SkillType.MINING, amount);
    }
    
    public void addMiningXP(Player player, double multiplier) {
        addXP(player, SkillType.MINING, SkillType.MINING.getBaseXpPerAction(), multiplier);
    }
    
    public void addWoodCuttingXP(Player player, int amount) {
        addXP(player, SkillType.WOOD_CUTTING, amount);
    }
    
    public void addWoodCuttingXP(Player player, double multiplier) {
        addXP(player, SkillType.WOOD_CUTTING, SkillType.WOOD_CUTTING.getBaseXpPerAction(), multiplier);
    }
    
    public void addBruceLeeXP(Player player, int amount) {
        addXP(player, SkillType.BRUCE_LEE, amount);
    }
    
    public void addDuelistXP(Player player, int amount) {
        addXP(player, SkillType.DUELIST, amount);
    }
    
    public void addExecutionerXP(Player player, int amount) {
        addXP(player, SkillType.EXECUTIONER, amount);
    }
    
    public void addArcheryXP(Player player, int amount) {
        addXP(player, SkillType.ARCHERY, amount);
    }
    
    public void addToughnessXP(Player player, int amount) {
        addXP(player, SkillType.TOUGHNESS, amount);
    }
    
    public void addAgilityXP(Player player, int amount) {
        addXP(player, SkillType.AGILITY, amount);
    }
    
    public void addFishingXP(Player player, int amount) {
        addXP(player, SkillType.FISHING, amount);
    }
    
    public void addCraftingXP(Player player, int amount) {
        addXP(player, SkillType.CRAFTING, amount);
    }
    
    public void addEnchantingXP(Player player, int amount) {
        addXP(player, SkillType.ENCHANTING, amount);
    }
    
    public void addAlchemistXP(Player player, int amount) {
        addXP(player, SkillType.ALCHEMIST, amount);
    }
    
    /**
     * Legacy methods for backward compatibility
     */
    @Deprecated
    public void addChoppingXP(Player player) {
        addWoodCuttingXP(player, 1.0);
    }
    
    @Deprecated
    public void addChoppingXP(Player player, double multiplier) {
        addWoodCuttingXP(player, multiplier);
    }
    
    @Deprecated
    public void addMobKillXP(Player player) {
        addBruceLeeXP(player, SkillType.BRUCE_LEE.getBaseXpPerAction());
    }
    
    @Deprecated
    public void addPlayerKillXP(Player player) {
        addBruceLeeXP(player, SkillType.BRUCE_LEE.getBaseXpPerAction() * 5);
    }
    
    @Deprecated
    public void addDamageXP(Player player) {
        addToughnessXP(player, SkillType.TOUGHNESS.getBaseXpPerAction());
    }
    
    /**
     * Get player's current level for a specific skill.
     */
    public int getLevel(Player player, SkillType skill) {
        return getPlayerLevel(player).getLevel(skill);
    }
    
    /**
     * Get player's total XP for a specific skill.
     */
    public long getTotalXP(Player player, SkillType skill) {
        return getPlayerLevel(player).getTotalXP(skill);
    }
    
    /**
     * Get XP needed for next level for a specific skill.
     */
    public long getXPToNextLevel(Player player, SkillType skill) {
        return getPlayerLevel(player).getXPForNextLevel(skill, levelThresholds);
    }
    
    /**
     * Get progress to next level (0-100) for a specific skill.
     */
    public double getProgressToNextLevel(Player player, SkillType skill) {
        return getPlayerLevel(player).getProgressToNextLevel(skill, levelThresholds);
    }
    
    /**
     * Get player's overall level (average of all skills).
     */
    public int getOverallLevel(Player player) {
        return getPlayerLevel(player).getOverallLevel();
    }
    
    /**
     * Legacy method - returns overall level
     */
    @Deprecated
    public int getLevel(Player player) {
        return getOverallLevel(player);
    }
    
    /**
     * Legacy method - returns total XP across all skills
     */
    @Deprecated
    public long getTotalXP(Player player) {
        return getPlayerLevel(player).getTotalXPAllSkills();
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
     * Send overall level info to player (all skills).
     */
    public void showLevelInfo(Player player) {
        PlayerLevel pLevel = getPlayerLevel(player);
        int overallLevel = pLevel.getOverallLevel();
        long totalXP = pLevel.getTotalXPAllSkills();
        
        player.sendMessage("§f═══════════════════════════");
        player.sendMessage("§6§lYour Skills");
        player.sendMessage("§eOverall Level: §6" + overallLevel + " §7(Average)");
        player.sendMessage("§eTotal XP: §6" + totalXP);
        player.sendMessage("§f");
        
        // Show all skills in a compact format
        for (SkillType skill : SkillType.values()) {
            int level = pLevel.getLevel(skill);
            long xpForNext = pLevel.getXPForNextLevel(skill, levelThresholds);
            double progress = pLevel.getProgressToNextLevel(skill, levelThresholds);
            
            String progressBar = createProgressBar(progress, 10);
            
            if (xpForNext > 0) {
                player.sendMessage(skill.getFormattedName() + " §7Lv." + level + " " + progressBar + " §7" + xpForNext + " XP");
            } else {
                player.sendMessage(skill.getFormattedName() + " §7Lv." + level + " §6[MAX]");
            }
        }
        
        player.sendMessage("§f═══════════════════════════");
    }
    
    /**
     * Send detailed level info for a specific skill.
     */
    public void showSkillInfo(Player player, SkillType skill) {
        PlayerLevel pLevel = getPlayerLevel(player);
        int level = pLevel.getLevel(skill);
        long xp = pLevel.getTotalXP(skill);
        long xpForNext = pLevel.getXPForNextLevel(skill, levelThresholds);
        double progress = pLevel.getProgressToNextLevel(skill, levelThresholds);
        
        player.sendMessage("§f═══════════════════════════");
        player.sendMessage(skill.getFormattedName());
        player.sendMessage("§7" + skill.getDescription());
        player.sendMessage("§f");
        player.sendMessage("§eLevel: §6" + level + "/" + MAX_LEVEL);
        player.sendMessage("§eTotal XP: §6" + xp);
        if (xpForNext > 0) {
            player.sendMessage("§eXP to Next: §6" + xpForNext);
            String progressBar = createProgressBar(progress, 20);
            player.sendMessage("§eProgress: " + progressBar + " §a" + String.format("%.1f", progress) + "%");
        } else {
            player.sendMessage("§eStatus: §6§lMAX LEVEL");
        }
        player.sendMessage("§f═══════════════════════════");
    }
    
    /**
     * Create a visual progress bar.
     */
    private String createProgressBar(double progress, int length) {
        int filled = (int) Math.round(progress / 100.0 * length);
        StringBuilder bar = new StringBuilder("§a[");
        
        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("▰");
            } else {
                bar.append("§7▱");
            }
        }
        
        bar.append("§a]");
        return bar.toString();
    }
}
