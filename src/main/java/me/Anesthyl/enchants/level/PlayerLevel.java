package me.Anesthyl.enchants.level;

import java.util.EnumMap;
import java.util.Map;

/**
 * Stores leveling data for a single player across multiple skills.
 * Each skill has its own level and XP progression.
 * 
 * Immutable getters; all mutations go through LevelManager.
 */
public class PlayerLevel {
    
    // Store level and XP for each skill type
    private final Map<SkillType, Integer> skillLevels;
    private final Map<SkillType, Long> skillXP;
    
    public PlayerLevel() {
        this.skillLevels = new EnumMap<>(SkillType.class);
        this.skillXP = new EnumMap<>(SkillType.class);
        
        // Initialize all skills to level 1 with 0 XP
        for (SkillType skill : SkillType.values()) {
            skillLevels.put(skill, 1);
            skillXP.put(skill, 0L);
        }
    }
    
    /**
     * Get current level for a specific skill (1-based).
     */
    public int getLevel(SkillType skill) {
        return skillLevels.getOrDefault(skill, 1);
    }
    
    /**
     * Get total experience for a specific skill.
     */
    public long getTotalXP(SkillType skill) {
        return skillXP.getOrDefault(skill, 0L);
    }
    
    /**
     * Get overall player level (average of all skills).
     */
    public int getOverallLevel() {
        int total = 0;
        for (int level : skillLevels.values()) {
            total += level;
        }
        return total / SkillType.values().length;
    }
    
    /**
     * Get total XP across all skills.
     */
    public long getTotalXPAllSkills() {
        long total = 0;
        for (long xp : skillXP.values()) {
            total += xp;
        }
        return total;
    }
    
    /**
     * Increase XP for a specific skill.
     * Returns true if level up occurred for that skill.
     */
    protected boolean addXP(SkillType skill, long amount, Map<Integer, Long> thresholds) {
        if (amount <= 0) return false;

        int oldLevel = getLevel(skill);
        long currentXP = getTotalXP(skill);
        long newXP = currentXP + amount;
        
        skillXP.put(skill, newXP);
        recalculateLevel(skill, thresholds);

        return oldLevel != getLevel(skill);
    }

    /**
     * Recalculate level for a specific skill based on total XP.
     * Should be called by LevelManager after XP changes.
     */
    protected void recalculateLevel(SkillType skill, Map<Integer, Long> thresholds) {
        long totalXP = getTotalXP(skill);
        int newLevel = 1;
        
        for (int lvl = 1; lvl <= thresholds.size(); lvl++) {
            if (totalXP >= thresholds.get(lvl)) {
                newLevel = lvl;
            } else {
                break;
            }
        }
        
        skillLevels.put(skill, newLevel);
    }
    
    /**
     * Set level and XP for a specific skill (used for loading from persistence).
     */
    protected void setSkillData(SkillType skill, int level, long xp) {
        skillLevels.put(skill, level);
        skillXP.put(skill, xp);
    }
    
    /**
     * Get XP progress to next level for a specific skill (0 to 100).
     */
    public double getProgressToNextLevel(SkillType skill, Map<Integer, Long> thresholds) {
        int level = getLevel(skill);
        long totalXP = getTotalXP(skill);
        
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
     * Get XP needed to reach next level for a specific skill.
     */
    public long getXPForNextLevel(SkillType skill, Map<Integer, Long> thresholds) {
        int level = getLevel(skill);
        long totalXP = getTotalXP(skill);
        
        Long nextThreshold = thresholds.get(level + 1);
        if (nextThreshold == null) {
            return -1; // Max level reached
        }
        return Math.max(0, nextThreshold - totalXP);
    }
    
    /**
     * Get all skill levels as a map.
     */
    public Map<SkillType, Integer> getAllSkillLevels() {
        return new EnumMap<>(skillLevels);
    }
    
    /**
     * Get all skill XP as a map.
     */
    public Map<SkillType, Long> getAllSkillXP() {
        return new EnumMap<>(skillXP);
    }
    
    @Override
    public String toString() {
        return String.format("PlayerLevel{overallLevel=%d, totalXP=%d, skills=%d}", 
            getOverallLevel(), getTotalXPAllSkills(), skillLevels.size());
    }
}
