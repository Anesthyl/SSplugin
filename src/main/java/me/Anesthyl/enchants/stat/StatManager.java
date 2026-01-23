package me.Anesthyl.enchants.stat;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages all player stats.
 * 
 * Responsibilities:
 * - Track stats per player
 * - Calculate stat bonuses from enchants
 * - Apply stat effects (health, speed, etc.)
 * - Recalculate when enchants change
 */
public class StatManager {
    
    private final JavaPlugin plugin;
    private final EnchantManager enchantManager;
    private final Map<String, PlayerStats> playerStats = new HashMap<>();
    
    public StatManager(JavaPlugin plugin, EnchantManager enchantManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
    }
    
    /**
     * Get or create a player's stats.
     */
    public PlayerStats getPlayerStats(Player player) {
        String uuid = player.getUniqueId().toString();
        return playerStats.computeIfAbsent(uuid, k -> new PlayerStats());
    }
    
    /**
     * Remove player stats when they leave.
     */
    public void removePlayer(Player player) {
        playerStats.remove(player.getUniqueId().toString());
    }
    
    /**
     * Recalculate all stats for a player based on their equipped items.
     * Call this when a player equips a new item or gains an enchant.
     */
    public void recalculateStats(Player player) {
        PlayerStats stats = getPlayerStats(player);
        stats.reset();
        
        // Sum stats from all equipped items
        for (ItemStack item : player.getInventory().getArmorContents()) {
            addItemStats(player, item, stats);
        }
        
        // Include main hand
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand != null) {
            addItemStats(player, mainHand, stats);
        }
        
        // Include off-hand
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand != null) {
            addItemStats(player, offHand, stats);
        }
        
        // Apply stat effects to the player
        applyStatEffects(player, stats);
    }
    
    /**
     * Add stat bonuses from a single item's enchants.
     */
    private void addItemStats(Player player, ItemStack item, PlayerStats stats) {
        if (item == null || item.getType().isAir()) return;
        
        Map<CustomEnchant, Integer> enchants = enchantManager.getItemEnchants(item);
        for (Map.Entry<CustomEnchant, Integer> entry : enchants.entrySet()) {
            CustomEnchant enchant = entry.getKey();
            int level = entry.getValue();
            
            // Get stat bonuses from enchant (scaled by level)
            // This assumes enchants will override getStatBonus() method
            for (StatType type : StatType.values()) {
                double bonus = enchant.getStatBonus(type, level);
                if (bonus != 0.0) {
                    stats.addStat(type, bonus);
                }
            }
        }
    }
    
    /**
     * Apply stat effects to a player (e.g., increase max health, speed, etc.).
     * This uses Bukkit attributes for mechanical effects.
     */
    private void applyStatEffects(Player player, PlayerStats stats) {
        try {
            // Max Health (scales health attribute)
            double healthBonus = stats.getStat(StatType.MAX_HEALTH);
            double baseHealth = 20.0; // Vanilla base
            player.getAttribute(Attribute.MAX_HEALTH)
                    .setBaseValue(baseHealth * (1.0 + healthBonus / 100.0));
            
            // Movement Speed
            double speedBonus = stats.getStat(StatType.MOVEMENT_SPEED);
            double baseSpeed = 0.1; // Vanilla base
            player.getAttribute(Attribute.MOVEMENT_SPEED)
                    .setBaseValue(baseSpeed * (1.0 + speedBonus / 100.0));
            
            // Attack Speed
            double attackSpeedBonus = stats.getStat(StatType.ATTACK_SPEED);
            double baseAttackSpeed = 4.0; // Vanilla base
            player.getAttribute(Attribute.ATTACK_SPEED)
                    .setBaseValue(baseAttackSpeed * (1.0 + attackSpeedBonus / 100.0));
            
            // Knockback Resistance
            double knockbackResist = stats.getStat(StatType.KNOCKBACK_RESISTANCE);
            player.getAttribute(Attribute.KNOCKBACK_RESISTANCE)
                    .setBaseValue(Math.min(knockbackResist / 100.0, 1.0)); // capped at 100%
            
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply stat effects to " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Get damage multiplier for a player (used in damage calculations).
     * Returns value like 1.15 for +15% damage.
     */
    public double getDamageMultiplier(Player player) {
        PlayerStats stats = getPlayerStats(player);
        double damageBonus = stats.getStat(StatType.DAMAGE);
        return 1.0 + (damageBonus / 100.0);
    }
    
    /**
     * Get defense multiplier for a player (damage reduction).
     * Returns value like 0.85 for 15% reduction (takes 85% of damage).
     */
    public double getDefenseMultiplier(Player player) {
        PlayerStats stats = getPlayerStats(player);
        double defenseBonus = stats.getStat(StatType.DEFENSE);
        return Math.max(1.0 - (defenseBonus / 100.0), 0.0);
    }
    
    /**
     * Get crit chance as a decimal (0.0 to 1.0).
     */
    public double getCritChance(Player player) {
        PlayerStats stats = getPlayerStats(player);
        double critChance = stats.getStat(StatType.CRIT_CHANCE);
        return Math.min(critChance / 100.0, 1.0); // Cap at 100%
    }
    
    /**
     * Get crit damage multiplier.
     * Returns value like 1.5 for +50% crit damage.
     */
    public double getCritDamage(Player player) {
        PlayerStats stats = getPlayerStats(player);
        double critDamage = stats.getStat(StatType.CRIT_DAMAGE);
        return 1.0 + (critDamage / 100.0);
    }
    
    /**
     * Get lifesteal percentage (0.0 to 1.0).
     */
    public double getLifesteal(Player player) {
        PlayerStats stats = getPlayerStats(player);
        double lifesteal = stats.getStat(StatType.LIFESTEAL);
        return Math.min(lifesteal / 100.0, 1.0); // Cap at 100%
    }
    
    /**
     * Send stat summary to player.
     */
    public void showStats(Player player) {
        PlayerStats stats = getPlayerStats(player);
        player.sendMessage(stats.getStatsTooltip());
    }
}
