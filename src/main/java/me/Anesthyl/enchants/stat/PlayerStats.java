package me.Anesthyl.enchants.stat;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores stat bonuses for a single player.
 * 
 * Stats are stored as percentages (e.g., 15.0 = +15%).
 * All stats default to 0 (no bonus).
 */
public class PlayerStats {
    
    private final Map<StatType, Double> stats = new HashMap<>();
    
    public PlayerStats() {
        // Initialize all stats to 0
        for (StatType type : StatType.values()) {
            stats.put(type, 0.0);
        }
    }
    
    /**
     * Set a stat to an absolute value.
     */
    public void setStat(StatType type, double value) {
        stats.put(type, value);
    }
    
    /**
     * Add to an existing stat (cumulative).
     */
    public void addStat(StatType type, double value) {
        stats.put(type, stats.getOrDefault(type, 0.0) + value);
    }
    
    /**
     * Get the current value of a stat.
     */
    public double getStat(StatType type) {
        return stats.getOrDefault(type, 0.0);
    }
    
    /**
     * Reset all stats to 0.
     */
    public void reset() {
        for (StatType type : StatType.values()) {
            stats.put(type, 0.0);
        }
    }
    
    /**
     * Get a copy of all stats.
     */
    public Map<StatType, Double> getAll() {
        return new HashMap<>(stats);
    }
    
    /**
     * Get formatted tooltip of all active stats.
     */
    public String getStatsTooltip() {
        StringBuilder sb = new StringBuilder();
        sb.append("§f═══════════════════\n");
        sb.append("§ePlayer Stats§f\n");
        sb.append("§f═══════════════════\n");
        
        for (StatType type : StatType.values()) {
            double value = getStat(type);
            if (value != 0.0) {
                String prefix = value > 0 ? "§a" : "§c";
                sb.append(type.getDisplayName()).append(": ");
                sb.append(prefix).append(type.getTooltip(value)).append("\n");
            }
        }
        
        sb.append("§f═══════════════════");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "PlayerStats{" +
                "stats=" + stats +
                '}';
    }
}
