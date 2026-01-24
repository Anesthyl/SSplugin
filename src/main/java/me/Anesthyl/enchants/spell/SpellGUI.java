package me.Anesthyl.enchants.spell;

import me.Anesthyl.enchants.level.LevelManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GUI for spell book workstation where players can unlock and level up spells.
 */
public class SpellGUI implements Listener {
    private final SpellManager spellManager;
    private final LevelManager levelManager;
    private final Map<Player, ItemStack> activeSpellBooks = new HashMap<>();

    public SpellGUI(SpellManager spellManager, LevelManager levelManager) {
        this.spellManager = spellManager;
        this.levelManager = levelManager;
    }

    /**
     * Opens the spell GUI for a player with their spell book.
     */
    public void openGUI(Player player, ItemStack spellBook) {
        if (!spellManager.isSpellBook(spellBook)) return;

        activeSpellBooks.put(player, spellBook);

        // Create animated title
        Component title = Component.text("✦ ")
                .color(NamedTextColor.DARK_PURPLE)
                .append(Component.text("S").color(NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("p").color(NamedTextColor.BLUE))
                .append(Component.text("e").color(NamedTextColor.DARK_PURPLE))
                .append(Component.text("l").color(NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("l").color(NamedTextColor.BLUE))
                .append(Component.text(" "))
                .append(Component.text("B").color(NamedTextColor.DARK_PURPLE))
                .append(Component.text("o").color(NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("o").color(NamedTextColor.BLUE))
                .append(Component.text("k").color(NamedTextColor.DARK_PURPLE))
                .append(Component.text(" ✦").color(NamedTextColor.LIGHT_PURPLE));

        Inventory gui = Bukkit.createInventory(null, 54, title);
        Map<Spell, Integer> spellData = spellManager.getSpellData(spellBook);
        Spell equippedSpell = spellManager.getEquippedSpell(spellBook);

        // Layout for 6 spells: 3 columns x 2 rows
        // Row 2: slots 11, 13, 15 (spells)
        // Row 3: slots 20, 22, 24 (equip buttons)
        // Row 4: slots 29, 31, 33 (spells)
        // Row 5: slots 38, 40, 42 (equip buttons)
        int[] spellSlots = {11, 13, 15, 29, 31, 33}; // Spell positions
        int[] equipSlots = {20, 22, 24, 38, 40, 42}; // Equip button positions directly below

        int index = 0;
        for (Spell spell : Spell.values()) {
            if (index >= spellSlots.length) break;

            int currentLevel = spellData.getOrDefault(spell, 0);

            // Spell item
            ItemStack spellItem = createSpellItem(spell, currentLevel, player, spellBook);
            gui.setItem(spellSlots[index], spellItem);

            // Equip button below spell (only if unlocked)
            if (currentLevel > 0) {
                ItemStack equipButton = createEquipButton(spell, spell == equippedSpell);
                gui.setItem(equipSlots[index], equipButton);
            }

            index++;
        }

        // Add decorative borders
        ItemStack border = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.displayName(Component.text(" "));
        border.setItemMeta(borderMeta);

        // Top and bottom borders
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, border);
            gui.setItem(45 + i, border);
        }

        // Add info item
        ItemStack info = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.displayName(Component.text("✨ How to Use ✨")
                .color(NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false)
                .decoration(TextDecoration.BOLD, true));
        List<Component> infoLore = new ArrayList<>();
        infoLore.add(Component.text("━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        infoLore.add(Component.text("• Click a spell to unlock/upgrade")
                .color(NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        infoLore.add(Component.text("• Click EQUIP to set active spell")
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));
        infoLore.add(Component.text("• Left-click to cast equipped spell")
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        infoLore.add(Component.text("━━━━━━━━━━━━━━━━━━━")
                .color(NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, false));
        infoMeta.lore(infoLore);
        info.setItemMeta(infoMeta);
        gui.setItem(49, info);

        player.openInventory(gui);
    }

    /**
     * Creates the equip button for a spell.
     */
    private ItemStack createEquipButton(Spell spell, boolean isEquipped) {
        Material buttonMaterial = isEquipped ? Material.LIME_DYE : Material.LIGHT_GRAY_DYE;
        ItemStack button = new ItemStack(buttonMaterial);
        ItemMeta meta = button.getItemMeta();

        Component name = isEquipped
                ? Component.text("✓ Equipped").color(NamedTextColor.GREEN)
                : Component.text("Equip").color(NamedTextColor.GRAY);
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text("Click to equip " + spell.getName())
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);

        button.setItemMeta(meta);
        return button;
    }

    /**
     * Creates an item representing a spell in the GUI.
     */
    private ItemStack createSpellItem(Spell spell, int currentLevel, Player player, ItemStack spellBook) {
        Material iconMaterial = currentLevel > 0 ? spell.getIcon() : Material.GRAY_DYE;
        ItemStack item = new ItemStack(iconMaterial);
        ItemMeta meta = item.getItemMeta();

        // Title
        Component name = currentLevel > 0
                ? Component.text(spell.getName() + " ").append(Component.text(toRoman(currentLevel)))
                        .color(NamedTextColor.GREEN)
                : Component.text(spell.getName() + " ").append(Component.text("(Locked)"))
                        .color(NamedTextColor.RED);
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));

        // Lore
        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(spell.getDescription())
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());

        if (currentLevel == 0) {
            // Show unlock requirements
            Spell.SpellRequirement req = spell.getRequirement(1);
            lore.add(Component.text("Unlock Requirements:")
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            addRequirementLore(lore, req, player);
            lore.add(Component.empty());
            lore.add(Component.text("Click to unlock!")
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
        } else if (currentLevel < spell.getMaxLevel()) {
            // Show current level and upgrade requirements
            lore.add(Component.text("Current Level: " + currentLevel + "/" + spell.getMaxLevel())
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
            lore.add(Component.empty());
            
            Spell.SpellRequirement req = spell.getRequirement(currentLevel + 1);
            lore.add(Component.text("Upgrade Requirements:")
                    .color(NamedTextColor.YELLOW)
                    .decoration(TextDecoration.ITALIC, false));
            addRequirementLore(lore, req, player);
            lore.add(Component.empty());
            lore.add(Component.text("Click to upgrade!")
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            // Max level
            lore.add(Component.text("Max Level Reached!")
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Adds requirement information to lore.
     */
    private void addRequirementLore(List<Component> lore, Spell.SpellRequirement req, Player player) {
        // Materials
        for (ItemStack mat : req.getMaterials()) {
            int playerAmount = getPlayerAmount(player, mat.getType());
            boolean hasEnough = playerAmount >= mat.getAmount();
            NamedTextColor color = hasEnough ? NamedTextColor.GREEN : NamedTextColor.RED;
            
            lore.add(Component.text("• " + mat.getAmount() + "x " + formatMaterialName(mat.getType()))
                    .color(color)
                    .decoration(TextDecoration.ITALIC, false)
                    .append(Component.text(" (" + playerAmount + ")")
                            .color(NamedTextColor.GRAY)));
        }
        
        // XP Levels
        int playerLevels = player.getLevel();
        boolean hasEnoughXP = playerLevels >= req.getXpLevels();
        NamedTextColor xpColor = hasEnoughXP ? NamedTextColor.GREEN : NamedTextColor.RED;
        
        lore.add(Component.text("• " + req.getXpLevels() + " XP Levels")
                .color(xpColor)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(" (" + playerLevels + ")")
                        .color(NamedTextColor.GRAY)));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if this is a spell book GUI
        if (!activeSpellBooks.containsKey(player)) return;

        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        ItemStack spellBook = activeSpellBooks.get(player);
        if (spellBook == null) return;

        // Check if it's an equip button
        if (clicked.getType() == Material.LIME_DYE || clicked.getType() == Material.LIGHT_GRAY_DYE) {
            handleEquipButton(player, clicked, spellBook);
            return;
        }

        // Find which spell was clicked
        Spell clickedSpell = getSpellFromItem(clicked);
        if (clickedSpell == null) return;

        Map<Spell, Integer> spellData = spellManager.getSpellData(spellBook);
        int currentLevel = spellData.getOrDefault(clickedSpell, 0);

        // Check if at max level
        if (currentLevel >= clickedSpell.getMaxLevel()) {
            player.sendMessage(Component.text("This spell is already at max level!")
                    .color(NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Get requirements
        int targetLevel = currentLevel + 1;
        Spell.SpellRequirement req = clickedSpell.getRequirement(targetLevel);

        // Check if player has requirements
        if (!hasRequirements(player, req)) {
            player.sendMessage(Component.text("You don't have the required materials or XP!")
                    .color(NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Consume requirements
        consumeRequirements(player, req);

        // Unlock or level up spell
        if (currentLevel == 0) {
            spellManager.unlockSpell(spellBook, clickedSpell);
            player.sendMessage(Component.text("Unlocked spell: ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(clickedSpell.getName())
                            .color(NamedTextColor.GOLD)));
        } else {
            spellManager.levelUpSpell(spellBook, clickedSpell);
            player.sendMessage(Component.text("Leveled up spell: ")
                    .color(NamedTextColor.GREEN)
                    .append(Component.text(clickedSpell.getName() + " → ")
                            .color(NamedTextColor.GOLD))
                    .append(Component.text(toRoman(targetLevel))
                            .color(NamedTextColor.AQUA)));
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.2f);

        // Refresh GUI
        player.closeInventory();
        openGUI(player, spellBook);
    }

    /**
     * Handles clicking an equip button.
     */
    private void handleEquipButton(Player player, ItemStack button, ItemStack spellBook) {
        if (!button.hasItemMeta()) return;

        ItemMeta meta = button.getItemMeta();
        List<Component> lore = meta.lore();
        if (lore == null || lore.isEmpty()) return;

        // Extract spell name from lore ("Click to equip SPELL_NAME")
        String loreText = ((net.kyori.adventure.text.TextComponent) lore.get(0)).content();
        String spellName = loreText.replace("Click to equip ", "");

        // Find the spell
        Spell spell = null;
        for (Spell s : Spell.values()) {
            if (s.getName().equals(spellName)) {
                spell = s;
                break;
            }
        }

        if (spell == null) return;

        // Equip the spell
        spellManager.setEquippedSpell(spellBook, spell);
        player.sendMessage(Component.text("Equipped: ")
                .color(NamedTextColor.GREEN)
                .append(Component.text(spell.getName())
                        .color(NamedTextColor.LIGHT_PURPLE)));
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);

        // Refresh GUI
        player.closeInventory();
        openGUI(player, spellBook);
    }

    /**
     * Gets the spell associated with a GUI item.
     */
    private Spell getSpellFromItem(ItemStack item) {
        if (!item.hasItemMeta()) return null;
        Component displayName = item.getItemMeta().displayName();
        if (displayName == null) return null;

        String name = ((net.kyori.adventure.text.TextComponent) displayName).content();
        for (Spell spell : Spell.values()) {
            if (name.startsWith(spell.getName())) {
                return spell;
            }
        }
        return null;
    }

    /**
     * Checks if a player has the required materials and XP.
     */
    private boolean hasRequirements(Player player, Spell.SpellRequirement req) {
        // Check materials
        for (ItemStack mat : req.getMaterials()) {
            if (getPlayerAmount(player, mat.getType()) < mat.getAmount()) {
                return false;
            }
        }
        
        // Check XP
        return player.getLevel() >= req.getXpLevels();
    }

    /**
     * Consumes the required materials and XP from a player.
     */
    private void consumeRequirements(Player player, Spell.SpellRequirement req) {
        // Consume materials
        for (ItemStack mat : req.getMaterials()) {
            int remaining = mat.getAmount();
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == mat.getType()) {
                    int toRemove = Math.min(remaining, item.getAmount());
                    item.setAmount(item.getAmount() - toRemove);
                    remaining -= toRemove;
                    if (remaining <= 0) break;
                }
            }
        }
        
        // Consume XP
        player.setLevel(player.getLevel() - req.getXpLevels());
    }

    /**
     * Gets the total amount of a material in a player's inventory.
     */
    private int getPlayerAmount(Player player, Material material) {
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
            }
        }
        return total;
    }

    /**
     * Formats a material name to be more readable.
     */
    private String formatMaterialName(Material material) {
        String name = material.name().replace("_", " ").toLowerCase();
        String[] words = name.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        return result.toString().trim();
    }

    /**
     * Converts a number to Roman numerals.
     */
    private String toRoman(int number) {
        String[] romanNumerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        if (number >= 1 && number <= 10) {
            return romanNumerals[number - 1];
        }
        return String.valueOf(number);
    }

    /**
     * Cleanup when player closes inventory.
     */
    public void removePlayer(Player player) {
        activeSpellBooks.remove(player);
    }
}
