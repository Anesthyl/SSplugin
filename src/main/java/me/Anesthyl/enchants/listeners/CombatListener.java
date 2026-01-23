package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import me.Anesthyl.enchants.level.LevelManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class CombatListener implements Listener {

    private final EnchantManager enchantManager;
    private final LevelManager levelManager;

    public CombatListener(EnchantManager enchantManager, LevelManager levelManager) {
        this.enchantManager = enchantManager;
        this.levelManager = levelManager;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        Map<CustomEnchant, Integer> enchants = enchantManager.getItemEnchants(weapon);

        enchants.forEach((enchant, level) -> enchant.onHit(player, target, level));
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        
        // Grant XP for kills
        if (event.getEntity() instanceof Player) {
            levelManager.addPlayerKillXP(killer);
        } else {
            levelManager.addMobKillXP(killer);
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        // Grant XP for taking damage (small amount per 0.5 hearts)
        double damageAmount = event.getFinalDamage();
        if (damageAmount >= 1.0) {
            levelManager.addDamageXP(player);
        }
    }
}
