package me.Anesthyl.enchants.enchantsystem;

import me.Anesthyl.enchants.Enchants;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

/**
 * Vein Miner Enchant
 *
 * Dev Notes:
 * - Pickaxe-only, single-level enchant.
 * - Breaks all connected ore blocks of the same type.
 * - Works with Smelter's Delight (drops smelted items) if present.
 * - Works with vanilla Fortune (LOOT_BONUS_BLOCKS) if present.
 * - Durability is consumed per block broken.
 * - Rare: Level 30 table only, 5% chance.
 */
public class VeinMinerEnchant extends CustomEnchant {

    private static final Set<Material> ORES = Set.of(
            Material.IRON_ORE, Material.GOLD_ORE, Material.COPPER_ORE,
            Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE,
            Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.LAPIS_ORE,
            Material.REDSTONE_ORE
    );

    private static final Random RANDOM = new Random();
    private final Enchants plugin;

    public VeinMinerEnchant(JavaPlugin plugin) {
        super(plugin, "vein_miner", "§bVein Miner", 1);
        this.plugin = (Enchants) plugin; // Keep plugin instance for manager access
    }

    @Override
    public boolean canApply(ItemStack item) {
        return item != null && item.getType().toString().endsWith("_PICKAXE");
    }

    @Override
    public void onHit(Player player, org.bukkit.entity.LivingEntity target, int level) {
        // Not used in combat
    }

    @Override
    public void onBlockBreak(Player player, Block block, int level) {
        if (!ORES.contains(block.getType())) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null) return;

        // ✅ Correct: Get all enchants on the tool from EnchantManager
        SmeltersDelightEnchant smelter = null;
        Map<CustomEnchant, Integer> enchants = plugin.getEnchantManager().getItemEnchants(tool);
        for (CustomEnchant e : enchants.keySet()) {
            if (e instanceof SmeltersDelightEnchant s) smelter = s;
        }

        // Determine Fortune level safely
        int fortuneLevel = tool.getEnchantmentLevel(Enchantment.FORTUNE);

        // Collect connected vein blocks
        Set<Block> vein = new HashSet<>();
        collectVein(block, vein, block.getType());

        for (Block b : vein) {
            Material dropType = b.getType();
            int amount = 1;

            // Apply Smelter's Delight if present
            if (smelter != null) {
                Material smelted = smelter.getSmeltedBlock(dropType);
                if (smelted != null) dropType = smelted;
            }

            // Apply vanilla Fortune-like logic
            if (fortuneLevel > 0) {
                for (int i = 0; i < fortuneLevel; i++) {
                    if (RANDOM.nextDouble() < 0.33) amount++;
                }
            }

            // Drop items naturally
            b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(dropType, amount));
            b.setType(Material.AIR);

            // Damage tool
            if (tool.getType().getMaxDurability() > 0) {
                tool.damage(1, player);
            }
        }
    }

    /**
     * Recursively collects all connected blocks of the same type
     */
    private void collectVein(Block block, Set<Block> collected, Material type) {
        if (collected.contains(block) || block.getType() != type) return;
        collected.add(block);

        int[][] directions = {
                {1, 0, 0}, {-1, 0, 0},
                {0, 1, 0}, {0, -1, 0},
                {0, 0, 1}, {0, 0, -1}
        };

        for (int[] d : directions) {
            collectVein(block.getRelative(d[0], d[1], d[2]), collected, type);
        }
    }

    @Override
    public boolean canAppearOnTable() {
        return true;
    }

    @Override
    public int getTableLevel() {
        return RANDOM.nextDouble() <= 0.05 ? 1 : 0; // 5% chance
    }

    @Override
    public void onTableEnchant(ItemStack item, int level) {
        if (item == null || level <= 0) return;
        item.getItemMeta().getPersistentDataContainer()
                .set(getKey(), org.bukkit.persistence.PersistentDataType.INTEGER, level);
    }
}
