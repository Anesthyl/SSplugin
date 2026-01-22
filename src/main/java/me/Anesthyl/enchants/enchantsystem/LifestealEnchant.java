package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class LifestealEnchant extends CustomEnchant {

    public LifestealEnchant(JavaPlugin plugin) {
        super(plugin, "lifesteal", "§cLifesteal", 3);
    }

    @Override
    public boolean canApply(ItemStack item) {
        // Only swords
        return item != null && item.getType().toString().endsWith("_SWORD");
    }

    @Override
    public void onHit(Player attacker, LivingEntity target, int level) {
        if (target == null || attacker == null) return;

        // Heal 0.5 hearts per level
        double heal = 0.25 * level;
        double newHealth = Math.min(attacker.getHealth() + heal, attacker.getMaxHealth());

        attacker.setHealth(newHealth);
    }
}
