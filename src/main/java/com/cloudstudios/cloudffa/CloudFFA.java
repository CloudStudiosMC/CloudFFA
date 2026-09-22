package com.cloudstudios.cloudffa;

import com.cloudstudios.cloudffa.commands.FFAAdminCommand;
import com.cloudstudios.cloudffa.commands.FFACommand;
import com.cloudstudios.cloudffa.commands.LanguageCommand;
import com.cloudstudios.cloudffa.listeners.CombatListener;
import com.cloudstudios.cloudffa.listeners.PlayerListener;
import com.cloudstudios.cloudffa.managers.ArenaManager;
import com.cloudstudios.cloudffa.managers.LanguageManager;
import com.cloudstudios.cloudffa.managers.PlayerManager;
import com.cloudstudios.cloudffa.managers.ScoreboardManager;
import com.cloudstudios.cloudffa.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class CloudFFA extends JavaPlugin {

    private static CloudFFA instance;

    private ArenaManager arenaManager;
    private PlayerManager playerManager;
    private ScoreboardManager scoreboardManager;
    private LanguageManager languageManager;
    private MessageUtil messageUtil;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.languageManager = new LanguageManager(this);
        this.languageManager.loadAll();

        this.messageUtil = new MessageUtil(this);
        this.arenaManager = new ArenaManager(this);
        this.playerManager = new PlayerManager(this);
        this.scoreboardManager = new ScoreboardManager(this);

        arenaManager.loadArenas();

        getCommand("ffa").setExecutor(new FFACommand(this));
        getCommand("ffa").setTabCompleter(new FFACommand(this));
        getCommand("ffaadmin").setExecutor(new FFAAdminCommand(this));
        getCommand("ffaadmin").setTabCompleter(new FFAAdminCommand(this));
        getCommand("language").setExecutor(new LanguageCommand(this));
        getCommand("language").setTabCompleter(new LanguageCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);

        // Clear all scoreboards on enable (removes stale data from previous plugin version)
        getServer().getScheduler().runTaskLater(this, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }, 20L);

        getLogger().info("CloudFFA enabled! Language: " + languageManager.getCurrentLanguage());
    }

    @Override
    public void onDisable() {
        // Clear all scoreboards and bossbars
        if (scoreboardManager != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                scoreboardManager.removeScoreboard(p);
            }
        }

        // Remove all bossbars
        if (arenaManager != null) {
            arenaManager.clearAllBossBars();
        }

        if (playerManager != null) playerManager.saveAll();
        if (arenaManager != null) {
            arenaManager.stopAllArenas();
            arenaManager.saveArenas();
        }
        getLogger().info("CloudFFA disabled!");
    }

    public static CloudFFA getInstance() { return instance; }
    public ArenaManager getArenaManager() { return arenaManager; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public MessageUtil getMessageUtil() { return messageUtil; }
}