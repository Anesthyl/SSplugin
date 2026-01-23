package me.Anesthyl.enchants.enchantsystem;

import me.Anesthyl.enchants.Enchants;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Vein Miner Enchant
 *
 * Dev Notes:
 * - Pickaxe-only enchant.
 * - Breaks connected ore blocks of the same type.
 * - Fully overrides vanilla drops.
 * - Integrates Smelter's Delight cleanly.
 * - Respects vanilla Fortune.
 * - Includes a hard vein cap for safety.
 */
public class VeinMinerEnchant extends CustomEnchant {

    private static final int VEIN_CAP = 64;

    private static final Set<Material> ORES = Set.of(
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.NETHER_GOLD_ORE,
            Material.NETHER_QUARTZ_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE
    );

    private final Enchants plugin;
    private static final Random RANDOM = new Random();

    public VeinMinerEnchant(JavaPlugin plugin) {
        super(plugin, "vein_miner", "§bVein Miner", 1);
        this.plugin = (Enchants) plugin;
    }

    @Override
    public boolean canApply(ItemStack item) {
        return item != null && item.getType().toString().endsWith("_PICKAXE");
    }

    @Override
    public void onHit(Player player, org.bukkit.entity.LivingEntity target, int level) {
        // Not used
    }

    @Override
    public void onBlockBreak(Player player, Block origin, int level) {
        if (!ORES.contains(origin.getType())) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null) return;

        // Find Smelter's Delight if present
        SmeltersDelightEnchant smelter = null;
        for (CustomEnchant e : plugin.getEnchantManager().getItemEnchants(tool).keySet()) {
            if (e instanceof SmeltersDelightEnchant s) {
                smelter = s;
                break;
            }
        }

        int fortune = tool.getEnchantmentLevel(Enchantment.FORTUNE);

        Set<Block> vein = new HashSet<>();
        collectVein(origin, origin.getType(), vein);

        for (Block block : vein) {
            Material dropType = block.getType();
            int amount = 1;

            // Smelter's Delight integration
            if (smelter != null) {
                Material smelted = smelter.getSmeltedResult(dropType);
                if (smelted != null) {
                    dropType = smelted;
                }
            }

            // Fortune logic
            for (int i = 0; i < fortune; i++) {
                if (RANDOM.nextDouble() < 0.33) amount++;
            }

            block.getWorld().dropItemNaturally(
                    block.getLocation(),
                    new ItemStack(dropType, amount)
            );

            block.setType(Material.AIR);

            // Tool durability
            if (tool.getType().getMaxDurability() > 0) {
                tool.damage(1, player);
            }
        }
    }

    private void collectVein(Block block, Material type, Set<Block> collected) {
        if (collected.size() >= VEIN_CAP) return;
        if (collected.contains(block)) return;
        if (block.getType() != type) return;

        collected.add(block);

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;
                    collectVein(block.getRelative(x, y, z), type, collected);
                }
            }
        }
    }

    @Override
    public boolean canAppearOnTable() {
        return true;
    }

    @Override
    public int getTableLevel() {
        return RANDOM.nextDouble() <= 0.05 ? 1 : 0;
    }

    @Override
    public void onTableEnchant(ItemStack item, int level) {
        item.getItemMeta().getPersistentDataContainer()
                .set(getKey(), org.bukkit.persistence.PersistentDataType.INTEGER, level);
    }
}
