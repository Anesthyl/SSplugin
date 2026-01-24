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
            Material oreType = block.getType();
            Material dropType = getOreDrop(oreType);
            int amount = getDropAmount(oreType, fortune);

            // Smelter's Delight integration - convert ore drop to smelted result
            if (smelter != null) {
                Material smelted = smelter.getSmeltedResult(oreType);
                if (smelted != null) {
                    dropType = smelted;
                }
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

    /**
     * Get the proper ore drop for each ore type (mimics vanilla behavior)
     */
    private Material getOreDrop(Material ore) {
        return switch (ore) {
            case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.RAW_IRON;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE -> Material.RAW_GOLD;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.RAW_COPPER;
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> Material.DIAMOND;
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> Material.EMERALD;
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> Material.LAPIS_LAZULI;
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> Material.REDSTONE;
            case NETHER_GOLD_ORE -> Material.GOLD_NUGGET;
            case NETHER_QUARTZ_ORE -> Material.QUARTZ;
            default -> ore; // Fallback
        };
    }

    /**
     * Calculate drop amount with Fortune scaling (vanilla-style)
     */
    private int getDropAmount(Material ore, int fortune) {
        int baseAmount = 1;

        // Some ores drop more base items (like lapis and redstone)
        if (ore == Material.LAPIS_ORE || ore == Material.DEEPSLATE_LAPIS_ORE) {
            baseAmount = 4 + RANDOM.nextInt(5); // 4-8 lapis
        } else if (ore == Material.REDSTONE_ORE || ore == Material.DEEPSLATE_REDSTONE_ORE) {
            baseAmount = 4 + RANDOM.nextInt(2); // 4-5 redstone
        } else if (ore == Material.NETHER_GOLD_ORE) {
            baseAmount = 2 + RANDOM.nextInt(4); // 2-5 gold nuggets
        }

        // Apply Fortune multiplier (vanilla Fortune behavior)
        if (fortune > 0) {
            // Fortune increases max possible drops
            int maxBonus = fortune;
            int bonus = RANDOM.nextInt(maxBonus + 1);
            baseAmount += bonus;
        }

        return baseAmount;
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
        if (item == null || !item.hasItemMeta() || level <= 0) return;

        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer()
                .set(getKey(), org.bukkit.persistence.PersistentDataType.INTEGER, level);
        item.setItemMeta(meta);
    }
}
