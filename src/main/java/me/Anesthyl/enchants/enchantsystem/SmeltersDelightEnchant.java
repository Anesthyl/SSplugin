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
 * - Converts ores directly into smelted results.
 * - Fully overrides vanilla drops.
 * - Works independently and alongside Vein Miner.
 * - Fortune-compatible.
 * - Supports deepslate ores.
 */
public class SmeltersDelightEnchant extends CustomEnchant {

    private static final Random RANDOM = new Random();

    /**
     * Mapping of ore → smelted result
     */
    private static final Map<Material, Material> SMELT_MAP = new HashMap<>();

    static {
        SMELT_MAP.put(Material.IRON_ORE, Material.IRON_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT);

        SMELT_MAP.put(Material.GOLD_ORE, Material.GOLD_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT);

        SMELT_MAP.put(Material.COPPER_ORE, Material.COPPER_INGOT);
        SMELT_MAP.put(Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT);

        SMELT_MAP.put(Material.NETHER_GOLD_ORE, Material.GOLD_NUGGET);
        SMELT_MAP.put(Material.NETHER_QUARTZ_ORE, Material.QUARTZ);
    }

    public SmeltersDelightEnchant(JavaPlugin plugin) {
        super(plugin, "smelters_delight", "§eSmelter's Delight", 3);
    }

    // --------------------------------------------------
    // Core Logic
    // --------------------------------------------------

    @Override
    public void onBlockBreak(Player player, Block block, int level) {
        Material smelted = getSmeltedResult(block.getType());
        if (smelted == null) return;

        ItemStack tool = player.getInventory().getItemInMainHand();

        // Base amount scales with enchant level
        int amount = 1 + RANDOM.nextInt(level);

        // Fortune support
        if (tool != null) {
            int fortune = tool.getEnchantmentLevel(
                    org.bukkit.enchantments.Enchantment.FORTUNE
            );

            for (int i = 0; i < fortune; i++) {
                if (RANDOM.nextDouble() < 0.33) {
                    amount++;
                }
            }
        }

        // Drop smelted result ONLY
        block.getWorld().dropItemNaturally(
                block.getLocation(),
                new ItemStack(smelted, amount)
        );

        // Remove block manually to prevent vanilla drops
        block.setType(Material.AIR);
    }

    @Override
    public void onHit(Player attacker, org.bukkit.entity.LivingEntity target, int level) {
        // Not used
    }

    // --------------------------------------------------
    // Table Logic
    // --------------------------------------------------

    @Override
    public boolean canAppearOnTable() {
        return true;
    }

    @Override
    public int getTableLevel() {
        // Level 30 table equivalent, 10% chance
        return RANDOM.nextDouble() <= 0.10
                ? 1 + RANDOM.nextInt(getMaxLevel())
                : 0;
    }

    @Override
    public void onTableEnchant(ItemStack item, int level) {
        if (item == null || level <= 0) return;

        item.getItemMeta().getPersistentDataContainer()
                .set(getKey(),
                        org.bukkit.persistence.PersistentDataType.INTEGER,
                        level);
    }

    // --------------------------------------------------
    // Utility / Integration
    // --------------------------------------------------

    /**
     * Companion method for Vein Miner and other enchants.
     *
     * @param type Ore material
     * @return Smelted result or null if not supported
     */
    public Material getSmeltedResult(Material type) {
        return SMELT_MAP.get(type);
    }

    @Override
    public boolean canApply(ItemStack item) {
        return item != null && item.getType().toString().endsWith("_PICKAXE");
    }
}
