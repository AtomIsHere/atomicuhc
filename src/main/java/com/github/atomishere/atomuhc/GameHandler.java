package com.github.atomishere.atomuhc;

import lombok.RequiredArgsConstructor;
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
public class GameHandler extends BukkitRunnable implements Listener {
    private final AtomUHC plugin;

    private boolean gameStarted = false;
    private boolean grace = true;

    private World currentWorld = null;

    public void start() {
        World world = new WorldCreator("uhcWorld").createWorld();

        world.getWorldBorder().setSize(2000);
        currentWorld = world;

        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.setGameMode(GameMode.SURVIVAL);
            RandUtils.randomTp(player, world);
            player.setMaxHealth(40);
            player.setHealth(40);
        }

        gameStarted = true;
        runTaskLater(plugin, 6000L);
    }

    public void stop() {
        if(!gameStarted) {
            return;
        }

        cancel();

        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            player.teleport(plugin.getLobbyWorld().getSpawnLocation());
            player.resetMaxHealth();
            player.setGameMode(GameMode.SPECTATOR);
        }

        File worldFile = currentWorld.getWorldFolder();
        Bukkit.getServer().unloadWorld(currentWorld, false);
        worldFile.delete();

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
        event.setCancelled(!gameStarted && !event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.CUSTOM));
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent event) {
        event.setCancelled(!gameStarted);
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        event.setCancelled(!gameStarted);
    }

    @Override
    public void run() {
        World dmWorld = new WorldCreator("dmworld").createWorld();

        dmWorld.getWorldBorder().setSize(500);
        grace = false;
        this.currentWorld = dmWorld;

        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            RandUtils.randomTp(player, dmWorld);
        }

        File worldFile = currentWorld.getWorldFolder();
        Bukkit.getServer().unloadWorld(currentWorld, false);
        worldFile.delete();
    }
}
