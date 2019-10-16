package com.github.atomishere.atomuhc;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class AtomUHC extends JavaPlugin {
    @Getter
    private World lobbyWorld = null;

    @Getter
    private GameHandler handler = null;

    @Override
    public void onEnable() {
        lobbyWorld = Bukkit.getServer().getWorld("world");

        handler = new GameHandler(this);

        Bukkit.getServer().getPluginManager().registerEvents(handler, this);

        this.getCommand("start").setExecutor(new StartCommand(this));
        this.getCommand("stop").setExecutor(new StopCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
