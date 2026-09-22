package com.cloudstudios.cloudffa.commands;

import com.cloudstudios.cloudffa.CloudFFA;
import com.cloudstudios.cloudffa.objects.Arena;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FFAAdminCommand implements CommandExecutor, TabCompleter {

    private final CloudFFA plugin;

    public FFAAdminCommand(CloudFFA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("cloudffa.admin")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(sender, args);
            case "delete" -> handleDelete(sender, args);
            case "setspawn" -> handleSetLocation(sender, args, "spawn");
            case "setexitlocation" -> handleSetLocation(sender, args, "exit");
            case "setbordercenter" -> handleSetBorderCenter(sender, args);
            case "savekit" -> handleSaveKit(sender, args);
            case "start" -> handleStart(sender, args);
            case "stop" -> handleStop(sender, args);
            case "stopall" -> handleStopAll(sender);
            case "reload" -> handleReload(sender);
            default -> plugin.getMessageUtil().send(sender, "unknown-command");
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        String pre = plugin.getMessageUtil().prefix();
        sender.sendMessage(pre + "§7/ffaadmin create <arena>");
        sender.sendMessage(pre + "§7/ffaadmin delete <arena>");
        sender.sendMessage(pre + "§7/ffaadmin setspawn <arena>");
        sender.sendMessage(pre + "§7/ffaadmin setexitlocation <arena>");
        sender.sendMessage(pre + "§7/ffaadmin setbordercenter <arena> [radius]");
        sender.sendMessage(pre + "§7/ffaadmin savekit <arena>");
        sender.sendMessage(pre + "§7/ffaadmin start <arena>");
        sender.sendMessage(pre + "§7/ffaadmin stop <arena>");
        sender.sendMessage(pre + "§7/ffaadmin stopall");
        sender.sendMessage(pre + "§7/ffaadmin reload");
    }

    private void handleCreate(CommandSender sender, String[] args) {
        if (args.length < 2) return;
        if (plugin.getArenaManager().getArena(args[1]) != null) {
            plugin.getMessageUtil().send(sender, "arena-exists");
            return;
        }
        plugin.getArenaManager().createArena(args[1]);
        plugin.getArenaManager().saveArenas();
        plugin.getMessageUtil().send(sender, "arena-created", "%arena%", args[1]);
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) return;
        plugin.getArenaManager().deleteArena(args[1]);
        plugin.getMessageUtil().send(sender, "arena-deleted", "%arena%", args[1]);
    }

    private void handleSetLocation(CommandSender sender, String[] args, String type) {
        if (!(sender instanceof Player p)) {
            plugin.getMessageUtil().send(sender, "player-only");
            return;
        }
        if (args.length < 2) return;
        Arena arena = plugin.getArenaManager().getArena(args[1]);
        if (arena == null) {
            plugin.getMessageUtil().send(sender, "arena-not-found");
            return;
        }
        switch (type) {
            case "spawn" -> arena.setSpawn(p.getLocation());
            case "exit" -> arena.setExitLocation(p.getLocation());
        }
        plugin.getArenaManager().saveArenas();
        plugin.getMessageUtil().send(sender, "arena-updated", "%arena%", arena.getName());
    }

    /**
     * Sets the border center. Optional radius (3rd arg) = border diameter = radius * 2.
     */
    private void handleSetBorderCenter(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) {
            plugin.getMessageUtil().send(sender, "player-only");
            return;
        }
        if (args.length < 2) {
            plugin.getMessageUtil().send(sender, "usage-setbordercenter");
            return;
        }
        Arena arena = plugin.getArenaManager().getArena(args[1]);
        if (arena == null) {
            plugin.getMessageUtil().send(sender, "arena-not-found");
            return;
        }

        arena.setBorderCenter(p.getLocation());

        if (args.length >= 3) {
            try {
                double radius = Double.parseDouble(args[2]);
                if (radius <= 0) {
                    plugin.getMessageUtil().send(sender, "border-invalid-size");
                    return;
                }
                arena.setBorderDefaultSize(radius * 2);
                plugin.getArenaManager().saveArenas();
                plugin.getMessageUtil().send(sender, "border-set",
                        "%arena%", arena.getName(),
                        "%size%", String.valueOf((int) (radius * 2)),
                        "%radius%", String.valueOf((int) radius));
                return;
            } catch (NumberFormatException e) {
                plugin.getMessageUtil().send(sender, "border-invalid-size");
                return;
            }
        }

        plugin.getArenaManager().saveArenas();
        plugin.getMessageUtil().send(sender, "border-center-set",
                "%arena%", arena.getName());
    }

    private void handleSaveKit(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) {
            plugin.getMessageUtil().send(sender, "player-only");
            return;
        }
        if (args.length < 2) return;
        Arena arena = plugin.getArenaManager().getArena(args[1]);
        if (arena == null) {
            plugin.getMessageUtil().send(sender, "arena-not-found");
            return;
        }
        arena.setKitContents(p.getInventory().getContents().clone());
        arena.setKitArmor(p.getInventory().getArmorContents().clone());
        arena.setKitOffhand(p.getInventory().getItemInOffHand());
        plugin.getArenaManager().saveArenas();
        plugin.getMessageUtil().send(sender, "arena-kit-set", "%arena%", args[1]);
    }

    private void handleStart(CommandSender sender, String[] args) {
        if (args.length < 2) return;
        Arena arena = plugin.getArenaManager().getArena(args[1]);
        if (arena == null) {
            plugin.getMessageUtil().send(sender, "arena-not-found");
            return;
        }
        if (arena.isRunning()) {
            plugin.getMessageUtil().send(sender, "arena-already-running");
            return;
        }
        if (arena.getSpawn() == null) {
            plugin.getMessageUtil().send(sender, "arena-incomplete");
            return;
        }
        plugin.getArenaManager().startArena(arena);
        plugin.getMessageUtil().send(sender, "arena-started", "%arena%", args[1]);
    }

    private void handleStop(CommandSender sender, String[] args) {
        if (args.length < 2) return;
        Arena arena = plugin.getArenaManager().getArena(args[1]);
        if (arena == null) {
            plugin.getMessageUtil().send(sender, "arena-not-found");
            return;
        }
        if (!arena.isRunning()) {
            plugin.getMessageUtil().send(sender, "arena-not-running");
            return;
        }
        plugin.getArenaManager().stopArena(arena);
        plugin.getMessageUtil().send(sender, "arena-stopped", "%arena%", args[1]);
    }

    private void handleStopAll(CommandSender sender) {
        plugin.getArenaManager().stopAllArenas();
        plugin.getMessageUtil().send(sender, "arena-stopped-all");
    }

    private void handleReload(CommandSender sender) {
        // Reload config
        plugin.reloadConfig();

        // Reload language
        plugin.getLanguageManager().reload();

        // Clear all scoreboards first (stale data)
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            plugin.getScoreboardManager().removeScoreboard(p);
        }

        // Reload arenas (stops all running, clears cache)
        plugin.getArenaManager().loadArenas();

        // Re-apply scoreboards for players still in an arena
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            Arena arena = plugin.getArenaManager().getArenaOfPlayer(p);
            if (arena != null) {
                plugin.getScoreboardManager().updateScoreboard(p);
            }
        }

        plugin.getMessageUtil().send(sender, "reloaded");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String s : List.of("create", "delete", "setspawn",
                    "setexitlocation", "setbordercenter", "savekit",
                    "start", "stop", "stopall", "reload")) {
                if (s.startsWith(args[0].toLowerCase())) out.add(s);
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("create")) return out;
            for (Arena a : plugin.getArenaManager().getArenas()) {
                if (a.getName().toLowerCase().startsWith(args[1].toLowerCase())) out.add(a.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("setbordercenter")) {
            out.add("50");
            out.add("75");
            out.add("100");
            out.add("150");
            out.add("200");
            out.add("300");
        }
        return out;
    }
}