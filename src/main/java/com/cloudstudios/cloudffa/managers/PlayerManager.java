package com.cloudstudios.cloudffa.managers;

import com.cloudstudios.cloudffa.CloudFFA;
import com.cloudstudios.cloudffa.objects.FFAPlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {

    private final CloudFFA plugin;
    private final Map<UUID, FFAPlayer> players = new HashMap<>();

    private final File dataFile;
    private final FileConfiguration dataConfig;

    public PlayerManager(CloudFFA plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public FFAPlayer getPlayer(Player p) {
        return players.computeIfAbsent(p.getUniqueId(), uuid -> {
            FFAPlayer fp = new FFAPlayer(uuid);
            String path = "players." + uuid + ".";
            fp.setKills(dataConfig.getInt(path + "kills"));
            fp.setDeaths(dataConfig.getInt(path + "deaths"));
            fp.setWins(dataConfig.getInt(path + "wins"));
            fp.setBestKillstreak(dataConfig.getInt(path + "bestKillstreak"));
            return fp;
        });
    }

    public FFAPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public void saveAll() {
        for (FFAPlayer fp : players.values()) {
            String path = "players." + fp.getUuid() + ".";
            dataConfig.set(path + "kills", fp.getKills());
            dataConfig.set(path + "deaths", fp.getDeaths());
            dataConfig.set(path + "wins", fp.getWins());
            dataConfig.set(path + "bestKillstreak", fp.getBestKillstreak());
        }
        try {
            dataConfig.save(dataFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save players.yml: " + e.getMessage());
        }
    }

    public Map<UUID, FFAPlayer> getAll() { return players; }
}