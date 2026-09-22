package com.cloudstudios.cloudffa.commands;

import com.cloudstudios.cloudffa.CloudFFA;
import com.cloudstudios.cloudffa.objects.Arena;
import com.cloudstudios.cloudffa.objects.FFAPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FFACommand implements CommandExecutor, TabCompleter {

    private final CloudFFA plugin;

    public FFACommand(CloudFFA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player p) sendHelp(p);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "join" -> handleJoin(sender, args);
            case "leave" -> handleLeave(sender);
            case "stats" -> handleStats(sender);
            case "wins" -> handleWins(sender, args);
            case "list" -> handleList(sender);
            default -> {
                if (sender instanceof Player p) sendHelp(p);
            }
        }
        return true;
    }

    private void sendHelp(Player p) {
        String pre = plugin.getMessageUtil().prefix();
        p.sendMessage(pre + "§7/ffa join <arena>");
        p.sendMessage(pre + "§7/ffa leave");
        p.sendMessage(pre + "§7/ffa stats");
        p.sendMessage(pre + "§7/ffa wins [player]");
        p.sendMessage(pre + "§7/ffa list");
    }

    private void handleJoin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) {
            plugin.getMessageUtil().send(sender, "player-only");
            return;
        }
        if (args.length < 2) {
            plugin.getMessageUtil().send(p, "usage-join");
            return;
        }
        Arena arena = plugin.getArenaManager().getArena(args[1]);
        if (arena == null) {
            plugin.getMessageUtil().send(p, "arena-not-found");
            return;
        }
        if (!arena.isRunning()) {
            plugin.getMessageUtil().send(p, "arena-not-running");
            return;
        }
        if (arena.getPlayers().contains(p.getUniqueId())) {
            plugin.getMessageUtil().send(p, "already-in-ffa");
            return;
        }
        if (!arena.hasKit()) {
            plugin.getMessageUtil().send(p, "arena-no-kit");
            return;
        }

        boolean ok = plugin.getArenaManager().joinArena(arena, p);
        if (ok) {
            plugin.getMessageUtil().send(p, "joined-ffa", "%arena%", arena.getName());
            for (Player other : plugin.getServer().getOnlinePlayers()) {
                if (other.getUniqueId().equals(p.getUniqueId())) continue;
                if (arena.getPlayers().contains(other.getUniqueId())) {
                    plugin.getMessageUtil().send(other, "player-joined-arena",
                            "%player%", p.getName(),
                            "%count%", String.valueOf(arena.getAlive().size()));
                }
            }
        } else {
            plugin.getMessageUtil().send(p, "join-failed");
        }
    }

    private void handleLeave(CommandSender sender) {
        if (!(sender instanceof Player p)) {
            plugin.getMessageUtil().send(sender, "player-only");
            return;
        }
        Arena arena = plugin.getArenaManager().getArenaOfPlayer(p);
        if (arena == null) {
            plugin.getMessageUtil().send(p, "not-in-ffa");
            return;
        }
        plugin.getArenaManager().removePlayerFromArena(arena, p);
        plugin.getMessageUtil().send(p, "left-ffa");

        for (Player other : plugin.getServer().getOnlinePlayers()) {
            if (other.getUniqueId().equals(p.getUniqueId())) continue;
            if (arena.getPlayers().contains(other.getUniqueId())) {
                plugin.getMessageUtil().send(other, "player-left-arena",
                        "%player%", p.getName(),
                        "%count%", String.valueOf(arena.getAlive().size()));
            }
        }
    }

    private void handleStats(CommandSender sender) {
        if (!(sender instanceof Player p)) {
            plugin.getMessageUtil().send(sender, "player-only");
            return;
        }
        FFAPlayer fp = plugin.getPlayerManager().getPlayer(p);
        plugin.getMessageUtil().sendNoPrefix(p, "stats-header");
        plugin.getMessageUtil().sendNoPrefix(p, "stats-title");
        plugin.getMessageUtil().sendNoPrefix(p, "stats-kills", "%kills%", String.valueOf(fp.getKills()));
        plugin.getMessageUtil().sendNoPrefix(p, "stats-deaths", "%deaths%", String.valueOf(fp.getDeaths()));
        plugin.getMessageUtil().sendNoPrefix(p, "stats-wins", "%wins%", String.valueOf(fp.getWins()));
        plugin.getMessageUtil().sendNoPrefix(p, "stats-kd", "%kd%", String.format("%.2f", fp.getKDR()));
        plugin.getMessageUtil().sendNoPrefix(p, "stats-best-streak", "%best%", String.valueOf(fp.getBestKillstreak()));
        plugin.getMessageUtil().sendNoPrefix(p, "stats-footer");
    }

    private void handleWins(CommandSender sender, String[] args) {
        List<FFAPlayer> all = new ArrayList<>(plugin.getPlayerManager().getAll().values());
        all.sort((a, b) -> Integer.compare(b.getWins(), a.getWins()));

        sender.sendMessage("§8§m----------------");
        sender.sendMessage("§b§lTOP WINS");
        int rank = 1;
        for (FFAPlayer fp : all) {
            if (rank > 10) break;
            String name = plugin.getServer().getOfflinePlayer(fp.getUuid()).getName();
            if (name == null) name = fp.getUuid().toString().substring(0, 8);
            sender.sendMessage("§7" + rank + ". §f" + name + " §7- §b" + fp.getWins());
            rank++;
        }
        sender.sendMessage("§8§m----------------");
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage("§8§m----------------");
        sender.sendMessage("§b§lACTIVE ARENAS");
        for (Arena a : plugin.getArenaManager().getArenas()) {
            String status = a.isRunning() ? "§aRUNNING" : "§cSTOPPED";
            sender.sendMessage("§7" + a.getName() + " §8- " + status
                    + " §7(" + a.getAlive().size() + "/" + a.getPlayers().size() + ")");
        }
        sender.sendMessage("§8§m----------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : List.of("join", "leave", "stats", "wins", "list")) {
                if (s.startsWith(args[0].toLowerCase())) out.add(s);
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            for (Arena a : plugin.getArenaManager().getArenas()) {
                if (a.isRunning() && a.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                    out.add(a.getName());
            }
        }
        return out;
    }
}