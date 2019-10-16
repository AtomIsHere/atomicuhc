package com.github.atomishere.atomuhc;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class StopCommand implements CommandExecutor {
    private final AtomUHC plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        plugin.getHandler().stop();
        return true;
    }
}
