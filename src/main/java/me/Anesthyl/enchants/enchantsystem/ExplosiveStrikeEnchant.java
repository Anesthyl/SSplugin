package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ExplosiveStrikeEnchant extends CustomEnchant {

    public ExplosiveStrikeEnchant(JavaPlugin plugin) {
        super(plugin, "explosive_strike", "§6Explosive Strike", 2);
    }

    @Override
    public boolean canApply(ItemStack item) {
        return item.getType().toString().endsWith("_SWORD");
    }

    @Override
    public void onHit(Player attacker, LivingEntity target, int level) {
        Location loc = target.getLocation();
        loc.getWorld().createExplosion(loc, level, false, false); // explosion power = level
    }
}
