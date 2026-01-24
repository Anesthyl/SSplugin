package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import me.Anesthyl.enchants.level.LevelManager;
import me.Anesthyl.enchants.level.SkillType;
import org.bukkit.Material;
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
        
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        Material weaponType = weapon.getType();
        
        // Determine XP amounts based on whether it's a player or mob kill
        int baseXP = event.getEntity() instanceof Player ? 
            SkillType.BRUCE_LEE.getBaseXpPerAction() * 5 : 
            SkillType.BRUCE_LEE.getBaseXpPerAction();
        
        // Award weapon-specific XP based on what was used
        if (weaponType.toString().contains("SWORD")) {
            // Duelist - Swordsmanship
            levelManager.addDuelistXP(killer, SkillType.DUELIST.getBaseXpPerAction());
        } else if (weaponType.toString().contains("AXE")) {
            // Executioner - Axemanship
            levelManager.addExecutionerXP(killer, SkillType.EXECUTIONER.getBaseXpPerAction());
        } else if (weaponType.isAir()) {
            // Bruce-Lee - Hand-to-hand combat (fists only)
            levelManager.addBruceLeeXP(killer, baseXP);
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        
        // Grant Toughness XP for taking damage
        double damageAmount = event.getFinalDamage();
        if (damageAmount >= 1.0) {
            // Award XP based on damage amount (1 XP per half heart)
            int xpAmount = (int) (damageAmount * SkillType.TOUGHNESS.getBaseXpPerAction());
            levelManager.addToughnessXP(player, xpAmount);
        }
    }
}
