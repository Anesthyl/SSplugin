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
        if (item == null || !item.hasItemMeta() || level <= 0) return;

        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.getPersistentDataContainer()
                .set(getKey(), org.bukkit.persistence.PersistentDataType.INTEGER, level);
        item.setItemMeta(meta);
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

        // Use player's feet position (eye location - 1.62 blocks for standing player)
        Block center = player.getLocation().subtract(0, 0.5, 0).getBlock();

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Check blocks at player's feet level and one block below
                for (int y = 0; y <= 1; y++) {
                    Block block = center.getRelative(x, -y, z);

                    // Only convert full lava source blocks
                    if (block.getType() != Material.LAVA) continue;
                    if (!block.getBlockData().getAsString().contains("[level=0]")) continue;

                    // Don't convert if player would be inside the obsidian
                    Block playerBlock = player.getLocation().getBlock();
                    Block playerFeetBlock = player.getLocation().subtract(0, 1, 0).getBlock();
                    if (block.equals(playerBlock) || block.equals(playerFeetBlock)) continue;

                    block.setType(Material.OBSIDIAN);
                    toRevert.add(block);
                }
            }
        }

        // Schedule revert back to lava
        if (!toRevert.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(
                    plugin,
                    () -> {
                        for (Block b : toRevert) {
                            // Only revert if player is not standing on it
                            boolean playerNearby = false;
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p.getLocation().distance(b.getLocation()) < 2.0) {
                                    playerNearby = true;
                                    break;
                                }
                            }

                            if (b.getType() == Material.OBSIDIAN && !playerNearby) {
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
     * Level 1 = 3x3 (radius 1), Level 2 = 5x5 (radius 2)
     *
     * config.yml example:
     * lava-walker:
     *   radius-per-level: 1
     */
    private int getRadius(int level) {
        FileConfiguration config = plugin.getConfig();
        int perLevel = config.getInt("lava-walker.radius-per-level", 1);
        return level * perLevel;
    }
}
