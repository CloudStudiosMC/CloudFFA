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

public class LanguageCommand implements CommandExecutor, TabCompleter {

    private final CloudFFA plugin;

    public LanguageCommand(CloudFFA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("cloudffa.language")) {
            plugin.getMessageUtil().send(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            plugin.getMessageUtil().send(sender, "language-current",
                    "%lang%", plugin.getLanguageManager().getCurrentLanguage());
            return true;
        }

        String lang = args[0].toLowerCase();
        if (!plugin.getLanguageManager().getAvailableLanguages().contains(lang)) {
            plugin.getMessageUtil().send(sender, "language-not-found", "%lang%", lang);
            return true;
        }

        plugin.getLanguageManager().setCurrentLanguage(lang);
        plugin.getMessageUtil().send(sender, "language-changed",
                "%lang%", plugin.getLanguageManager().getLanguageName(lang));

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            Arena arena = plugin.getArenaManager().getArenaOfPlayer(p);
            if (arena != null) {
                plugin.getScoreboardManager().updateScoreboard(p);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            for (String lang : plugin.getLanguageManager().getAvailableLanguages()) {
                if (lang.startsWith(args[0].toLowerCase())) out.add(lang);
            }
        }
        return out;
    }
}