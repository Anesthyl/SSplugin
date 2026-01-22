package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Explosive Strike Enchant
 *
 * - Causes small explosions on hitting a target.
 * - Applies to axes and swords.
 * - Table eligible, rare, levels 1-3.
 */
public class ExplosiveStrikeEnchant extends CustomEnchant {

    public ExplosiveStrikeEnchant(JavaPlugin plugin) {
        super(plugin, "explosive_strike", "§cExplosive Strike", 3);
    }

    @Override
    public boolean canApply(ItemStack item) {
        String type = item.getType().toString();
        return type.endsWith("_SWORD") || type.endsWith("_AXE");
    }

    @Override
    public void onHit(Player attacker, LivingEntity target, int level) {
        Location loc = target.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        // Explosion radius scales with level
        float power = 0.5f * level;
        world.createExplosion(loc.getX(), loc.getY(), loc.getZ(), power, false, false); // no fire, no block damage
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
        // 12% chance to appear on table
        if (Math.random() > 0.12) return 0;
        return 1 + new java.util.Random().nextInt(getMaxLevel());
    }

    @Override
    public void onTableEnchant(ItemStack item, int level) {
        item.getItemMeta().getPersistentDataContainer().set(getKey(), org.bukkit.persistence.PersistentDataType.INTEGER, level);
    }
}
