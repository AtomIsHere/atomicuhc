package com.github.atomishere.atomuhc;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

import java.util.Random;

public class RandUtils {
    public static void randomTp(Player player, World world) {
        WorldBorder border = world.getWorldBorder();

        int highBound = Double.valueOf(border.getSize() / 2).intValue();

        int x = genRandom(highBound, 0);
        int z = genRandom(highBound, 0);

        Location randLoc = world.getHighestBlockAt(x, z).getLocation();
        randLoc.setY(randLoc.getY() + 0.3);

        player.teleport(randLoc);
    }

    private static int genRandom(int highBound, int lowBound) {
        return new Random().nextInt(highBound + lowBound) - lowBound;
    }
}
