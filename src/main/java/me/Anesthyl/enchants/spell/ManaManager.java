package me.Anesthyl.enchants.spell;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages mana for players - displayed as a boss bar above hunger bar.
 */
public class ManaManager {
    private final JavaPlugin plugin;
    private final Map<UUID, PlayerMana> playerManaMap = new HashMap<>();
    private BukkitTask regenTask;
    private me.Anesthyl.enchants.Commands.InfiniteManaCommand infiniteManaCommand;

    // Mana constants
    private static final double MAX_MANA = 100.0;
    private static final double MANA_REGEN_RATE = 2.0; // Mana per second
    private static final int REGEN_INTERVAL = 20; // Ticks (1 second)
    private static final long HIDE_DELAY = 2400L; // 2 minutes in ticks (120 seconds * 20 ticks)

    public ManaManager(JavaPlugin plugin) {
        this.plugin = plugin;
        startManaRegeneration();
    }

    /**
     * Sets the infinite mana command reference (for checking infinite mana status).
     */
    public void setInfiniteManaCommand(me.Anesthyl.enchants.Commands.InfiniteManaCommand command) {
        this.infiniteManaCommand = command;
    }

    /**
     * Gets or creates mana data for a player.
     */
    public PlayerMana getPlayerMana(Player player) {
        return playerManaMap.computeIfAbsent(player.getUniqueId(), uuid -> {
            PlayerMana mana = new PlayerMana(player);
            // Don't show immediately - will show on first mana use
            return mana;
        });
    }

    /**
     * Gets current mana amount for a player.
     */
    public double getMana(Player player) {
        return getPlayerMana(player).getCurrentMana();
    }

    /**
     * Sets mana for a player.
     */
    public void setMana(Player player, double amount) {
        PlayerMana mana = getPlayerMana(player);
        mana.setCurrentMana(Math.max(0, Math.min(MAX_MANA, amount)));
        mana.updateBossBar();
        mana.resetHideTimer(plugin, HIDE_DELAY);
    }

    /**
     * Adds mana to a player.
     */
    public void addMana(Player player, double amount) {
        PlayerMana mana = getPlayerMana(player);
        mana.setCurrentMana(Math.max(0, Math.min(MAX_MANA, mana.getCurrentMana() + amount)));
        mana.updateBossBar();
        mana.resetHideTimer(plugin, HIDE_DELAY);
    }

    /**
     * Uses mana from a player. Returns true if successful.
     */
    public boolean useMana(Player player, double amount) {
        // Check for infinite mana
        if (infiniteManaCommand != null && infiniteManaCommand.hasInfiniteMana(player)) {
            PlayerMana mana = getPlayerMana(player);
            mana.updateBossBar();
            mana.resetHideTimer(plugin, HIDE_DELAY);
            return true; // Always successful with infinite mana
        }
        
        PlayerMana mana = getPlayerMana(player);
        double current = mana.getCurrentMana();
        if (current >= amount) {
            mana.setCurrentMana(current - amount);
            mana.updateBossBar();
            mana.resetHideTimer(plugin, HIDE_DELAY);
            return true;
        }
        return false;
    }

    /**
     * Checks if player has enough mana.
     */
    public boolean hasMana(Player player, double amount) {
        return getMana(player) >= amount;
    }

    /**
     * Removes a player from tracking.
     */
    public void removePlayer(Player player) {
        PlayerMana mana = playerManaMap.remove(player.getUniqueId());
        if (mana != null) {
            mana.cleanup();
        }
    }

    /**
     * Starts the mana regeneration task.
     */
    private void startManaRegeneration() {
        regenTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (PlayerMana mana : playerManaMap.values()) {
                if (mana.getCurrentMana() < MAX_MANA) {
                    mana.setCurrentMana(Math.min(MAX_MANA, mana.getCurrentMana() + MANA_REGEN_RATE));
                    mana.updateBossBar();
                }
            }
        }, REGEN_INTERVAL, REGEN_INTERVAL);
    }

    /**
     * Stops the mana regeneration task.
     */
    public void shutdown() {
        if (regenTask != null) {
            regenTask.cancel();
        }
        for (PlayerMana mana : playerManaMap.values()) {
            mana.cleanup();
        }
        playerManaMap.clear();
    }

    /**
     * Represents a player's mana data.
     */
    public static class PlayerMana {
        private final Player player;
        private double currentMana;
        private BossBar bossBar;
        private BukkitTask hideTask;
        private boolean isVisible;

        public PlayerMana(Player player) {
            this.player = player;
            this.currentMana = MAX_MANA;
            this.isVisible = false;
            createBossBar();
        }

        private void createBossBar() {
            bossBar = BossBar.bossBar(
                    Component.text("✦ Mana ✦").color(NamedTextColor.AQUA),
                    1.0f,
                    BossBar.Color.BLUE,
                    BossBar.Overlay.NOTCHED_10
            );
        }

        public void showBossBar() {
            if (bossBar != null && !isVisible) {
                player.showBossBar(bossBar);
                isVisible = true;
            }
        }

        public void hideBossBar() {
            if (bossBar != null && isVisible) {
                player.hideBossBar(bossBar);
                isVisible = false;
            }
        }

        /**
         * Resets the hide timer - shows the bar and schedules it to hide after delay.
         */
        public void resetHideTimer(JavaPlugin plugin, long delay) {
            // Cancel existing hide task
            if (hideTask != null) {
                hideTask.cancel();
            }

            // Show the boss bar
            showBossBar();

            // Schedule hide task
            hideTask = Bukkit.getScheduler().runTaskLater(plugin, this::hideBossBar, delay);
        }

        public void updateBossBar() {
            if (bossBar != null) {
                float progress = (float) (currentMana / MAX_MANA);
                bossBar.progress(Math.max(0.0f, Math.min(1.0f, progress)));

                // Change color based on mana level
                if (progress > 0.6) {
                    bossBar.color(BossBar.Color.BLUE);
                } else if (progress > 0.3) {
                    bossBar.color(BossBar.Color.YELLOW);
                } else {
                    bossBar.color(BossBar.Color.RED);
                }

                // Update title with current/max mana
                Component title = Component.text("✦ Mana: ")
                        .color(NamedTextColor.AQUA)
                        .append(Component.text(String.format("%.0f", currentMana))
                                .color(NamedTextColor.WHITE))
                        .append(Component.text("/")
                                .color(NamedTextColor.GRAY))
                        .append(Component.text(String.format("%.0f", MAX_MANA))
                                .color(NamedTextColor.WHITE))
                        .append(Component.text(" ✦")
                                .color(NamedTextColor.AQUA));
                bossBar.name(title);
            }
        }

        public double getCurrentMana() {
            return currentMana;
        }

        public void setCurrentMana(double mana) {
            this.currentMana = mana;
        }

        public double getMaxMana() {
            return MAX_MANA;
        }

        /**
         * Cleanup method to cancel tasks and hide bar.
         */
        public void cleanup() {
            if (hideTask != null) {
                hideTask.cancel();
                hideTask = null;
            }
            hideBossBar();
        }
    }
}
