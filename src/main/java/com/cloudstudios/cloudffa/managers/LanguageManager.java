package com.cloudstudios.cloudffa.managers;

import com.cloudstudios.cloudffa.CloudFFA;
import com.cloudstudios.cloudffa.utils.ColorUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LanguageManager {

    private final CloudFFA plugin;
    private final Map<String, FileConfiguration> languages = new HashMap<>();
    private final Map<String, String> languageNames = new LinkedHashMap<>();

    private static final List<String> SUPPORTED = List.of("en", "hu", "de");

    private String currentLanguage;

    public LanguageManager(CloudFFA plugin) {
        this.plugin = plugin;
        languageNames.put("en", "English");
        languageNames.put("hu", "Magyar");
        languageNames.put("de", "Deutsch");
    }

    public void loadAll() {
        languages.clear();

        for (String lang : SUPPORTED) {
            File outFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
            if (!outFile.exists()) {
                if (plugin.getResource("lang/" + lang + ".yml") != null) {
                    plugin.saveResource("lang/" + lang + ".yml", false);
                }
            }

            if (outFile.exists()) {
                languages.put(lang, YamlConfiguration.loadConfiguration(outFile));
            } else {
                InputStream in = plugin.getResource("lang/" + lang + ".yml");
                if (in != null) {
                    YamlConfiguration cfg = YamlConfiguration.loadConfiguration(
                            new InputStreamReader(in, StandardCharsets.UTF_8));
                    languages.put(lang, cfg);
                }
            }
        }

        String configured = plugin.getConfig().getString("language", "en").toLowerCase();
        if (!languages.containsKey(configured)) {
            plugin.getLogger().warning("Language '" + configured + "' not found, falling back to 'en'.");
            configured = "en";
        }
        this.currentLanguage = configured;
        plugin.getLogger().info("Loaded language: " + configured + " (" + getLanguageName(configured) + ")");
    }

    public void reload() {
        loadAll();
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public void setCurrentLanguage(String lang) {
        if (!languages.containsKey(lang)) return;
        this.currentLanguage = lang;
        plugin.getConfig().set("language", lang);
        plugin.saveConfig();
    }

    public String getLanguageName(String code) {
        return languageNames.getOrDefault(code, code);
    }

    public Set<String> getAvailableLanguages() {
        return languages.keySet();
    }

    public String getRaw(String key) {
        FileConfiguration cfg = languages.get(currentLanguage);
        if (cfg != null && cfg.isString(key)) return cfg.getString(key);

        FileConfiguration fallback = languages.get("en");
        if (fallback != null && fallback.isString(key)) return fallback.getString(key);

        return "<red>Missing message: " + key;
    }

    public List<String> getRawList(String key) {
        FileConfiguration cfg = languages.get(currentLanguage);
        if (cfg != null && cfg.isList(key)) return cfg.getStringList(key);

        FileConfiguration fallback = languages.get("en");
        if (fallback != null && fallback.isList(key)) return fallback.getStringList(key);

        return Collections.emptyList();
    }

    public String getString(String key, String... replacements) {
        String raw = getRaw(key);
        raw = applyPlaceholders(raw, replacements);
        return ColorUtil.colorize(raw);
    }

    public String getPrefixLegacy() {
        return ColorUtil.colorize(getRaw("prefix"));
    }

    private String applyPlaceholders(String input, String... replacements) {
        if (input == null) return "";
        if (replacements == null || replacements.length < 2) return input;
        String result = input;
        for (int i = 0; i < replacements.length - 1; i += 2) {
            result = result.replace(replacements[i], replacements[i + 1]);
        }
        return result;
    }
}