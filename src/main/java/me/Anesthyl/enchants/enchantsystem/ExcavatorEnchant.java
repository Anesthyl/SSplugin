package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Excavator Enchant
 *
 * Dev Notes:
 * - Single-level custom enchant (level 1).
 * - Only applies to pickaxes and shovels.
 * - Mines a 3x3 area centered on the target block.
 * - Preserves vanilla mechanics (Fortune, Silk Touch, etc.) via breakNaturally(tool).
 * - Extra durability is deducted per broken block.
 * - Can appear on enchanting table at level 30 only with 10% chance.
 * - Compatible with other custom enchants and vanilla enchant combinations.
 */
public class ExcavatorEnchant extends CustomEnchant {

    private static final List<Material> VALID_TOOLS = Arrays.asList(
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL,
            Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL
    );

    private static final Random RANDOM = new Random();

    public ExcavatorEnchant(JavaPlugin plugin) {
        super(plugin, "excavator", "§bExcavator", 1); // Single level
    }

    @Override
    public boolean canApply(ItemStack item) {
        if (item == null) return false;
        return VALID_TOOLS.contains(item.getType());
    }

    @Override
    public void onHit(Player attacker, org.bukkit.entity.LivingEntity target, int level) {
        // Combat not used
    }

    /**
     * Triggered when a block is broken.
     * Breaks a 3x3 area centered on the target block.
     *
     * @param player Player breaking the block
     * @param block  Block being broken
     * @param level  Level of the enchant (always 1)
     */
    public void onBlockBreak(Player player, Block block, int level) {
        if (!canApply(player.getInventory().getItemInMainHand())) return;

        ItemStack tool = player.getInventory().getItemInMainHand();

        // Center coordinates
        int cx = block.getX();
        int cy = block.getY();
        int cz = block.getZ();
        Material targetType = block.getType();
        var world = block.getWorld();

        int extraBlocksBroken = 0;

        // 3x3 cube around the center
        for (int x = cx - 1; x <= cx + 1; x++) {
            for (int y = cy - 1; y <= cy + 1; y++) {
                for (int z = cz - 1; z <= cz + 1; z++) {
                    Block b = world.getBlockAt(x, y, z);

                    if (b.equals(block)) continue; // Skip center (already broken)
                    if (b.getType() != targetType || b.getType() == Material.AIR) continue;

                    // Break naturally, preserving Fortune/Silk Touch
                    b.breakNaturally(tool);
                    extraBlocksBroken++;
                }
            }
        }

        // Deduct durability for extra blocks mined
        if (tool.getItemMeta() instanceof Damageable damageable) {
            damageable.setDamage(damageable.getDamage() + extraBlocksBroken);
            tool.setItemMeta(damageable);
        }
    }

    /**
     * Can appear on the enchanting table.
     */
    @Override
    public boolean canAppearOnTable() {
        return true;
    }

    /**
     * Table level logic.
     * - Only gives level 1 (the single-level enchant).
     * - 10% chance to appear.
     * - Only obtainable at "level 30" table.
     */
    @Override
    public int getTableLevel() {
        return RANDOM.nextDouble() <= 0.10 ? 1 : 0;
    }

    /**
     * Apply the enchant from the table to the item.
     * Stores level in PersistentDataContainer.
     */
    @Override
    public void onTableEnchant(ItemStack item, int level) {
        if (item == null || !item.hasItemMeta() || level <= 0) return;

        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer()
                .set(getKey(), org.bukkit.persistence.PersistentDataType.INTEGER, level);
        item.setItemMeta(meta);
    }
}
