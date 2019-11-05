package com.github.atomishere.atomuhc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

@RequiredArgsConstructor
public class GameHandler implements Listener {
    private static String GENERATOR_SETTINGS = "{\"coordinateScale\":684.412,\"heightScale\":684.412,\"lowerLimitScale\":512.0,\"upperLimitScale\":512.0,\"depthNoiseScaleX\":200.0,\"depthNoiseScaleZ\":200.0,\"depthNoiseScaleExponent\":0.5,\"mainNoiseScaleX\":80.0,\"mainNoiseScaleY\":160.0,\"mainNoiseScaleZ\":80.0,\"baseSize\":8.5,\"stretchY\":12.0,\"biomeDepthWeight\":1.0,\"biomeDepthOffset\":0.0,\"biomeScaleWeight\":1.0,\"biomeScaleOffset\":0.0,\"seaLevel\":63,\"useCaves\":true,\"useDungeons\":true,\"dungeonChance\":8,\"useStrongholds\":true,\"useVillages\":true,\"useMineShafts\":true,\"useTemples\":true,\"useMonuments\":true,\"useRavines\":true,\"useWaterLakes\":true,\"waterLakeChance\":4,\"useLavaLakes\":true,\"lavaLakeChance\":80,\"useLavaOceans\":false,\"fixedBiome\":-1,\"biomeSize\":4,\"riverSize\":4,\"dirtSize\":33,\"dirtCount\":10,\"dirtMinHeight\":0,\"dirtMaxHeight\":256,\"gravelSize\":33,\"gravelCount\":8,\"gravelMinHeight\":0,\"gravelMaxHeight\":256,\"graniteSize\":33,\"graniteCount\":10,\"graniteMinHeight\":0,\"graniteMaxHeight\":80,\"dioriteSize\":33,\"dioriteCount\":10,\"dioriteMinHeight\":0,\"dioriteMaxHeight\":80,\"andesiteSize\":33,\"andesiteCount\":10,\"andesiteMinHeight\":0,\"andesiteMaxHeight\":80,\"coalSize\":17,\"coalCount\":20,\"coalMinHeight\":0,\"coalMaxHeight\":128,\"ironSize\":15,\"ironCount\":20,\"ironMinHeight\":0,\"ironMaxHeight\":64,\"goldSize\":15,\"goldCount\":20,\"goldMinHeight\":0,\"goldMaxHeight\":64,\"redstoneSize\":8,\"redstoneCount\":8,\"redstoneMinHeight\":0,\"redstoneMaxHeight\":16,\"diamondSize\":15,\"diamondCount\":20,\"diamondMinHeight\":0,\"diamondMaxHeight\":64,\"lapisSize\":15,\"lapisCount\":20,\"lapisCenterHeight\":16,\"lapisSpread\":64}";
    private static String FLAT_SETTINGS = "3;minecraft:bedrock,2*minecraft:dirt,minecraft:grass;1;";

    private final AtomUHC plugin;

    private boolean gameStarted = false;
    @Setter(AccessLevel.PRIVATE)
    private boolean grace = true;

    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    private World currentWorld = null;

    private DMTimer dmTask = null;

    public void start() {
        if(gameStarted) {
            return;
        }

        World world = new WorldCreator("uhcWorld")
                .type(WorldType.CUSTOMIZED)
                .generatorSettings(GENERATOR_SETTINGS)
                .createWorld();

        world.getWorldBorder().setSize(2000);
        currentWorld = world;

        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            RandUtils.randomTp(player, world);
            player.setMaxHealth(40);
            player.setHealth(40);
        }

        gameStarted = true;

        DMTimer dmTask = new DMTimer(plugin);

        dmTask.runTaskTimer(plugin, 20L, 20L);
    }

    public void stop() {
        if(!gameStarted) {
            return;
        }

        try {
            dmTask.cancel();
        } catch(NullPointerException ignored) {
        }

        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.teleport(plugin.getLobbyWorld().getSpawnLocation());
            player.getInventory().clear();
            player.resetMaxHealth();
            player.setGameMode(GameMode.SPECTATOR);
        }

        File worldFile = currentWorld.getWorldFolder();
        Bukkit.getServer().unloadWorld(currentWorld, false);
        delDir(worldFile);

        currentWorld = null;

        gameStarted = false;
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        if(gameStarted) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
        } else {
            return;
        }

        int alivePlayers = 0;
        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            if(player.getGameMode().equals(GameMode.SURVIVAL)) {
                alivePlayers++;
            }
        }

        if(alivePlayers <= 1) {
            stop();
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        if(!gameStarted) {
            return;
        }

        int alivePlayers = 0;
        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            if(player.getGameMode().equals(GameMode.SURVIVAL)) {
                alivePlayers++;
            }
        }

        if(alivePlayers <= 1) {
            stop();
        }
    }

    @EventHandler
    public void onPlayerLooseHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        if(gameStarted) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Game is currently running.");
        }
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.getPlayer().setGameMode(GameMode.SPECTATOR);
    }

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {
        event.setCancelled(gameStarted && event.getEntity() instanceof Player && event.getRegainReason().equals(EntityRegainHealthEvent.RegainReason.SATIATED));
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        event.setCancelled(!gameStarted || (grace && event.getEntity() instanceof Player));
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM));
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        event.setCancelled(!gameStarted);
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        event.setCancelled(!gameStarted);
    }

    public static void delDir(File dir) {
        String[] entries = dir.list();

        if(entries != null) {
            for (String entry : entries) {
                File currentFile = new File(dir.getPath(), entry);

                if (!currentFile.exists()) {
                    continue;
                }

                if (currentFile.isDirectory()) {
                    delDir(currentFile);
                } else {
                    currentFile.delete();
                }
            }
        }

        dir.delete();
    }

    @RequiredArgsConstructor
    public static class DMTimer extends BukkitRunnable {
        private final AtomUHC plugin;

        private int count = 0;
        @Override
        public void run() {
            count++;

            if(count >= 595) {
                if(count > 600) {
                    loadDm();
                    cancel();
                    return;
                }

                for(Player player : Bukkit.getServer().getOnlinePlayers()) {
                    player.sendMessage(ChatColor.GREEN + "Deathmatch will begin in " + (600 - count) + " seconds.");
                    player.getWorld().playSound(player.getLocation(), Sound.NOTE_PLING, 1F, 2F);
                }
            }
        }

        public void loadDm() {
            World dmWorld = new WorldCreator("dmworld")
                    .type(WorldType.FLAT)
                    .generatorSettings(FLAT_SETTINGS)
                    .createWorld();


            dmWorld.getWorldBorder().setSize(100);

            for(Player player : Bukkit.getServer().getOnlinePlayers()) {
                RandUtils.randomTp(player, dmWorld);
            }
            plugin.getHandler().setGrace(false);

            File worldFile = plugin.getHandler().getCurrentWorld().getWorldFolder();
            Bukkit.getServer().unloadWorld(plugin.getHandler().getCurrentWorld(), false);
            delDir(worldFile);

            plugin.getHandler().setCurrentWorld(dmWorld);
        }
    }
}
