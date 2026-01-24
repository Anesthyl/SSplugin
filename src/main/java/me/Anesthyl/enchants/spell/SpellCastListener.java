package me.Anesthyl.enchants.spell;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Handles spell casting when left-clicking with a spell book.
 */
public class SpellCastListener implements Listener {
    private final JavaPlugin plugin;
    private final SpellManager spellManager;
    private final ManaManager manaManager;

    public SpellCastListener(JavaPlugin plugin, SpellManager spellManager, ManaManager manaManager) {
        this.plugin = plugin;
        this.spellManager = spellManager;
        this.manaManager = manaManager;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if holding a spell book
        if (!spellManager.isSpellBook(item)) return;

        // Get equipped spell
        Spell equippedSpell = spellManager.getEquippedSpell(item);
        if (equippedSpell == null) {
            player.sendMessage(Component.text("No spell equipped! Right-click a lectern to equip a spell.")
                    .color(NamedTextColor.RED));
            return;
        }

        // Check spell level
        int spellLevel = spellManager.getSpellLevel(item, equippedSpell);
        if (spellLevel == 0) {
            player.sendMessage(Component.text("This spell is not unlocked!")
                    .color(NamedTextColor.RED));
            return;
        }

        // Check mana cost
        double manaCost = equippedSpell.getManaCost(spellLevel);
        if (!manaManager.hasMana(player, manaCost)) {
            player.sendMessage(Component.text("Not enough mana! Need ")
                    .color(NamedTextColor.RED)
                    .append(Component.text(String.format("%.0f", manaCost))
                            .color(NamedTextColor.YELLOW))
                    .append(Component.text(" mana")
                            .color(NamedTextColor.RED)));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        // Cast the spell
        event.setCancelled(true);
        boolean success = castSpell(player, equippedSpell, spellLevel);

        if (success) {
            // Use mana
            manaManager.useMana(player, manaCost);

            player.sendMessage(Component.text("Cast: ")
                    .color(NamedTextColor.AQUA)
                    .append(Component.text(equippedSpell.getName())
                            .color(NamedTextColor.LIGHT_PURPLE))
                    .append(Component.text(" (-" + String.format("%.0f", manaCost) + " mana)")
                            .color(NamedTextColor.GRAY)));
        }
    }

    /**
     * Casts a spell based on the spell type.
     */
    private boolean castSpell(Player player, Spell spell, int level) {
        Location eyeLocation = player.getEyeLocation();
        Vector direction = eyeLocation.getDirection();

        switch (spell) {
            case FIREBALL:
                return castFireball(player, eyeLocation, direction, level);

            case TELEPORT:
                return castTeleport(player, level);

            case WIND_BLAST:
                return castWindBlast(player, eyeLocation, direction, level);

            case NETHER_SHIELD:
                return castNetherShield(player, level);

            case LIGHTNING_STRIKE:
                return castLightningStrike(player, eyeLocation, direction, level);

            case ROCK_WALL:
                return castRockWall(player, eyeLocation, direction, level);

            default:
                return false;
        }
    }

    /**
     * Casts a fireball spell.
     * Level 1: Small fireball, minimal damage/destruction
     * Level 2: Medium fireball, moderate damage/destruction
     * Level 3: Large fireball, high damage/destruction
     */
    private boolean castFireball(Player player, Location eyeLocation, Vector direction, int level) {
        World world = player.getWorld();

        // Scale based on level
        float explosionPower;
        boolean setFire;
        int particleCount;

        switch (level) {
            case 1:
                explosionPower = 1.5f; // Small explosion
                setFire = false;
                particleCount = 20;
                break;
            case 2:
                explosionPower = 2.5f; // Medium explosion
                setFire = true;
                particleCount = 40;
                break;
            case 3:
                explosionPower = 4.0f; // Large explosion with significant block damage
                setFire = true;
                particleCount = 60;
                break;
            default:
                explosionPower = 1.0f;
                setFire = false;
                particleCount = 20;
        }

        // Launch fireball
        Fireball fireball = world.spawn(eyeLocation.add(direction.multiply(1.5)), Fireball.class);
        fireball.setShooter(player);
        fireball.setDirection(direction);
        fireball.setYield(explosionPower);
        fireball.setIsIncendiary(setFire);

        // Visual and audio effects
        float pitch = 1.0f + (level * 0.1f);
        world.playSound(eyeLocation, Sound.ENTITY_BLAZE_SHOOT, 1.0f, pitch);
        world.spawnParticle(Particle.FLAME, eyeLocation, particleCount, 0.3, 0.3, 0.3, 0.05);

        // Level 3: Add extra visual effect
        if (level == 3) {
            world.spawnParticle(Particle.SOUL_FIRE_FLAME, eyeLocation, 20, 0.3, 0.3, 0.3, 0.05);
            world.playSound(eyeLocation, Sound.ENTITY_WITHER_SHOOT, 0.5f, 1.5f);
        }

        return true;
    }

    /**
     * Casts a teleport spell.
     * Level 1: 15 block range
     * Level 2: 20 block range
     * Level 3: 25 block range, grants brief speed boost
     */
    private boolean castTeleport(Player player, int level) {
        // Get block player is looking at (range increases with level)
        int range = 10 + (level * 5);
        Location targetLocation = player.getTargetBlock(null, range).getLocation();

        // Find safe location above target block
        targetLocation.add(0, 1, 0);

        // Teleport player
        Location oldLocation = player.getLocation().clone();
        player.teleport(targetLocation);

        // Effects
        int particleCount = 30 + (level * 10);
        player.getWorld().playSound(oldLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f + (level * 0.1f));
        player.getWorld().spawnParticle(Particle.PORTAL, oldLocation, particleCount, 0.5, 1, 0.5, 0.5);
        player.getWorld().playSound(targetLocation, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f + (level * 0.1f));
        player.getWorld().spawnParticle(Particle.PORTAL, targetLocation, particleCount, 0.5, 1, 0.5, 0.5);

        // Level 3: Grant speed boost
        if (level == 3) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SPEED, 60, 1)); // 3 seconds, Speed II
            player.getWorld().spawnParticle(Particle.END_ROD, targetLocation, 20, 0.5, 1, 0.5, 0.1);
        }

        return true;
    }

    /**
     * Casts a wind blast spell.
     * Level 1: 4 block radius, moderate knockback, 2 damage
     * Level 2: 5 block radius, strong knockback, 4 damage
     * Level 3: 6 block radius, very strong knockback, 6 damage, slows enemies
     */
    private boolean castWindBlast(Player player, Location eyeLocation, Vector direction, int level) {
        World world = player.getWorld();

        // Scale based on level
        double radius = 3.0 + (level * 1.0);
        double knockbackPower = 1.5 + (level * 0.75);
        double damage = 2.0 * level;
        int particleCount = 50 + (level * 25);

        // Create wind particles
        world.spawnParticle(Particle.CLOUD, eyeLocation, particleCount,
                direction.getX(), direction.getY(), direction.getZ(), 0.3);
        world.playSound(eyeLocation, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 1.0f, 1.0f + (level * 0.2f));

        Location centerLocation = eyeLocation.clone().add(direction.multiply(3));

        // Push nearby entities
        for (Entity entity : world.getNearbyEntities(centerLocation, radius, radius, radius)) {
            if (entity instanceof LivingEntity && entity != player) {
                Vector knockback = entity.getLocation().toVector()
                        .subtract(centerLocation.toVector())
                        .normalize()
                        .multiply(knockbackPower);
                entity.setVelocity(knockback);

                if (entity instanceof Damageable) {
                    ((Damageable) entity).damage(damage, player);

                    // Level 3: Apply slowness effect
                    if (level == 3 && entity instanceof LivingEntity) {
                        ((LivingEntity) entity).addPotionEffect(new org.bukkit.potion.PotionEffect(
                                org.bukkit.potion.PotionEffectType.SLOWNESS, 60, 1)); // 3 seconds, Slowness II
                    }
                }
            }
        }

        // Level 3: Extra visual effect
        if (level == 3) {
            world.spawnParticle(Particle.SWEEP_ATTACK, centerLocation, 10, radius, radius, radius, 0);
        }

        return true;
    }

    /**
     * Casts a nether shield spell.
     * Level 1: 5 seconds Resistance I + Fire Resistance
     * Level 2: 7 seconds Resistance II + Fire Resistance
     * Level 3: 9 seconds Resistance III + Fire Resistance + Absorption
     */
    private boolean castNetherShield(Player player, int level) {
        // Grant resistance and fire resistance (duration and strength scale with level)
        int duration = 100 + (level * 40); // In ticks (5, 7, 9 seconds)
        int resistanceAmplifier = level - 1;

        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.RESISTANCE, duration, resistanceAmplifier));
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.FIRE_RESISTANCE, duration, 0));

        // Level 3: Add absorption (extra hearts)
        if (level == 3) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.ABSORPTION, duration, 1)); // Absorption II
        }

        // Visual effects
        Location location = player.getLocation();
        int particleCount = 50 + (level * 25);
        float pitch = 1.5f + (level * 0.2f);

        player.getWorld().playSound(location, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1.0f, pitch);
        player.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME,
                location.clone().add(0, 1, 0), particleCount, 0.5, 1, 0.5, 0.05);

        // Level 3: Extra visual effect
        if (level == 3) {
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING,
                    location.clone().add(0, 1, 0), 20, 0.5, 1, 0.5, 0.1);
            player.getWorld().playSound(location, Sound.ITEM_TOTEM_USE, 0.5f, 2.0f);
        }

        return true;
    }

    /**
     * Casts a lightning strike spell.
     * Level 1: Single lightning strike at target location
     * Level 2: Multiple lightning strikes in AoE around target
     * Level 3: Devastating lightning storm - larger AoE with more strikes
     */
    private boolean castLightningStrike(Player player, Location eyeLocation, Vector direction, int level) {
        World world = player.getWorld();

        switch (level) {
            case 1:
                // Single strike at target location
                Location targetLoc = player.getTargetBlock(null, 50).getLocation();
                world.strikeLightning(targetLoc);
                world.playSound(eyeLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
                break;

            case 2:
                // Multiple strikes in AoE
                Location centerLoc = player.getTargetBlock(null, 50).getLocation();
                world.strikeLightning(centerLoc);

                // Strike 4 additional locations around the center
                for (int i = 0; i < 4; i++) {
                    double angle = (Math.PI * 2 * i) / 4;
                    double offsetX = Math.cos(angle) * 3;
                    double offsetZ = Math.sin(angle) * 3;
                    Location strikeLoc = centerLoc.clone().add(offsetX, 0, offsetZ);

                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        world.strikeLightning(strikeLoc);
                    }, 5L + (i * 3L)); // Stagger strikes
                }
                world.playSound(eyeLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.2f);
                break;

            case 3:
                // Devastating lightning storm - 9 strikes in a larger area
                Location stormCenter = player.getTargetBlock(null, 50).getLocation();

                // Center strike
                world.strikeLightning(stormCenter);

                // 8 strikes in a larger radius (5 blocks)
                for (int i = 0; i < 8; i++) {
                    double angle = (Math.PI * 2 * i) / 8;
                    double offsetX = Math.cos(angle) * 5;
                    double offsetZ = Math.sin(angle) * 5;
                    Location strikeLoc = stormCenter.clone().add(offsetX, 0, offsetZ);

                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        world.strikeLightning(strikeLoc);
                        world.spawnParticle(Particle.ELECTRIC_SPARK, strikeLoc, 50, 1, 1, 1, 0.2);
                    }, 3L + (i * 4L)); // Stagger strikes over time
                }

                // Massive visual and audio effects
                world.playSound(eyeLocation, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.8f);
                world.playSound(stormCenter, Sound.ENTITY_WITHER_SPAWN, 0.5f, 2.0f);
                world.spawnParticle(Particle.ELECTRIC_SPARK, eyeLocation, 100, 2, 2, 2, 0.3);

                // Add dramatic sky particles
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    world.spawnParticle(Particle.CLOUD, stormCenter.clone().add(0, 10, 0), 200, 5, 2, 5, 0.1);
                }, 10L);
                break;
        }

        return true;
    }

    /**
     * Casts a rock wall spell.
     * Level 1: 3-block tall wall of cobblestone (lasts 5 seconds)
     * Level 2: 4-block tall wall of stone (lasts 7 seconds)
     * Level 3: 5-block tall wall of obsidian (lasts 10 seconds)
     */
    private boolean castRockWall(Player player, Location eyeLocation, Vector direction, int level) {
        World world = player.getWorld();

        // Determine wall properties based on level
        Material wallMaterial;
        int wallHeight;
        int duration; // in ticks

        switch (level) {
            case 1:
                wallMaterial = Material.COBBLESTONE;
                wallHeight = 3;
                duration = 100; // 5 seconds
                break;
            case 2:
                wallMaterial = Material.STONE;
                wallHeight = 4;
                duration = 140; // 7 seconds
                break;
            case 3:
                wallMaterial = Material.OBSIDIAN;
                wallHeight = 5;
                duration = 200; // 10 seconds
                break;
            default:
                wallMaterial = Material.COBBLESTONE;
                wallHeight = 3;
                duration = 100;
        }

        // Get location 3 blocks in front of player
        Location wallCenter = eyeLocation.clone().add(direction.multiply(3));
        wallCenter.setY(Math.floor(wallCenter.getY()));

        // Calculate perpendicular direction for wall width
        Vector perpendicular = new Vector(-direction.getZ(), 0, direction.getX()).normalize();

        // Store original blocks to restore later
        List<org.bukkit.block.Block> wallBlocks = new ArrayList<>();

        // Create wall (5 blocks wide)
        for (int width = -2; width <= 2; width++) {
            Location columnBase = wallCenter.clone().add(perpendicular.clone().multiply(width));

            for (int height = 0; height < wallHeight; height++) {
                Location blockLoc = columnBase.clone().add(0, height, 0);
                org.bukkit.block.Block block = world.getBlockAt(blockLoc);

                // Only replace air or replaceable blocks
                if (block.getType() == Material.AIR || block.getType().name().contains("GRASS") ||
                        block.getType() == Material.SNOW || block.getType().name().contains("FLOWER")) {

                    wallBlocks.add(block);
                    block.setType(wallMaterial);

                    // Particle effect
                    world.spawnParticle(Particle.BLOCK, blockLoc.clone().add(0.5, 0.5, 0.5),
                            10, 0.3, 0.3, 0.3, 0, wallMaterial.createBlockData());
                }
            }
        }

        // Sound effect
        world.playSound(wallCenter, Sound.BLOCK_STONE_PLACE, 1.0f, 0.8f);

        // Level 3: Extra effects
        if (level == 3) {
            world.playSound(wallCenter, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.5f, 0.5f);
            world.spawnParticle(Particle.CLOUD, wallCenter, 30, 2, wallHeight / 2.0, 0.5, 0);
        }

        // Schedule wall removal
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            for (org.bukkit.block.Block block : wallBlocks) {
                block.setType(Material.AIR);
                world.spawnParticle(Particle.BLOCK, block.getLocation().add(0.5, 0.5, 0.5),
                        5, 0.3, 0.3, 0.3, 0, wallMaterial.createBlockData());
            }
            world.playSound(wallCenter, Sound.BLOCK_STONE_BREAK, 0.5f, 0.8f);
        }, duration);

        return true;
    }
}
