package me.Anesthyl.enchants.achievements;

import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Manages achievements for custom enchants
 */
public class AchievementManager {

    private final JavaPlugin plugin;
    private final EnchantManager enchantManager;
    private final NamespacedKey firstLegendaryKey;
    private final NamespacedKey legendaryCollectorKey;

    public AchievementManager(JavaPlugin plugin, EnchantManager enchantManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
        this.firstLegendaryKey = new NamespacedKey(plugin, "achievement_first_legendary");
        this.legendaryCollectorKey = new NamespacedKey(plugin, "achievement_legendary_collector");
    }

    /**
     * Check if player has "First Legendary" achievement
     */
    public boolean hasFirstLegendary(Player player) {
        return player.getPersistentDataContainer()
                .has(firstLegendaryKey, PersistentDataType.BYTE);
    }

    /**
     * Grant "First Legendary" achievement
     */
    public void grantFirstLegendary(Player player) {
        if (hasFirstLegendary(player)) return;

        player.getPersistentDataContainer()
                .set(firstLegendaryKey, PersistentDataType.BYTE, (byte) 1);

        // Announce achievement
        Bukkit.broadcastMessage("§6§l✦ §e" + player.getName() + " §7obtained their §d§lFIRST LEGENDARY ENCHANTMENT! §6§l✦");
        player.sendMessage("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("§6§l          ACHIEVEMENT UNLOCKED!");
        player.sendMessage("");
        player.sendMessage("§d§l      First Legendary Enchantment");
        player.sendMessage("§7  Obtain an item with a legendary enchant");
        player.sendMessage("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }

    /**
     * Check if player has "Legendary Collector" achievement
     */
    public boolean hasLegendaryCollector(Player player) {
        return player.getPersistentDataContainer()
                .has(legendaryCollectorKey, PersistentDataType.BYTE);
    }

    /**
     * Grant "Legendary Collector" achievement
     */
    public void grantLegendaryCollector(Player player) {
        if (hasLegendaryCollector(player)) return;

        player.getPersistentDataContainer()
                .set(legendaryCollectorKey, PersistentDataType.BYTE, (byte) 1);

        // Announce achievement
        Bukkit.broadcastMessage("§6§l✦✦✦ §e" + player.getName() + " §7is now a §d§lLEGENDARY COLLECTOR! §6§l✦✦✦");
        player.sendMessage("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        player.sendMessage("§6§l          ACHIEVEMENT UNLOCKED!");
        player.sendMessage("");
        player.sendMessage("§d§l        Legendary Collector");
        player.sendMessage("§7   Have 3 legendary enchants on one item");
        player.sendMessage("§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
    }
}
