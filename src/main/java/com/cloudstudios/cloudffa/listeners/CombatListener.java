package com.cloudstudios.cloudffa.listeners;

import com.cloudstudios.cloudffa.CloudFFA;
import com.cloudstudios.cloudffa.objects.Arena;
import com.cloudstudios.cloudffa.objects.FFAPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class CombatListener implements Listener {

    private final CloudFFA plugin;

    public CombatListener(CloudFFA plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Arena arena = plugin.getArenaManager().getArenaOfPlayer(victim);
        if (arena == null) return;

        if (!plugin.getConfig().getBoolean("death.broadcast-death-message", false)) {
            e.setDeathMessage(null);
        }

        if (!plugin.getConfig().getBoolean("death.keep-drops", false)) {
            e.getDrops().clear();
        }

        FFAPlayer vp = plugin.getPlayerManager().getPlayer(victim);
        vp.addDeath();
        vp.resetKillstreak();
        plugin.getArenaManager().playerDied(arena, victim);

        Player killer = victim.getKiller();
        if (killer != null) {
            Arena killerArena = plugin.getArenaManager().getArenaOfPlayer(killer);
            if (killerArena != null && killerArena.equals(arena)) {
                FFAPlayer kp = plugin.getPlayerManager().getPlayer(killer);
                kp.addKill();
                kp.incrementKillstreak();
                if (kp.getKillstreak() > kp.getBestKillstreak()) {
                    kp.setBestKillstreak(kp.getKillstreak());
                }

                plugin.getMessageUtil().send(killer, "kill-message",
                        "%kills%", String.valueOf(kp.getKills()),
                        "%streak%", String.valueOf(kp.getKillstreak()));

                plugin.getMessageUtil().send(victim, "death-message",
                        "%killer%", killer.getName());

                plugin.getMessageUtil().actionBar(victim, "death-actionbar",
                        "%alive%", String.valueOf(arena.getAlive().size()));

                plugin.getScoreboardManager().updateScoreboard(killer);
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (victim.isOnline()) {
                plugin.getArenaManager().removePlayerFromArena(arena, victim);
                plugin.getMessageUtil().send(victim, "you-died-removed");
            }
        }, 5L);
    }
}