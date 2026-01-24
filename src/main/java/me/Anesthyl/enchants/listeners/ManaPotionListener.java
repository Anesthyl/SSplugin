package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.spell.ManaManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

/**
 * Handles mana potion consumption to restore player mana.
 */
public class ManaPotionListener implements Listener {
    private final ManaManager manaManager;
    private static final double MANA_RESTORE_AMOUNT = 50.0;

    public ManaPotionListener(ManaManager manaManager) {
        this.manaManager = manaManager;
    }

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item.getType() != Material.POTION) {
            return;
        }

        if (!(item.getItemMeta() instanceof PotionMeta meta)) {
            return;
        }

        // Check if this is a mana potion by checking the display name
        if (meta.hasDisplayName()) {
            Component displayName = meta.displayName();
            if (displayName != null && isManaPotion(displayName)) {
                // Restore mana
                manaManager.addMana(player, MANA_RESTORE_AMOUNT);

                // Send feedback message
                player.sendMessage(
                    Component.text("✦ ", NamedTextColor.AQUA)
                        .append(Component.text("Restored ", NamedTextColor.GRAY))
                        .append(Component.text(String.format("%.0f", MANA_RESTORE_AMOUNT), NamedTextColor.AQUA))
                        .append(Component.text(" mana!", NamedTextColor.GRAY))
                );
            }
        }
    }

    /**
     * Checks if the given display name matches the mana potion name.
     */
    private boolean isManaPotion(Component component) {
        String plainText = stripFormatting(component);
        return plainText.contains("Mana Potion");
    }

    /**
     * Strips formatting from a Component to get plain text.
     */
    private String stripFormatting(Component component) {
        if (component == null) {
            return "";
        }
        // Convert to plain text for comparison
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(component);
    }

    /**
     * Creates a mana potion ItemStack.
     * This method can be used by the brewing listener to create the result.
     */
    public static ItemStack createManaPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        // Set base potion type to water (clear potion)
        meta.setBasePotionType(PotionType.WATER);

        // Set custom color to bright blue/cyan
        meta.setColor(org.bukkit.Color.fromRGB(0, 191, 255));

        // Set display name
        meta.displayName(
            Component.text("Mana Potion", NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false)
        );

        potion.setItemMeta(meta);
        return potion;
    }
}
