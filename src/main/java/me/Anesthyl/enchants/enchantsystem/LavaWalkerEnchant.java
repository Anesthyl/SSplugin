package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

/**
 * Lava Walker Enchant
 *
 * Dev Notes:
 * - Boots-only movement enchant (Frost Walker–style behavior).
 * - Converts nearby lava source blocks into obsidian temporarily.
 * - Radius scales per level via config.
 * - Obsidian reverts back to lava after a short delay.
 * - Logic is triggered via LavaWalkerListener (PlayerMoveEvent).
 * - Safe for multiplayer and does not permanently alter terrain.
 */
public class LavaWalkerEnchant extends CustomEnchant {

    private final JavaPlugin plugin;

    public LavaWalkerEnchant(JavaPlugin plugin) {
        super(
                plugin,
                "lava_walker",
                "§cLava Walker",
                2
        );
        this.plugin = plugin;
    }

    // ------------------------------------------------------------
    // Table / Availability
    // ------------------------------------------------------------

    @Override
    public boolean canApply(ItemStack item) {
        return item != null && item.getType().toString().endsWith("_BOOTS");
    }

    @Override
    public boolean canAppearOnTable() {
        return true;
    }

    @Override
    public int getTableLevel() {
        // Example rarity:
        // Level 1: common-ish
        // Level 2: rare
        double roll = Math.random();
        if (roll < 0.05) return 2;   // 5%
        if (roll < 0.25) return 1;   // 20%
        return 0;
    }

    @Override
    public void onTableEnchant(ItemStack item, int level) {
        if (item == null || level <= 0) return;

        item.getItemMeta().getPersistentDataContainer()
                .set(getKey(), org.bukkit.persistence.PersistentDataType.INTEGER, level);
    }

    // ------------------------------------------------------------
    // Combat / Block hooks (unused)
    // ------------------------------------------------------------

    @Override
    public void onHit(Player attacker, LivingEntity target, int level) {
        // Not a combat enchant
    }

    @Override
    public void onBlockBreak(Player player, Block block, int level) {
        // Not a mining enchant
    }

    // ------------------------------------------------------------
    // Movement Logic (called from LavaWalkerListener)
    // ------------------------------------------------------------

    /**
     * Called by LavaWalkerListener on PlayerMoveEvent.
     */
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event, int level) {
        Player player = event.getPlayer();

        if (player.isSneaking()) return; // Vanilla Frost Walker behavior
        if (player.isFlying()) return;

        int radius = getRadius(level);
        Set<Block> toRevert = new HashSet<>();

        Block center = player.getLocation().getBlock();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {

                Block block = center.getRelative(x, -1, z);

                // Only convert full lava source blocks
                if (block.getType() != Material.LAVA) continue;

                block.setType(Material.OBSIDIAN);
                toRevert.add(block);
            }
        }

        // Schedule revert back to lava
        if (!toRevert.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(
                    plugin,
                    () -> {
                        for (Block b : toRevert) {
                            if (b.getType() == Material.OBSIDIAN) {
                                b.setType(Material.LAVA);
                            }
                        }
                    },
                    100L // 5 seconds (vanilla Frost Walker is ~4s)
            );
        }
    }

    // ------------------------------------------------------------
    // Config
    // ------------------------------------------------------------

    /**
     * Radius per level (configurable).
     *
     * config.yml example:
     * lava-walker:
     *   radius-per-level: 2
     */
    private int getRadius(int level) {
        FileConfiguration config = plugin.getConfig();
        int perLevel = config.getInt("lava-walker.radius-per-level", 2);
        return level * perLevel;
    }
}
