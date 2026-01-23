package me.Anesthyl.enchants.stat;

/**
 * Enum for all player stat types.
 * 
 * Used by PlayerStats and StatManager to track and apply bonuses.
 */
public enum StatType {
    
    // Offensive Stats
    DAMAGE("§cDamage", "§7+%s%% damage"),
    ATTACK_SPEED("§cAttack Speed", "§7+%s%% attack speed"),
    CRIT_CHANCE("§cCrit Chance", "§7+%s%% crit chance"),
    CRIT_DAMAGE("§cCrit Damage", "§7+%s%% crit damage"),
    
    // Defensive Stats
    DEFENSE("§9Defense", "§7+%s%% damage reduction"),
    MAX_HEALTH("§4Max Health", "§7+%s%% max health"),
    HEALTH_REGEN("§4Health Regen", "§7+%s HP/sec"),
    
    // Utility Stats
    MOVEMENT_SPEED("§eSpeed", "§7+%s%% movement speed"),
    KNOCKBACK_RESISTANCE("§eKnockback Resist", "§7+%s%% resistance"),
    
    // Special
    LIFESTEAL("§aLifesteal", "§7+%s%% healing from damage"),
    XP_GAIN("§eXP Gain", "§7+%s%% experience"),
    LOOT_BONUS("§6Loot Bonus", "§7+%s%% extra drops");
    
    private final String displayName;
    private final String tooltip; // %s will be replaced with percentage
    
    StatType(String displayName, String tooltip) {
        this.displayName = displayName;
        this.tooltip = tooltip;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getTooltip(double value) {
        return String.format(tooltip, String.format("%.1f", value));
    }
}
