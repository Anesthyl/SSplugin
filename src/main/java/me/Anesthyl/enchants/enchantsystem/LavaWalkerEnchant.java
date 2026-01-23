package me.Anesthyl.enchants.enchantsystem;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.Random;

/**
 * Lava Walker Enchant
 *
 * Dev Notes:
 * - Boots-only enchant.
 * - Converts lava under player's feet to obsidian.
 * - Grants temporary movement speed boost while standing on obsidian.
 * - Single-level enchant (level 1).
 * - Table rarity: 20% chance, only at level 20+ tables.
 * - Fully compatible with other custom and vanilla enchants.
 */
public class LavaWalkerEnchant extends CustomEnchant {

    private static final Random RANDOM = new Random();
    private final NamespacedKey modifierKey;
    private final AttributeModifier speedBoost;

    public LavaWalkerEnchant(JavaPlugin plugin) {
        super(plugin, "lava_walker", "§6Lava Walker", 1); // single level
        this.modifierKey = new NamespacedKey(plugin, "lava_walker_speed");
        this.speedBoost = new AttributeModifier(
                modifierKey,
                0.1, // 10% speed boost
                AttributeModifier.Operation.ADD_SCALAR,
                EquipmentSlotGroup.FEET
        );
    }

    @Override
    public boolean canApply(ItemStack item) {
        return item != null && item.getType().toString().endsWith("_BOOTS");
    }

    @Override
    public void onHit(Player player, org.bukkit.entity.LivingEntity target, int level) {
        // Passive boots enchant, not used in combat
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
        // Only appear on level 20+ tables with 20% chance
        return RANDOM.nextDouble() <= 0.20 ? 1 : 0;
    }

    @Override
    public void onTableEnchant(ItemStack item, int level) {
        if (item == null || level <= 0) return;

        item.getItemMeta().getPersistentDataContainer()
                .set(getKey(), org.bukkit.persistence.PersistentDataType.INTEGER, level);
    }

    /**
     * Called when a player moves. Converts lava to obsidian and applies speed boost.
     *
     * @param event PlayerMoveEvent
     * @param level Enchant level (always 1)
     */
    public void onPlayerMove(PlayerMoveEvent event, int level) {
        Player player = event.getPlayer();
        ItemStack boots = player.getInventory().getBoots();
        if (boots == null || !canApply(boots)) return;

        Block under = player.getLocation().subtract(0, 1, 0).getBlock();

        if (under.getType() == Material.LAVA) {
            under.setType(Material.OBSIDIAN);

            // Apply speed boost if not already present
            if (!player.getAttribute(Attribute.MOVEMENT_SPEED).getModifiers().contains(speedBoost)) {
                player.getAttribute(Attribute.MOVEMENT_SPEED).addModifier(speedBoost);
            }
        } else {
            // Remove speed boost if player is off obsidian
            if (player.getAttribute(Attribute.MOVEMENT_SPEED).getModifiers().contains(speedBoost)) {
                player.getAttribute(Attribute.MOVEMENT_SPEED).removeModifier(speedBoost);
            }
        }
    }
}
