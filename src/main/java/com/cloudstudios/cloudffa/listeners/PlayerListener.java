package com.cloudstudios.cloudffa.listeners;

import com.cloudstudios.cloudffa.CloudFFA;
import com.cloudstudios.cloudffa.objects.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final CloudFFA plugin;

    public PlayerListener(CloudFFA plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        Arena arena = plugin.getArenaManager().getArenaOfPlayer(p);
        if (arena != null) {
            plugin.getArenaManager().removePlayerFromArena(arena, p);
        }
        plugin.getPlayerManager().saveAll();
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent e) {
        if (e.getEntity() instanceof Player p) {
            Arena arena = plugin.getArenaManager().getArenaOfPlayer(p);
            if (arena != null) {
                e.setCancelled(true);
            }
        }
    }
}