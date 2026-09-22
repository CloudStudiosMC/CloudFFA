package com.cloudstudios.cloudffa.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private ColorUtil() {}

    private static final Pattern HEX_PATTERN = Pattern.compile("<#([A-Fa-f0-9]{6})>");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile(
            "<gradient:#([A-Fa-f0-9]{6}):#([A-Fa-f0-9]{6})>(.*?)</gradient>");

    public static String colorize(String input) {
        if (input == null || input.isEmpty()) return "";

        String result = input;

        // 1) Gradient
        Matcher grad = GRADIENT_PATTERN.matcher(result);
        while (grad.find()) {
            String color = grad.group(1);
            String text = grad.group(3);
            result = result.replace(grad.group(0), hexToLegacy(color) + text);
        }

        // 2) Hex színek
        Matcher hex = HEX_PATTERN.matcher(result);
        while (hex.find()) {
            result = result.replace(hex.group(0), hexToLegacy(hex.group(1)));
        }

        // 3) MiniMessage tag-ek cseréje (NYITÓ és ZÁRÓ is!)
        result = result
                .replace("<black>", "§0").replace("</black>", "§r")
                .replace("<dark_blue>", "§1").replace("</dark_blue>", "§r")
                .replace("<dark_green>", "§2").replace("</dark_green>", "§r")
                .replace("<dark_aqua>", "§3").replace("</dark_aqua>", "§r")
                .replace("<dark_red>", "§4").replace("</dark_red>", "§r")
                .replace("<dark_purple>", "§5").replace("</dark_purple>", "§r")
                .replace("<gold>", "§6").replace("</gold>", "§r")
                .replace("<gray>", "§7").replace("</gray>", "§r")
                .replace("<dark_gray>", "§8").replace("</dark_gray>", "§r")
                .replace("<blue>", "§9").replace("</blue>", "§r")
                .replace("<green>", "§a").replace("</green>", "§r")
                .replace("<aqua>", "§b").replace("</aqua>", "§r")
                .replace("<red>", "§c").replace("</red>", "§r")
                .replace("<light_purple>", "§d").replace("</light_purple>", "§r")
                .replace("<yellow>", "§e").replace("</yellow>", "§r")
                .replace("<white>", "§f").replace("</white>", "§r")
                .replace("<bold>", "§l").replace("</bold>", "§r")
                .replace("<italic>", "§o").replace("</italic>", "§r")
                .replace("<underlined>", "§n").replace("</underlined>", "§r")
                .replace("<strikethrough>", "§m").replace("</strikethrough>", "§r")
                .replace("<obfuscated>", "§k").replace("</obfuscated>", "§r")
                .replace("<reset>", "§r");

        // 4) Legacy & kódok
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    private static String hexToLegacy(String hex) {
        StringBuilder sb = new StringBuilder("§x");
        for (char c : hex.toCharArray()) {
            sb.append("§").append(c);
        }
        return sb.toString();
    }

    public static String strip(String input) {
        if (input == null || input.isEmpty()) return "";
        return ChatColor.stripColor(colorize(input));
    }

    public static String toLegacy(String input) {
        return colorize(input);
    }
}