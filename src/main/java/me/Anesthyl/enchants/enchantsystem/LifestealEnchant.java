package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

/**
 * Lifesteal Enchant
 *
 * - Heals the player on hit.
 * - Sword only.
 * - Table rarity: 15%.
 * - Fully version-safe.
 */
public class LifestealEnchant extends CustomEnchant {

    private static final Random RANDOM = new Random();

    public LifestealEnchant(JavaPlugin plugin) {
        super(plugin, "lifesteal", "§aLifesteal", 3);
    }

    @Override
    public boolean canApply(ItemStack item) {
        return item != null && item.getType().toString().endsWith("_SWORD");
    }

    @Override
    public void onHit(Player attacker, LivingEntity target, int level) {
        double heal = 2.0 * level; // Scales with enchant level
        double maxHealth = 20.0; // default vanilla max health

        if (attacker.getAttribute(Attribute.MAX_HEALTH) != null) {
            maxHealth = attacker.getAttribute(Attribute.MAX_HEALTH).getValue();
        }

        attacker.setHealth(Math.min(attacker.getHealth() + heal, maxHealth));
    }

    @Override
    public void onBlockBreak(Player player, org.bukkit.block.Block block, int level) {
        // Not used
    }

    @Override
    public boolean canAppearOnTable() {
        return true;
    }

    @Override
    public int getTableLevel() {
        return RANDOM.nextDouble() <= 0.15 ? 1 + RANDOM.nextInt(getMaxLevel()) : 0;
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
