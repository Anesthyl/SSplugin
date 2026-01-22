package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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

    /**
     * Called when a block is broken.
     * @param player The player breaking the block
     * @param block The block being broken
     * @param level Enchant level
     */
    public void onBlockBreak(Player player, Block block, int level) {
        Material smelted = SMELT_MAP.get(block.getType());
        if (smelted == null) return; // Not smeltable, leave normal drops

        // Cancel default drops
        block.setType(Material.AIR);

        // Determine drop amount using Fortune-like scaling
        int amount = 1;
        for (int i = 0; i < level; i++) {
            if (RANDOM.nextDouble() < 0.33) { // ~33% chance per level for extra drop
                amount++;
            }
        }

        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smelted, amount));
    }

    /**
     * Returns the smelted version of a block type.
     * Used by the BlockBreakListener to check if the block is smeltable.
     */
    public Material getSmeltedBlock(Material type) {
        return SMELT_MAP.get(type);
    }
}
