package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.level.LevelManager;
import me.Anesthyl.enchants.level.SkillType;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for additional skills: Archery, Fishing, Crafting, Enchanting, Alchemist, and Agility.
 */
public class SkillListener implements Listener {

    private final LevelManager levelManager;

    public SkillListener(LevelManager levelManager) {
        this.levelManager = levelManager;
    }

    /**
     * Archery XP - awarded when hitting entities with arrows
     */
    @EventHandler
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        
        // Award Archery XP based on damage dealt
        double damage = event.getFinalDamage();
        int xpAmount = (int) (damage * SkillType.ARCHERY.getBaseXpPerAction() / 2.0);
        levelManager.addArcheryXP(player, Math.max(xpAmount, SkillType.ARCHERY.getBaseXpPerAction()));
    }

    /**
     * Fishing XP - awarded when successfully catching something
     */
    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
        if (event.getCaught() == null) return;
        
        Player player = event.getPlayer();
        
        // Award more XP for treasure, normal for fish, less for junk
        int xpAmount = SkillType.FISHING.getBaseXpPerAction();
        
        // Try to determine item type from caught entity
        org.bukkit.entity.Item caughtItem = (org.bukkit.entity.Item) event.getCaught();
        ItemStack caught = caughtItem.getItemStack();
        
        if (caught != null) {
            Material type = caught.getType();
            
            // Treasure items (enchanted books, saddles, etc.)
            if (type == Material.ENCHANTED_BOOK || type == Material.NAME_TAG || 
                type == Material.SADDLE || type == Material.NAUTILUS_SHELL) {
                xpAmount *= 3; // Triple XP for treasure
            } 
            // Junk items
            else if (type == Material.LEATHER_BOOTS || type == Material.LEATHER || 
                     type == Material.BONE || type == Material.STRING || 
                     type == Material.TRIPWIRE_HOOK || type == Material.STICK) {
                xpAmount /= 2; // Half XP for junk
            }
        }
        
        levelManager.addFishingXP(player, xpAmount);
    }

    /**
     * Crafting XP - awarded when crafting items
     */
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        ItemStack result = event.getRecipe().getResult();
        int amount = result.getAmount();
        
        // Award XP based on item complexity and amount
        int xpAmount = SkillType.CRAFTING.getBaseXpPerAction();
        
        // More XP for complex items
        Material type = result.getType();
        if (type.toString().contains("DIAMOND") || type.toString().contains("NETHERITE")) {
            xpAmount *= 3;
        } else if (type.toString().contains("IRON") || type.toString().contains("GOLD")) {
            xpAmount *= 2;
        }
        
        // Multiply by amount crafted (capped at 5x to prevent abuse from shift-clicking)
        xpAmount *= Math.min(amount, 5);
        
        levelManager.addCraftingXP(player, xpAmount);
    }

    /**
     * Enchanting XP - awarded when enchanting items
     */
    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        
        // Award XP based on experience level cost
        int levelCost = event.getExpLevelCost();
        int xpAmount = SkillType.ENCHANTING.getBaseXpPerAction() * levelCost;
        
        levelManager.addEnchantingXP(player, xpAmount);
    }

    /**
     * Alchemist XP - awarded when brewing potions
     */
    @EventHandler
    public void onBrew(BrewEvent event) {
        // Try to find the player who initiated the brewing
        // Note: Bukkit doesn't track who started brewing, so we award XP to nearby players
        if (event.getBlock().getWorld().getNearbyEntities(
            event.getBlock().getLocation(), 5, 5, 5,
            entity -> entity instanceof Player).isEmpty()) {
            return;
        }
        
        // Get the closest player to the brewing stand
        Player player = (Player) event.getBlock().getWorld().getNearbyEntities(
            event.getBlock().getLocation(), 5, 5, 5,
            entity -> entity instanceof Player
        ).stream().findFirst().orElse(null);
        
        if (player == null) return;
        
        // Award XP for brewing (multiply by number of potions being brewed)
        int potionCount = 0;
        for (int i = 0; i < 3; i++) {
            if (event.getContents().getItem(i) != null) {
                potionCount++;
            }
        }
        
        int xpAmount = SkillType.ALCHEMIST.getBaseXpPerAction() * potionCount;
        levelManager.addAlchemistXP(player, xpAmount);
    }

    /**
     * Agility XP - awarded when sprinting
     * (Uses a simple distance-based calculation)
     */
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Only award XP if player is sprinting
        if (!player.isSprinting()) return;
        
        // Only award XP if player moved a reasonable distance (prevents standing still spam)
        if (event.getFrom().distance(event.getTo()) < 0.5) return;
        
        // Award a small amount of Agility XP periodically (every ~5 blocks)
        // Use a simple random chance to prevent constant XP spam
        if (Math.random() < 0.02) { // ~2% chance per movement = roughly every 50 movements
            levelManager.addAgilityXP(player, SkillType.AGILITY.getBaseXpPerAction());
        }
    }
}
