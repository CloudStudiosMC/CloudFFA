package com.cloudstudios.cloudffa.managers;

import com.cloudstudios.cloudffa.CloudFFA;
import com.cloudstudios.cloudffa.objects.Arena;
import com.cloudstudios.cloudffa.objects.FFAPlayer;
import com.cloudstudios.cloudffa.utils.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardManager {

    private final CloudFFA plugin;

    public ScoreboardManager(CloudFFA plugin) {
        this.plugin = plugin;
    }

    public void updateScoreboard(Player player) {
        Arena arena = plugin.getArenaManager().getArenaOfPlayer(player);
        if (arena == null) return;

        FFAPlayer fp = plugin.getPlayerManager().getPlayer(player);
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();

        // Create a fresh scoreboard every time (avoids stale data)
        Scoreboard board = manager.getNewScoreboard();

        String titleRaw = arena.getScoreboardTitle();
        if (titleRaw == null) {
            titleRaw = "<gradient:#00d4a0:#1a8c7a><bold>⚔ CLOUD FFA ⚔</bold></gradient>";
        }
        String title = ColorUtil.colorize(titleRaw);

        Objective obj = board.registerNewObjective("cloudffa", "dummy", title);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = arena.getScoreboardLines();
        if (lines == null || lines.isEmpty()) {
            lines = getDefaultLines();
        }

        List<String> processed = new ArrayList<>();
        for (String line : lines) {
            String result = applyPlaceholders(line, fp, arena);
            processed.add(result);
            if (processed.size() >= 15) break;
        }

        int score = processed.size();
        for (String line : processed) {
            setLine(board, obj, score, line);
            score--;
        }

        player.setScoreboard(board);
    }

    private String applyPlaceholders(String line, FFAPlayer fp, Arena arena) {
        if (line == null) return "";
        String playerName = Bukkit.getOfflinePlayer(fp.getUuid()).getName();
        if (playerName == null) playerName = "Player";

        int alive = arena != null ? arena.getAlive().size() : 0;
        int total = arena != null ? arena.getPlayers().size() : 0;

        int elapsed = arena != null ? arena.getElapsedSeconds() : 0;
        int joinPhase = arena != null ? arena.getJoinPhaseSeconds() : 0;
        int remaining = Math.max(0, joinPhase - elapsed);

        return line
                .replace("%player%", playerName)
                .replace("%kills%", String.valueOf(fp.getKills()))
                .replace("%deaths%", String.valueOf(fp.getDeaths()))
                .replace("%wins%", String.valueOf(fp.getWins()))
                .replace("%kd%", String.format("%.2f", fp.getKDR()))
                .replace("%streak%", String.valueOf(fp.getKillstreak()))
                .replace("%best_streak%", String.valueOf(fp.getBestKillstreak()))
                .replace("%arena%", arena != null ? arena.getName() : "-")
                .replace("%alive%", String.valueOf(alive))
                .replace("%players%", String.valueOf(total))
                .replace("%remaining%", String.valueOf(remaining))
                .replace("%world%", arena != null && arena.getSpawn() != null && arena.getSpawn().getWorld() != null
                        ? arena.getSpawn().getWorld().getName() : "-")
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()));
    }

    private void setLine(Scoreboard board, Objective obj, int score, String text) {
        String colored = ColorUtil.colorize(text);
        String entry = colored + "§r§" + getUniqueSuffix(score);

        Team team = board.registerNewTeam("line_" + score);
        team.addEntry(entry);
        obj.getScore(entry).setScore(score);
    }

    private String getUniqueSuffix(int score) {
        char[] chars = "abcdefghijklmnop".toCharArray();
        return "§" + chars[score % chars.length] + "§r";
    }

    private List<String> getDefaultLines() {
        List<String> list = new ArrayList<>();
        list.add("<dark_gray>┌────────────────────┐");
        list.add("<gray>  Player: <white>%player%");
        list.add("<gray>  Arena: <aqua>%arena%");
        list.add("<dark_gray>└────────────────────┘");
        list.add("");
        list.add("<gradient:#00d4a0:#1a8c7a><bold>  » Match Info «</bold></gradient>");
        list.add("<gray>  Alive: <green>%alive%<dark_gray>/<green>%players%");
        list.add("<gray>  Countdown: <red>%remaining%s");
        list.add("");
        list.add("<gradient:#00d4a0:#1a8c7a><bold>  » Your Stats «</bold></gradient>");
        list.add("<gray>  Kills: <white>%kills%");
        list.add("<gray>  Streak: <yellow>%streak%");
        list.add("<gray>  Best: <gold>%best_streak%");
        list.add("<gray>  Wins: <light_purple>%wins%");
        return list;
    }

    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}