package me.Anesthyl.enchants.listeners;

import me.Anesthyl.enchants.enchantsystem.CustomEnchant;
import me.Anesthyl.enchants.enchantsystem.EnchantManager;
import me.Anesthyl.enchants.util.EnchantUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * EnchantTableListener
 *
 * Dev Notes:
 * - Mimics vanilla enchanting behavior.
 * - Rolls all eligible enchants but applies ONLY ONE.
 * - Respects enchant rarity via getTableLevel().
 * - Prevents multiple custom enchants per table use.
 * - Compatibility rules enforced via EnchantUtil.
 */
public class EnchantTableListener implements Listener {

    private final EnchantManager enchantManager;
    private final Random random = new Random();

    public EnchantTableListener(EnchantManager enchantManager) {
        this.enchantManager = enchantManager;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        ItemStack item = event.getItem();

        List<EnchantRoll> rolled = new ArrayList<>();

        // Roll all possible enchants
        for (CustomEnchant enchant : enchantManager.getEnchants()) {
            if (!enchant.canApply(item)) continue;
            if (!enchant.canAppearOnTable()) continue;

            int level = enchant.getTableLevel();
            if (level > 0) {
                rolled.add(new EnchantRoll(enchant, level));
            }
        }

        // Nothing rolled → do nothing
        if (rolled.isEmpty()) return;

        // Pick ONE enchant result
        EnchantRoll chosen = rolled.get(random.nextInt(rolled.size()));

        EnchantUtil.applyEnchant(
                item,
                chosen.enchant,
                chosen.level,
                enchantManager
        );
    }

    /**
     * Simple record to store a rolled enchant + level
     */
    private static class EnchantRoll {
        final CustomEnchant enchant;
        final int level;

        EnchantRoll(CustomEnchant enchant, int level) {
            this.enchant = enchant;
            this.level = level;
        }
    }
}
