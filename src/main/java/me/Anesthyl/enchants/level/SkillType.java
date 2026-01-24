package me.Anesthyl.enchants.level;

/**
 * Enum defining all skill categories in the leveling system.
 * Each skill has its own independent level and XP progression.
 */
public enum SkillType {
    
    MINING("Mining", "⛏", "Break ores and stone", 10),
    BRUCE_LEE("Bruce-Lee", "👊", "Hand-to-hand combat mastery", 15),
    TOUGHNESS("Toughness", "❤", "Take damage without dying", 5),
    AGILITY("Agility", "⚡", "Sprint and jump", 3),
    ENCHANTING("Enchanting", "✨", "Enchant items", 25),
    WOOD_CUTTING("Wood Cutting", "🪓", "Chop logs and wood", 12),
    ARCHERY("Archery", "🏹", "Damage enemies with bows", 20),
    FISHING("Fishing", "🎣", "Catch fish and treasure", 15),
    CRAFTING("Crafting", "🔨", "Craft items", 8),
    DUELIST("Duelist", "🗡", "Deal damage with swords", 18),
    EXECUTIONER("Executioner", "🪓", "Deal damage with axes", 18),
    ALCHEMIST("Alchemist", "⚗", "Brew potions", 30);
    
    private final String displayName;
    private final String icon;
    private final String description;
    private final int baseXpPerAction;
    
    SkillType(String displayName, String icon, String description, int baseXpPerAction) {
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.baseXpPerAction = baseXpPerAction;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getBaseXpPerAction() {
        return baseXpPerAction;
    }
    
    /**
     * Get a formatted display string for this skill.
     */
    public String getFormattedName() {
        return icon + " §e" + displayName;
    }
    
    /**
     * Get skill type from string name (case insensitive).
     */
    public static SkillType fromString(String name) {
        if (name == null) return null;
        
        String normalized = name.toUpperCase().replace(" ", "_");
        try {
            return SkillType.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
