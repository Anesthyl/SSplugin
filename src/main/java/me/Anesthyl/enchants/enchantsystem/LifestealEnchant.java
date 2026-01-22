package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Lifesteal Enchant
 *
 * - Heals player on hitting a target.
 * - Works in combat only.
 * - Applies to swords.
 * - Can appear on enchantment table, level 1-3, uncommon.
 */
public class LifestealEnchant extends CustomEnchant {

    public LifestealEnchant(JavaPlugin plugin) {
        super(plugin, "lifesteal", "§aLifesteal", 3);
    }

    @Override
    public boolean canApply(ItemStack item) {
        return item != null && item.getType().toString().endsWith("_SWORD");
    }

    @Override
    public void onHit(Player attacker, LivingEntity target, int level) {
        // Heal player: level * 2 HP per hit
        double heal = 2.0 * level;
        double maxHealth = attacker.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
        double newHealth = Math.min(attacker.getHealth() + heal, maxHealth);
        attacker.setHealth(newHealth);
    }

    @Override
    public void onBlockBreak(Player player, org.bukkit.block.Block block, int level) {
        // Not used
    }

    @Override
    public boolean canAppearOnTable() {
        return true; // Rare, table-eligible
    }

    @Override
    public int getTableLevel() {
        // 15% base chance
        if (Math.random() > 0.15) return 0;
        return 1 + new java.util.Random().nextInt(getMaxLevel());
    }

    @Override
    public void onTableEnchant(ItemStack item, int level) {
        item.getItemMeta().getPersistentDataContainer().set(getKey(), org.bukkit.persistence.PersistentDataType.INTEGER, level);
    }
}
