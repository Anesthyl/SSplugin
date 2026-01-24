package me.Anesthyl.enchants.spell;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Manages spell books and spell data stored in items.
 */
public class SpellManager {
    private final JavaPlugin plugin;
    private final NamespacedKey spellBookKey;
    private final NamespacedKey spellDataKey;
    private final NamespacedKey equippedSpellKey;

    public SpellManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.spellBookKey = new NamespacedKey(plugin, "spellbook");
        this.spellDataKey = new NamespacedKey(plugin, "spell_data");
        this.equippedSpellKey = new NamespacedKey(plugin, "equipped_spell");
    }

    /**
     * Creates a new empty spell book item.
     */
    public ItemStack createSpellBook() {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();

        meta.displayName(Component.text("Spell Book")
                .color(NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Place in a lectern to access spells")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Unlocked Spells: 0")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        // Mark as spell book
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(spellBookKey, PersistentDataType.BYTE, (byte) 1);
        
        // Initialize empty spell data
        pdc.set(spellDataKey, PersistentDataType.STRING, serializeSpellData(new HashMap<>()));

        book.setItemMeta(meta);
        return book;
    }

    /**
     * Checks if an item is a spell book.
     */
    public boolean isSpellBook(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(spellBookKey, PersistentDataType.BYTE);
    }

    /**
     * Gets spell data from a spell book.
     * Returns a map of Spell -> Level
     */
    public Map<Spell, Integer> getSpellData(ItemStack spellBook) {
        if (!isSpellBook(spellBook)) return new HashMap<>();
        
        ItemMeta meta = spellBook.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String data = pdc.get(spellDataKey, PersistentDataType.STRING);
        
        return deserializeSpellData(data);
    }

    /**
     * Saves spell data to a spell book.
     */
    public void setSpellData(ItemStack spellBook, Map<Spell, Integer> spellData) {
        if (!isSpellBook(spellBook)) return;
        
        ItemMeta meta = spellBook.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(spellDataKey, PersistentDataType.STRING, serializeSpellData(spellData));
        
        // Update lore
        updateSpellBookLore(meta, spellData);
        
        spellBook.setItemMeta(meta);
    }

    /**
     * Gets the level of a specific spell in a spell book.
     */
    public int getSpellLevel(ItemStack spellBook, Spell spell) {
        return getSpellData(spellBook).getOrDefault(spell, 0);
    }

    /**
     * Sets the level of a specific spell in a spell book.
     */
    public void setSpellLevel(ItemStack spellBook, Spell spell, int level) {
        Map<Spell, Integer> data = getSpellData(spellBook);
        if (level <= 0) {
            data.remove(spell);
        } else {
            data.put(spell, Math.min(level, spell.getMaxLevel()));
        }
        setSpellData(spellBook, data);
    }

    /**
     * Unlocks a spell at level 1.
     */
    public void unlockSpell(ItemStack spellBook, Spell spell) {
        setSpellLevel(spellBook, spell, 1);
    }

    /**
     * Levels up a spell by 1 level.
     */
    public boolean levelUpSpell(ItemStack spellBook, Spell spell) {
        int currentLevel = getSpellLevel(spellBook, spell);
        if (currentLevel == 0 || currentLevel >= spell.getMaxLevel()) {
            return false;
        }
        setSpellLevel(spellBook, spell, currentLevel + 1);
        return true;
    }

    /**
     * Updates the lore of a spell book based on its spell data.
     */
    private void updateSpellBookLore(ItemMeta meta, Map<Spell, Integer> spellData) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Place in a lectern to access spells")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Unlocked Spells: " + spellData.size())
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        
        if (!spellData.isEmpty()) {
            lore.add(Component.empty());
            for (Map.Entry<Spell, Integer> entry : spellData.entrySet()) {
                lore.add(Component.text("• " + entry.getKey().getName() + " " + entry.getValue())
                        .color(NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }
        
        meta.lore(lore);
    }

    /**
     * Serializes spell data to a string.
     * Format: SPELL1:LEVEL,SPELL2:LEVEL
     */
    private String serializeSpellData(Map<Spell, Integer> data) {
        if (data.isEmpty()) return "";
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Spell, Integer> entry : data.entrySet()) {
            if (sb.length() > 0) sb.append(",");
            sb.append(entry.getKey().name()).append(":").append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * Deserializes spell data from a string.
     */
    private Map<Spell, Integer> deserializeSpellData(String data) {
        Map<Spell, Integer> result = new HashMap<>();
        if (data == null || data.isEmpty()) return result;

        try {
            String[] entries = data.split(",");
            for (String entry : entries) {
                String[] parts = entry.split(":");
                if (parts.length == 2) {
                    Spell spell = Spell.valueOf(parts[0]);
                    int level = Integer.parseInt(parts[1]);
                    result.put(spell, level);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deserialize spell data: " + data);
        }

        return result;
    }

    /**
     * Gets the equipped spell from a spell book.
     */
    public Spell getEquippedSpell(ItemStack spellBook) {
        if (!isSpellBook(spellBook)) return null;

        ItemMeta meta = spellBook.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        String spellName = pdc.get(equippedSpellKey, PersistentDataType.STRING);

        if (spellName == null || spellName.isEmpty()) return null;

        try {
            return Spell.valueOf(spellName);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Sets the equipped spell for a spell book.
     */
    public void setEquippedSpell(ItemStack spellBook, Spell spell) {
        if (!isSpellBook(spellBook)) return;

        ItemMeta meta = spellBook.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (spell == null) {
            pdc.remove(equippedSpellKey);
        } else {
            pdc.set(equippedSpellKey, PersistentDataType.STRING, spell.name());
        }

        // Update lore to show equipped spell
        updateSpellBookLoreWithEquipped(meta, getSpellData(spellBook), spell);

        spellBook.setItemMeta(meta);
    }

    /**
     * Updates the lore of a spell book to show equipped spell.
     */
    private void updateSpellBookLoreWithEquipped(ItemMeta meta, Map<Spell, Integer> spellData, Spell equipped) {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Right-click lectern to access spells")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Left-click to cast equipped spell")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("Unlocked Spells: " + spellData.size())
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));

        if (equipped != null) {
            lore.add(Component.empty());
            lore.add(Component.text("Equipped: " + equipped.getName())
                    .color(NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true));
        }

        if (!spellData.isEmpty()) {
            lore.add(Component.empty());
            for (Map.Entry<Spell, Integer> entry : spellData.entrySet()) {
                lore.add(Component.text("• " + entry.getKey().getName() + " " + entry.getValue())
                        .color(NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }

        meta.lore(lore);
    }
}
