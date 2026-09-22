package com.cloudstudios.cloudffa.utils;

import com.cloudstudios.cloudffa.CloudFFA;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtil {

    private final CloudFFA plugin;

    public MessageUtil(CloudFFA plugin) {
        this.plugin = plugin;
    }

    public void send(CommandSender sender, String key, String... replacements) {
        String prefix = plugin.getLanguageManager().getPrefixLegacy();
        String msg = plugin.getLanguageManager().getString(key, replacements);
        sender.sendMessage(prefix + msg);
    }

    public void sendNoPrefix(CommandSender sender, String key, String... replacements) {
        sender.sendMessage(plugin.getLanguageManager().getString(key, replacements));
    }

    /**
     * Sends a multi-line message (supports \n) without prefix.
     */
    public void sendMultiline(CommandSender sender, String key, String... replacements) {
        String msg = plugin.getLanguageManager().getString(key, replacements);
        if (msg == null || msg.isEmpty()) return;
        for (String line : msg.split("\\\\n|\\n")) {
            if (!line.isEmpty()) sender.sendMessage(line);
        }
    }

    public void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(ColorUtil.colorize(message));
    }

    public void actionBar(Player player, String key, String... replacements) {
        String msg = plugin.getLanguageManager().getString(key, replacements);
        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(msg)
        );
    }

    public void title(Player player, String titleKey, String subtitleKey) {
        String title = plugin.getLanguageManager().getString(titleKey);
        String subtitle = subtitleKey == null
                ? ""
                : plugin.getLanguageManager().getString(subtitleKey);

        player.sendTitle(title, subtitle, 10, 40, 10);
    }

    public String prefix() {
        return plugin.getLanguageManager().getPrefixLegacy();
    }

    public String getString(String key, String... replacements) {
        return plugin.getLanguageManager().getString(key, replacements);
    }
}