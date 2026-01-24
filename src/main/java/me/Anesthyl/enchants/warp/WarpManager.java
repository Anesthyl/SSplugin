package me.Anesthyl.enchants.warp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages player warps - each player can create their own named warps.
 */
public class WarpManager {
    private final JavaPlugin plugin;
    private final File warpsFile;
    private FileConfiguration warpsConfig;

    // Map of UUID -> Map of warp name -> Location
    private final Map<UUID, Map<String, Location>> playerWarps = new HashMap<>();

    public WarpManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        loadWarps();
    }

    /**
     * Sets a warp for a player at their current location.
     */
    public void setWarp(UUID playerUUID, String warpName, Location location) {
        Map<String, Location> warps = playerWarps.computeIfAbsent(playerUUID, k -> new HashMap<>());
        warps.put(warpName.toLowerCase(), location);
        saveWarps();
    }

    /**
     * Gets a warp location for a player.
     */
    public Location getWarp(UUID playerUUID, String warpName) {
        Map<String, Location> warps = playerWarps.get(playerUUID);
        if (warps == null) return null;
        return warps.get(warpName.toLowerCase());
    }

    /**
     * Deletes a warp for a player.
     */
    public boolean deleteWarp(UUID playerUUID, String warpName) {
        Map<String, Location> warps = playerWarps.get(playerUUID);
        if (warps == null) return false;

        boolean removed = warps.remove(warpName.toLowerCase()) != null;
        if (removed) {
            saveWarps();
        }
        return removed;
    }

    /**
     * Gets all warp names for a player.
     */
    public Set<String> getPlayerWarps(UUID playerUUID) {
        Map<String, Location> warps = playerWarps.get(playerUUID);
        if (warps == null) return new HashSet<>();
        return new HashSet<>(warps.keySet());
    }

    /**
     * Checks if a player has a specific warp.
     */
    public boolean hasWarp(UUID playerUUID, String warpName) {
        Map<String, Location> warps = playerWarps.get(playerUUID);
        if (warps == null) return false;
        return warps.containsKey(warpName.toLowerCase());
    }

    /**
     * Loads warps from the warps.yml file.
     */
    private void loadWarps() {
        if (!warpsFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                warpsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create warps.yml: " + e.getMessage());
            }
        }

        warpsConfig = YamlConfiguration.loadConfiguration(warpsFile);

        // Load all player warps
        for (String uuidString : warpsConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                Map<String, Location> warps = new HashMap<>();

                for (String warpName : warpsConfig.getConfigurationSection(uuidString).getKeys(false)) {
                    String path = uuidString + "." + warpName;

                    String worldName = warpsConfig.getString(path + ".world");
                    double x = warpsConfig.getDouble(path + ".x");
                    double y = warpsConfig.getDouble(path + ".y");
                    double z = warpsConfig.getDouble(path + ".z");
                    float yaw = (float) warpsConfig.getDouble(path + ".yaw");
                    float pitch = (float) warpsConfig.getDouble(path + ".pitch");

                    Location location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                    warps.put(warpName, location);
                }

                playerWarps.put(uuid, warps);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load warps for " + uuidString + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded warps for " + playerWarps.size() + " players");
    }

    /**
     * Saves all warps to the warps.yml file.
     */
    private void saveWarps() {
        warpsConfig = new YamlConfiguration();

        for (Map.Entry<UUID, Map<String, Location>> entry : playerWarps.entrySet()) {
            String uuidString = entry.getKey().toString();

            for (Map.Entry<String, Location> warpEntry : entry.getValue().entrySet()) {
                String warpName = warpEntry.getKey();
                Location loc = warpEntry.getValue();

                String path = uuidString + "." + warpName;
                warpsConfig.set(path + ".world", loc.getWorld().getName());
                warpsConfig.set(path + ".x", loc.getX());
                warpsConfig.set(path + ".y", loc.getY());
                warpsConfig.set(path + ".z", loc.getZ());
                warpsConfig.set(path + ".yaw", loc.getYaw());
                warpsConfig.set(path + ".pitch", loc.getPitch());
            }
        }

        try {
            warpsConfig.save(warpsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save warps.yml: " + e.getMessage());
        }
    }
}
