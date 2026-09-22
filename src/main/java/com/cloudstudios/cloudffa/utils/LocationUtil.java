package com.cloudstudios.cloudffa.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public final class LocationUtil {

    private LocationUtil() {}

    public static String serialize(Location loc) {
        if (loc == null || loc.getWorld() == null) return null;
        return loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ()
                + "," + loc.getYaw() + "," + loc.getPitch();
    }

    public static Location deserialize(String s) {
        if (s == null || s.isEmpty()) return null;
        String[] p = s.split(",");
        if (p.length < 4) return null;
        try {
            return new Location(Bukkit.getWorld(p[0]),
                    Double.parseDouble(p[1]),
                    Double.parseDouble(p[2]),
                    Double.parseDouble(p[3]),
                    p.length > 4 ? Float.parseFloat(p[4]) : 0f,
                    p.length > 5 ? Float.parseFloat(p[5]) : 0f);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}