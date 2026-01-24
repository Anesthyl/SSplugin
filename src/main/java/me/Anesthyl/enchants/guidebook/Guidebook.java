package me.Anesthyl.enchants.guidebook;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates the legendary guidebook given to players on first join.
 */
public class Guidebook {
    private final JavaPlugin plugin;
    private final NamespacedKey guidebookKey;

    public Guidebook(JavaPlugin plugin) {
        this.plugin = plugin;
        this.guidebookKey = new NamespacedKey(plugin, "legendary_guidebook");
    }

    /**
     * Creates the legendary guidebook item.
     */
    public ItemStack createGuidebook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        // Set book metadata
        meta.setTitle("Legends of the Arcane");
        meta.setAuthor("The Ancient Order");

        // Mark as guidebook using PDC
        meta.getPersistentDataContainer().set(guidebookKey,
            org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);

        // Add pages
        meta.addPages(createIntroPage());
        meta.addPages(createLorePage());
        meta.addPages(createEnchantmentsPage1());
        meta.addPages(createEnchantmentsPage2());
        meta.addPages(createEnchantmentsPage3());
        meta.addPages(createSpellsPage1());
        meta.addPages(createSpellsPage2());
        meta.addPages(createManaPotionPage());
        meta.addPages(createBackpackPage());
        meta.addPages(createSpellWorkstationPage());

        book.setItemMeta(meta);
        return book;
    }

    private Component createIntroPage() {
        return Component.text()
            .append(Component.text("═══════════════\n", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .append(Component.text("LEGENDS\n", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text("OF THE\n", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text("ARCANE\n", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text("═══════════════\n\n", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .append(Component.text("Welcome, Seeker.\n\n", NamedTextColor.DARK_GRAY))
            .append(Component.text("You hold in your hands the collected wisdom of the Ancient Order—secrets of enchantments lost to time and spells of unimaginable power.\n\n", NamedTextColor.BLACK))
            .append(Component.text("May this guide lead you to greatness.", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC))
            .build();
    }

    private Component createLorePage() {
        return Component.text()
            .append(Component.text("═ THE LEGEND ═\n\n", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .append(Component.text("Long ago, before the age of kingdoms, the ", NamedTextColor.BLACK))
            .append(Component.text("Ancient Order ", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .append(Component.text("discovered rifts between worlds. Through these tears in reality, they channeled raw magical energy into crystals, armor, and weapons.\n\n", NamedTextColor.BLACK))
            .append(Component.text("These enchantments were not mere tricks—they were ", NamedTextColor.BLACK))
            .append(Component.text("fragments of reality itself", NamedTextColor.GOLD, TextDecoration.ITALIC))
            .append(Component.text(", bound to mortal tools.\n\n", NamedTextColor.BLACK))
            .append(Component.text("But the Order vanished, leaving only their knowledge behind...", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC))
            .build();
    }

    private Component createEnchantmentsPage1() {
        return Component.text()
            .append(Component.text("═ COMBAT ARTS ═\n\n", NamedTextColor.DARK_RED, TextDecoration.BOLD))
            .append(Component.text("⚔ Lifesteal\n", NamedTextColor.RED, TextDecoration.BOLD))
            .append(Component.text("Drain the life force of your enemies to sustain yourself in battle.\n\n", NamedTextColor.BLACK))
            .append(Component.text("⚡ Explosive Strike\n", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text("Channel explosive energy through your weapon, devastating all nearby foes.\n\n", NamedTextColor.BLACK))
            .append(Component.text("✧ XP Boost\n", NamedTextColor.GREEN, TextDecoration.BOLD))
            .append(Component.text("Amplify experience gained from all activities. Knowledge flows faster to those who seek it.", NamedTextColor.BLACK))
            .build();
    }

    private Component createEnchantmentsPage2() {
        return Component.text()
            .append(Component.text("═ MINING ARTS ═\n\n", NamedTextColor.GRAY, TextDecoration.BOLD))
            .append(Component.text("⛏ Vein Miner\n", NamedTextColor.DARK_AQUA, TextDecoration.BOLD))
            .append(Component.text("Shatter entire veins of ore with a single strike. The earth yields to your will.\n\n", NamedTextColor.BLACK))
            .append(Component.text("🔥 Smelter's Delight\n", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text("Ores melt instantly upon mining, refined by pure heat channeled through your tool.\n\n", NamedTextColor.BLACK))
            .append(Component.text("⚒ Excavator\n", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
            .append(Component.text("Dig 3x3 areas instantly. The ground trembles before you.", NamedTextColor.BLACK))
            .build();
    }

    private Component createEnchantmentsPage3() {
        return Component.text()
            .append(Component.text("═ MYSTIC ARTS ═\n\n", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
            .append(Component.text("🌋 Lava Walker\n", NamedTextColor.RED, TextDecoration.BOLD))
            .append(Component.text("Walk upon molten lava as if it were solid ground. The nether bows to you.\n\n", NamedTextColor.BLACK))
            .append(Component.text("🦘 Donald Jump\n", NamedTextColor.YELLOW, TextDecoration.BOLD))
            .append(Component.text("Leap to great heights with enhanced jumping power. Defy gravity itself.\n\n", NamedTextColor.BLACK))
            .append(Component.text("✨ Shiny\n", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text("Armor blessed with fortune—creatures drop more treasure when slain by your hand.", NamedTextColor.BLACK))
            .build();
    }

    private Component createSpellsPage1() {
        return Component.text()
            .append(Component.text("═══ SPELLS ═══\n\n", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .append(Component.text("🔥 Fireball\n", NamedTextColor.RED, TextDecoration.BOLD))
            .append(Component.text("Launch blazing fire.\n", NamedTextColor.DARK_GRAY))
            .append(Component.text("Cost: 20-30 mana\n\n", NamedTextColor.BLUE))
            .append(Component.text("⚡ Lightning Strike\n", NamedTextColor.YELLOW, TextDecoration.BOLD))
            .append(Component.text("Call down lightning.\n", NamedTextColor.DARK_GRAY))
            .append(Component.text("Cost: 40-60 mana\n\n", NamedTextColor.BLUE))
            .append(Component.text("🌀 Wind Blast\n", NamedTextColor.AQUA, TextDecoration.BOLD))
            .append(Component.text("Knock back enemies.\n", NamedTextColor.DARK_GRAY))
            .append(Component.text("Cost: 20-30 mana", NamedTextColor.BLUE))
            .build();
    }

    private Component createSpellsPage2() {
        return Component.text()
            .append(Component.text("═ MORE SPELLS ═\n\n", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .append(Component.text("🌀 Teleport\n", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
            .append(Component.text("Teleport to target.\n", NamedTextColor.DARK_GRAY))
            .append(Component.text("Cost: 25-35 mana\n\n", NamedTextColor.BLUE))
            .append(Component.text("🛡 Nether Shield\n", NamedTextColor.DARK_RED, TextDecoration.BOLD))
            .append(Component.text("Summon protection.\n", NamedTextColor.DARK_GRAY))
            .append(Component.text("Cost: 30-40 mana\n\n", NamedTextColor.BLUE))
            .append(Component.text("🪨 Rock Wall\n", NamedTextColor.GRAY, TextDecoration.BOLD))
            .append(Component.text("Conjure stone barrier.\n", NamedTextColor.DARK_GRAY))
            .append(Component.text("Cost: 25-35 mana", NamedTextColor.BLUE))
            .build();
    }

    private Component createManaPotionPage() {
        return Component.text()
            .append(Component.text("═ MANA POTION ═\n\n", NamedTextColor.AQUA, TextDecoration.BOLD))
            .append(Component.text("Infuse water with magical glowstone energy to create mana.\n\n", NamedTextColor.BLACK))
            .append(Component.text("Recipe:\n", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .append(Component.text("1. Fill bottles with ", NamedTextColor.BLACK))
            .append(Component.text("Water\n", NamedTextColor.BLUE))
            .append(Component.text("2. Add ", NamedTextColor.BLACK))
            .append(Component.text("Glowstone Dust\n", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text("   to the brewing stand\n\n", NamedTextColor.BLACK))
            .append(Component.text("Effect:\n", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .append(Component.text("Restores ", NamedTextColor.BLACK))
            .append(Component.text("50 mana", NamedTextColor.AQUA, TextDecoration.BOLD))
            .append(Component.text(" when consumed.", NamedTextColor.BLACK))
            .build();
    }

    private Component createBackpackPage() {
        return Component.text()
            .append(Component.text("═══ BACKPACK ═══\n\n", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text("Carry extra items with a magical backpack!\n\n", NamedTextColor.BLACK))
            .append(Component.text("Recipe:\n", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .append(Component.text("Crafting Table Pattern:\n", NamedTextColor.BLACK))
            .append(Component.text("L L L\n", NamedTextColor.GOLD))
            .append(Component.text("L ", NamedTextColor.GOLD))
            .append(Component.text("C", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
            .append(Component.text(" L\n", NamedTextColor.GOLD))
            .append(Component.text("L L L\n\n", NamedTextColor.GOLD))
            .append(Component.text("L", NamedTextColor.GOLD, TextDecoration.BOLD))
            .append(Component.text(" = Leather\n", NamedTextColor.BLACK))
            .append(Component.text("C", NamedTextColor.DARK_GRAY, TextDecoration.BOLD))
            .append(Component.text(" = Chest\n\n", NamedTextColor.BLACK))
            .append(Component.text("Right-click to open 27 slots of storage!", NamedTextColor.DARK_GRAY, TextDecoration.ITALIC))
            .build();
    }

    private Component createSpellWorkstationPage() {
        return Component.text()
            .append(Component.text("═ SPELL SYSTEM ═\n\n", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .append(Component.text("To unlock spells:\n\n", NamedTextColor.BLACK))
            .append(Component.text("1. Obtain a ", NamedTextColor.BLACK))
            .append(Component.text("Spell Book\n", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
            .append(Component.text("2. Place it in a ", NamedTextColor.BLACK))
            .append(Component.text("Lectern\n", NamedTextColor.GOLD))
            .append(Component.text("3. Use the GUI to unlock and upgrade spells\n\n", NamedTextColor.BLACK))
            .append(Component.text("Cast Spells:\n", NamedTextColor.DARK_PURPLE, TextDecoration.BOLD))
            .append(Component.text("Hold your spell book and left-click to cast your equipped spell.\n\n", NamedTextColor.BLACK))
            .append(Component.text("Mana regenerates over time.", NamedTextColor.BLUE, TextDecoration.ITALIC))
            .build();
    }

    /**
     * Checks if an item is the guidebook.
     */
    public boolean isGuidebook(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
            .has(guidebookKey, org.bukkit.persistence.PersistentDataType.BYTE);
    }
}
