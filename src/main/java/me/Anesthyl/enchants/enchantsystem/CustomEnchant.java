package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class CustomEnchant {

    protected final NamespacedKey key;
    protected final String displayName;
    protected final int maxLevel;

    public CustomEnchant(JavaPlugin plugin, String id, String displayName, int maxLevel) {
        this.key = new NamespacedKey(plugin, id);
        this.displayName = displayName;
        this.maxLevel = maxLevel;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    // Returns true if this item can get the enchant
    public abstract boolean canApply(ItemStack item);

    // Called when the player hits a target
    public void onHit(Player attacker, LivingEntity target, int level) {}
}
