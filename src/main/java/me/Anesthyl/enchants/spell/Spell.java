package me.Anesthyl.enchants.spell;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * Represents a spell that can be learned and leveled up in a spell book.
 */
public enum Spell {
    // Define spells here with their requirements per level
    FIREBALL(
            "Fireball",
            "Launch a blazing fireball at your enemies",
            Material.FIRE_CHARGE,
            Map.of(
                    1, new SpellRequirement(List.of(new ItemStack(Material.BLAZE_POWDER, 4)), 5),
                    2, new SpellRequirement(List.of(new ItemStack(Material.BLAZE_POWDER, 8)), 10),
                    3, new SpellRequirement(List.of(
                            new ItemStack(Material.BLAZE_POWDER, 16),
                            new ItemStack(Material.NETHER_STAR, 1)
                    ), 20)
            )
    ),
    TELEPORT(
            "Teleport",
            "Instantly teleport to your target location",
            Material.ENDER_PEARL,
            Map.of(
                    1, new SpellRequirement(List.of(new ItemStack(Material.ENDER_PEARL, 4)), 5),
                    2, new SpellRequirement(List.of(new ItemStack(Material.ENDER_PEARL, 8)), 10),
                    3, new SpellRequirement(List.of(
                            new ItemStack(Material.ENDER_PEARL, 16),
                            new ItemStack(Material.NETHER_STAR, 1)
                    ), 20)
            )
    ),
    WIND_BLAST(
            "Wind Blast",
            "Send a powerful gust of wind to knock back foes",
            Material.WIND_CHARGE,
            Map.of(
                    1, new SpellRequirement(List.of(new ItemStack(Material.WIND_CHARGE, 4)), 5),
                    2, new SpellRequirement(List.of(new ItemStack(Material.WIND_CHARGE, 8)), 10),
                    3, new SpellRequirement(List.of(
                            new ItemStack(Material.WIND_CHARGE, 16),
                            new ItemStack(Material.NETHER_STAR, 1)
                    ), 20)
            )
    ),
    NETHER_SHIELD(
            "Nether Shield",
            "Summon a shield of nether energy for protection",
            Material.NETHERITE_SCRAP,
            Map.of(
                    1, new SpellRequirement(List.of(new ItemStack(Material.NETHERITE_SCRAP, 2)), 5),
                    2, new SpellRequirement(List.of(new ItemStack(Material.NETHERITE_SCRAP, 4)), 10),
                    3, new SpellRequirement(List.of(
                            new ItemStack(Material.NETHERITE_SCRAP, 8),
                            new ItemStack(Material.NETHER_STAR, 1)
                    ), 20)
            )
      ),
    LIGHTNING_STRIKE(
            "Lightning Strike",
            "Call down devastating lightning from the sky",
            Material.LIGHTNING_ROD,
            Map.of(
                    1, new SpellRequirement(List.of(new ItemStack(Material.COPPER_INGOT, 8)), 5),
                    2, new SpellRequirement(List.of(new ItemStack(Material.COPPER_INGOT, 16)), 10),
                    3, new SpellRequirement(List.of(
                            new ItemStack(Material.COPPER_INGOT, 32),
                            new ItemStack(Material.NETHER_STAR, 1)
                    ), 20)
            )
    ),
    ROCK_WALL(
            "Rock Wall",
            "Conjure a temporary wall of stone for defense",
            Material.COBBLESTONE,
            Map.of(
                    1, new SpellRequirement(List.of(new ItemStack(Material.COBBLESTONE, 16)), 5),
                    2, new SpellRequirement(List.of(new ItemStack(Material.STONE, 24)), 10),
                    3, new SpellRequirement(List.of(
                            new ItemStack(Material.OBSIDIAN, 8),
                            new ItemStack(Material.NETHER_STAR, 1)
                    ), 20)
            )
    );

    private final String name;
    private final String description;
    private final Material icon;
    private final Map<Integer, SpellRequirement> levelRequirements;

    Spell(String name, String description, Material icon, Map<Integer, SpellRequirement> levelRequirements) {
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.levelRequirements = levelRequirements;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Material getIcon() {
        return icon;
    }

    public int getMaxLevel() {
        return levelRequirements.size();
    }

    public SpellRequirement getRequirement(int level) {
        return levelRequirements.get(level);
    }

    /**
     * Gets the mana cost for casting this spell at a given level.
     */
    public double getManaCost(int level) {
        switch (this) {
            case FIREBALL:
                return 15 + (level * 5); // 20, 25, 30
            case TELEPORT:
                return 20 + (level * 5); // 25, 30, 35
            case WIND_BLAST:
                return 15 + (level * 5); // 20, 25, 30
            case NETHER_SHIELD:
                return 25 + (level * 5); // 30, 35, 40
            case LIGHTNING_STRIKE:
                return 30 + (level * 10); // 40, 50, 60
            case ROCK_WALL:
                return 20 + (level * 5); // 25, 30, 35
            default:
                return 20;
        }
    }

    /**
     * Represents the requirements to unlock or level up a spell.
     */
    public static class SpellRequirement {
        private final List<ItemStack> materials;
        private final int xpLevels;

        public SpellRequirement(List<ItemStack> materials, int xpLevels) {
            this.materials = materials;
            this.xpLevels = xpLevels;
        }

        public List<ItemStack> getMaterials() {
            return materials;
        }

        public int getXpLevels() {
            return xpLevels;
        }
    }
}
