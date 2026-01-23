package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Smelter's Delight Enchant
 *
 * Dev Notes:
 * - Pickaxe-only enchant.
 * - Converts mined ores into their smelted form (ingots, nuggets, quartz).
 * - Works naturally with vanilla Fortune.
 * - Table Rarity: 10% chance at level 30.
 * - Internal level scaling 1-3: extra drop chance per level.
 * - Modular: can be used together with other custom enchants like Vein Miner.
 */
public class SmeltersDelightEnchant extends CustomEnchant {

    private static final Map<Material, Material> SMELT_MAP = new HashMap<>();
    private static final Random RANDOM = new Random();

    static {
        SMELT_MAP.put(Material.IRON_ORE, Material.IRON_INGOT);
        SMELT_MAP.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        SMELT_MAP.put(Material.NETHER_GOLD_ORE, Material.GOLD_NUGGET);
        SMELT_MAP.put(Material.NETHER_QUARTZ_ORE, Material.QUARTZ);
    }

    public SmeltersDelightEnchant(JavaPlugin plugin) {
        super(plugin, "smelters_delight", "§eSmelter's Delight", 3);
    }

    @Override
    public boolean canApply(ItemStack item) {
        return item != null && item.getType().toString().endsWith("_PICKAXE");
    }

    @Override
    public void onHit(Player attacker, org.bukkit.entity.LivingEntity target, int level) {
        // Combat not used
    }

    @Override
    public void onBlockBreak(Player player, Block block, int level) {
        if (block == null) return;

        Material smelted = SMELT_MAP.get(block.getType());
        if (smelted == null) return; // Not smeltable

        // Remove original block
        block.setType(Material.AIR);

        // Determine Fortune level
        ItemStack tool = player.getInventory().getItemInMainHand();
        int fortuneLevel = 0;
        if (tool != null && tool.getEnchantments() != null) {
            fortuneLevel = tool.getEnchantments().getOrDefault(
                    org.bukkit.enchantments.Enchantment.getByName("FORTUNE"), 0
            );
        }

        // Determine drop amount: internal level scaling + fortune
        int amount = 1 + RANDOM.nextInt(level); // base drops from enchant level
        for (int i = 0; i < fortuneLevel; i++) {
            if (RANDOM.nextDouble() < 0.33) amount++; // vanilla-style fortune
        }

        // Drop items naturally
        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smelted, amount));
    }

    @Override
    public boolean canAppearOnTable() {
        return true;
    }

    @Override
    public int getTableLevel() {
        // Only obtainable at level 30 table, 10% chance
        return RANDOM.nextDouble() <= 0.10 ? 1 + RANDOM.nextInt(getMaxLevel()) : 0;
    }

    @Override
    public void onTableEnchant(ItemStack item, int level) {
        if (item == null || level <= 0) return;
        item.getItemMeta().getPersistentDataContainer()
                .set(getKey(), org.bukkit.persistence.PersistentDataType.INTEGER, level);
    }

    /**
     * Returns the smelted form of the given block type.
     * Used by VeinMiner and other modular custom enchant interactions.
     */
    public Material getSmeltedBlock(Material type) {
        return SMELT_MAP.get(type);
    }
}
