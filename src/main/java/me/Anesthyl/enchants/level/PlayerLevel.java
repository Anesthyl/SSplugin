package me.Anesthyl.enchants.level;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores leveling data for a single player.
 * 
 * Immutable getters; all mutations go through LevelManager.
 */
public class PlayerLevel {
    
    private int level;
    private long totalXP;
    
    public PlayerLevel(int startLevel, long startXP) {
        this.level = startLevel;
        this.totalXP = startXP;
    }
    
    public PlayerLevel() {
        this(1, 0L);
    }
    
    /**
     * Get current level (1-based).
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Get total experience accumulated.
     */
    public long getTotalXP() {
        return totalXP;
    }
    
    /**
     * Increase total XP by amount.
     * Returns true if level up occurred.
     */
    protected boolean addXP(long amount, Map<Integer, Long> thresholds) {
        if (amount <= 0) return false;

        int oldLevel = level;
        totalXP += amount;
        recalculateLevel(thresholds);

        return oldLevel != level;
    }

    /**
     * Recalculate level based on total XP.
     * Should be called by LevelManager after XP changes.
     */
    protected void recalculateLevel(Map<Integer, Long> thresholds) {
        int newLevel = 1;
        for (int lvl = 1; lvl <= thresholds.size(); lvl++) {
            if (totalXP >= thresholds.get(lvl)) {
                newLevel = lvl;
            } else {
                break;
            }
        }
        level = newLevel;
    }
    
    /**
     * Get XP progress to next level (0 to 100).
     */
    public double getProgressToNextLevel(Map<Integer, Long> thresholds) {
        long currentThreshold = thresholds.getOrDefault(level, 0L);
        long nextThreshold = thresholds.getOrDefault(level + 1, Long.MAX_VALUE);
        
        if (nextThreshold == Long.MAX_VALUE || currentThreshold == nextThreshold) {
            return 100.0; // Max level
        }
        
        long xpInRange = totalXP - currentThreshold;
        long rangeSize = nextThreshold - currentThreshold;
        
        return Math.min(100.0, (xpInRange / (double) rangeSize) * 100.0);
    }
    
    /**
     * Get XP needed to reach next level.
     */
    public long getXPForNextLevel(Map<Integer, Long> thresholds) {
        Long nextThreshold = thresholds.get(level + 1);
        if (nextThreshold == null) {
            return -1; // Max level reached
        }
        return Math.max(0, nextThreshold - totalXP);
    }
    
    @Override
    public String toString() {
        return String.format("PlayerLevel{level=%d, totalXP=%d}", level, totalXP);
    }
}
