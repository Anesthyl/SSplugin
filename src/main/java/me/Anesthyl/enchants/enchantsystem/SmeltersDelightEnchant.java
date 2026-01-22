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
 * Smelter's Delight Enchant (version-safe)
 *
 * - Converts mined ores into smelted ingots.
 * - Fortune-compatible: scales naturally with the Fortune level on the pickaxe.
 * - Applies only to pickaxes.
 * - Table rarity: 10% chance at level 30.
 * - Internal effect level: 1-3 (extra drop scaling)
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
        // Not used in combat
    }

    @Override
    public void onBlockBreak(Player player, Block block, int level) {
        Material smelted = SMELT_MAP.get(block.getType());
        if (smelted == null) return;

        // Cancel normal drops
        block.setType(Material.AIR);

        // Version-safe Fortune: check persistent enchantments manually
        ItemStack tool = player.getInventory().getItemInMainHand();
        int fortuneLevel = 0;
        if (tool != null && tool.getEnchantments() != null) {
            fortuneLevel = tool.getEnchantments().getOrDefault(org.bukkit.enchantments.Enchantment.getByName("FORTUNE"), 0);
        }

        // Drop calculation: base + fortune + level scaling
        int amount = 1 + RANDOM.nextInt(level + 1); // internal level scaling
        for (int i = 0; i < fortuneLevel; i++) {
            if (RANDOM.nextDouble() < 0.33) amount++; // vanilla-style fortune chance
        }

        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smelted, amount));
    }

    @Override
    public boolean canAppearOnTable() {
        return true;
    }

    @Override
    public int getTableLevel() {
        // 10% chance to appear
        if (RANDOM.nextDouble() > 0.10) return 0;

        return 1 + RANDOM.nextInt(getMaxLevel());
    }

    @Override
    public void onTableEnchant(ItemStack item, int level) {
        item.getItemMeta().getPersistentDataContainer()
                .set(getKey(), org.bukkit.persistence.PersistentDataType.INTEGER, level);
    }
}
