package com.cloudstudios.cloudffa.managers;

import com.cloudstudios.cloudffa.CloudFFA;
import com.cloudstudios.cloudffa.objects.Arena;
import com.cloudstudios.cloudffa.objects.FFAPlayer;
import com.cloudstudios.cloudffa.utils.ColorUtil;
import com.cloudstudios.cloudffa.utils.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArenaManager {

    private final CloudFFA plugin;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Map<String, BossBar> arenaBossBars = new HashMap<>();
    private final File arenasFolder;

    public ArenaManager(CloudFFA plugin) {
        this.plugin = plugin;
        this.arenasFolder = new File(plugin.getDataFolder(), "arenas");
        if (!arenasFolder.exists()) arenasFolder.mkdirs();
    }

    public File getArenasFolder() {
        return arenasFolder;
    }

    public File getArenaFile(String name) {
        return new File(arenasFolder, name.toLowerCase() + ".yml");
    }

    // ==================================================
    //  LOAD
    // ==================================================

    public void loadArenas() {
        // Stop all running arenas first (prevents stale state)
        for (Arena a : new ArrayList<>(arenas.values())) {
            if (a.isRunning()) stopArena(a);
        }

        arenas.clear();
        arenaBossBars.clear();

        if (!arenasFolder.exists()) {
            arenasFolder.mkdirs();
            return;
        }

        File[] files = arenasFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String arenaName = file.getName().substring(0, file.getName().length() - 4);
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);

            Arena arena = new Arena(arenaName);

            arena.setSpawn(LocationUtil.deserialize(cfg.getString("spawn")));
            arena.setExitLocation(LocationUtil.deserialize(cfg.getString("exit-location")));
            arena.setBorderCenter(LocationUtil.deserialize(cfg.getString("border-center")));

            List<?> contents = cfg.getList("kit.contents");
            List<?> armor = cfg.getList("kit.armor");
            if (contents != null) arena.setKitContents(contents.toArray(new ItemStack[0]));
            if (armor != null) arena.setKitArmor(armor.toArray(new ItemStack[0]));
            arena.setKitOffhand(cfg.getItemStack("kit.offhand"));

            arena.setJoinPhaseSeconds(cfg.getInt("join-phase-seconds", 120));

            arena.setDefaultEffects(cfg.getStringList("effects.defaults"));
            if (cfg.getConfigurationSection("effects.phases") != null) {
                for (String phaseKey : cfg.getConfigurationSection("effects.phases").getKeys(false)) {
                    try {
                        int start = Integer.parseInt(phaseKey);
                        arena.getTimedEffects().put(start, cfg.getStringList("effects.phases." + phaseKey));
                    } catch (NumberFormatException ignored) {}
                }
            }

            arena.setBorderDefaultSize(cfg.getDouble("border.default-size", 150));
            arena.setBorderFinalSize(cfg.getDouble("border.final-size", 20));
            if (cfg.getConfigurationSection("border.phases") != null) {
                for (String phaseKey : cfg.getConfigurationSection("border.phases").getKeys(false)) {
                    try {
                        int start = Integer.parseInt(phaseKey);
                        double size = cfg.getDouble("border.phases." + phaseKey + ".size", 50);
                        double seconds = cfg.getDouble("border.phases." + phaseKey + ".seconds", 5);
                        arena.getBorderPhases().put(start, new double[]{size, seconds});
                    } catch (NumberFormatException ignored) {}
                }
            }

            arena.setWinnerRewardCommands(cfg.getStringList("winner-rewards.commands"));

            // Scoreboard
            if (cfg.isConfigurationSection("scoreboard")) {
                arena.setScoreboardTitle(cfg.getString("scoreboard.title", null));
                List<String> lines = cfg.getStringList("scoreboard.lines");
                if (lines != null && !lines.isEmpty()) {
                    arena.setScoreboardLines(lines);
                }
            }

            // BossBar
            if (cfg.isConfigurationSection("bossbar")) {
                arena.setBossBarEnabled(cfg.getBoolean("bossbar.enabled", true));
                arena.setBossBarTitleJoin(cfg.getString("bossbar.join-title"));
                arena.setBossBarTitleFight(cfg.getString("bossbar.fight-title"));
                arena.setBossBarColor(cfg.getString("bossbar.color", "GREEN"));
                arena.setBossBarStyle(cfg.getString("bossbar.style", "SOLID"));
            }

            arenas.put(arenaName.toLowerCase(), arena);
        }
    }

    /**
     * Removes all bossbars (used on plugin disable).
     */
    public void clearAllBossBars() {
        for (BossBar bar : arenaBossBars.values()) {
            bar.removeAll();
        }
        arenaBossBars.clear();
    }

    // ==================================================
    //  SAVE
    // ==================================================

    public void saveArena(Arena arena) {
        File file = getArenaFile(arena.getName());
        FileConfiguration cfg = new YamlConfiguration();

        cfg.set("name", arena.getName());
        if (arena.getSpawn() != null) cfg.set("spawn", LocationUtil.serialize(arena.getSpawn()));
        if (arena.getExitLocation() != null) cfg.set("exit-location", LocationUtil.serialize(arena.getExitLocation()));
        if (arena.getBorderCenter() != null) cfg.set("border-center", LocationUtil.serialize(arena.getBorderCenter()));

        cfg.set("join-phase-seconds", arena.getJoinPhaseSeconds());

        if (arena.getKitContents() != null) cfg.set("kit.contents", Arrays.asList(arena.getKitContents()));
        if (arena.getKitArmor() != null) cfg.set("kit.armor", Arrays.asList(arena.getKitArmor()));
        if (arena.getKitOffhand() != null) cfg.set("kit.offhand", arena.getKitOffhand());

        cfg.set("effects.defaults", arena.getDefaultEffects());
        for (Map.Entry<Integer, List<String>> entry : arena.getTimedEffects().entrySet()) {
            cfg.set("effects.phases." + entry.getKey(), entry.getValue());
        }

        cfg.set("border.default-size", arena.getBorderDefaultSize());
        cfg.set("border.final-size", arena.getBorderFinalSize());
        for (Map.Entry<Integer, double[]> entry : arena.getBorderPhases().entrySet()) {
            cfg.set("border.phases." + entry.getKey() + ".size", entry.getValue()[0]);
            cfg.set("border.phases." + entry.getKey() + ".seconds", entry.getValue()[1]);
        }

        cfg.set("winner-rewards.commands", arena.getWinnerRewardCommands());

        // Scoreboard
        if (arena.getScoreboardTitle() != null) {
            cfg.set("scoreboard.title", arena.getScoreboardTitle());
        }
        if (arena.getScoreboardLines() != null && !arena.getScoreboardLines().isEmpty()) {
            cfg.set("scoreboard.lines", arena.getScoreboardLines());
        }

        // BossBar
        cfg.set("bossbar.enabled", arena.isBossBarEnabled());
        if (arena.getBossBarTitleJoin() != null) cfg.set("bossbar.join-title", arena.getBossBarTitleJoin());
        if (arena.getBossBarTitleFight() != null) cfg.set("bossbar.fight-title", arena.getBossBarTitleFight());
        cfg.set("bossbar.color", arena.getBossBarColor());
        cfg.set("bossbar.style", arena.getBossBarStyle());

        try {
            cfg.save(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Could not save arena file " + file.getName() + ": " + e.getMessage());
        }
    }

    public void saveArenas() {
        for (Arena a : arenas.values()) {
            saveArena(a);
        }
    }

    // ==================================================
    //  GET / CREATE / DELETE
    // ==================================================

    public Arena getArena(String name) {
        if (name == null) return null;
        return arenas.get(name.toLowerCase());
    }

    public Arena createArena(String name) {
        Arena a = new Arena(name);

        a.setJoinPhaseSeconds(120);
        a.setBorderDefaultSize(150);
        a.setBorderFinalSize(20);

        List<String> defaultEffects = new ArrayList<>();
        defaultEffects.add("SPEED:0:999999");
        a.setDefaultEffects(defaultEffects);

        a.getTimedEffects().put(0, List.of("SPEED:0:999999"));
        a.getTimedEffects().put(120, List.of("SPEED:1:999999", "STRENGTH:0:999999"));

        a.getBorderPhases().put(120, new double[]{120, 30});
        a.getBorderPhases().put(180, new double[]{80, 30});
        a.getBorderPhases().put(240, new double[]{50, 30});

        List<String> rewardCmds = new ArrayList<>();
        rewardCmds.add("give %player% diamond 5");
        rewardCmds.add("eco give %player% 1000");
        a.setWinnerRewardCommands(rewardCmds);

        // Default BossBar
        a.setBossBarEnabled(true);
        a.setBossBarTitleJoin("<gradient:#00d4a0:#1a8c7a>%alive% players</gradient> <dark_gray>| <white>PvP in <yellow>%remaining%s");
        a.setBossBarTitleFight("<gradient:#00d4a0:#1a8c7a>%alive% players alive</gradient>");
        a.setBossBarColor("GREEN");
        a.setBossBarStyle("SOLID");

        arenas.put(name.toLowerCase(), a);
        saveArena(a);
        return a;
    }

    public void deleteArena(String name) {
        Arena a = getArena(name);
        if (a != null && a.isRunning()) stopArena(a);
        arenas.remove(name.toLowerCase());
        arenaBossBars.remove(name.toLowerCase());

        File file = getArenaFile(name);
        if (file.exists()) file.delete();
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    // ==================================================
    //  START / STOP
    // ==================================================

    public void startArena(Arena arena) {
        if (arena.isRunning()) return;
        if (!arena.hasKit()) {
            plugin.getLogger().warning("Arena " + arena.getName() + " has no kit!");
        }
        if (arena.getSpawn() == null) {
            plugin.getLogger().warning("Arena " + arena.getName() + " has no spawn!");
            return;
        }

        arena.setRunning(true);
        arena.setJoinPhase(true);
        arena.getPlayers().clear();
        arena.getAlive().clear();

        Location center = arena.getBorderCenter() != null ? arena.getBorderCenter() : arena.getSpawn();
        World world = center.getWorld();
        if (world != null) {
            WorldBorder border = world.getWorldBorder();
            border.setCenter(center);
            border.setSize(arena.getBorderDefaultSize());
        }

        // Create BossBar
        BossBar bar = Bukkit.createBossBar(
                ColorUtil.colorize("<gradient:#00d4a0:#1a8c7a>Waiting for players...</gradient>"),
                BarColor.GREEN,
                BarStyle.SOLID
        );
        arenaBossBars.put(arena.getName().toLowerCase(), bar);

        // START broadcast to EVERYONE
        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getMessageUtil().sendMultiline(p, "event-start-format",
                    "%arena%", arena.getName(),
                    "%seconds%", String.valueOf(arena.getJoinPhaseSeconds()));
        }

        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!arena.isRunning()) return;
            tickArena(arena);
        }, 20L, 20L).getTaskId();

        arena.setTaskId(taskId);
    }

    private void tickArena(Arena arena) {
        int elapsed = arena.getElapsedSeconds();
        int joinPhase = arena.getJoinPhaseSeconds();

        if (arena.isJoinPhase()) {
            int remaining = joinPhase - elapsed;
            if (remaining > 0 && remaining % 10 == 0) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    plugin.getMessageUtil().send(p, "arena-countdown-broadcast",
                            "%arena%", arena.getName(),
                            "%seconds%", String.valueOf(remaining));
                }
            }
        }

        // Join phase ended
        if (arena.isJoinPhase() && elapsed >= joinPhase) {
            // Minimum 2 players check
            if (arena.getPlayers().size() < 2) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    plugin.getMessageUtil().sendMultiline(p, "event-cancel-format",
                            "%arena%", arena.getName());
                }
                stopArena(arena);
                return;
            }

            // Start PvP
            arena.setJoinPhase(false);
            for (UUID uuid : arena.getPlayers()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    plugin.getMessageUtil().sendMultiline(p, "event-pvp-format");
                    plugin.getMessageUtil().title(p, "pvp-title", "pvp-subtitle");
                }
            }
        }

        // Effect phase
        List<String> effects = arena.getTimedEffects().get(elapsed);
        if (effects != null) {
            for (UUID uuid : arena.getAlive()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null) continue;
                for (String eff : effects) {
                    applyEffect(p, eff);
                }
            }
            for (String eff : effects) {
                String[] parts = eff.split(":");
                String effectName = parts.length > 0 ? parts[0] : "Unknown";
                int duration = parts.length > 2 ? Integer.parseInt(parts[2]) : 999999;
                for (UUID uuid : arena.getPlayers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        plugin.getMessageUtil().sendMultiline(p, "event-effect-format",
                                "%effect%", effectName,
                                "%duration%", String.valueOf(duration));
                    }
                }
            }
        }

        // Border shrink
        double[] borderPhase = arena.getBorderPhases().get(elapsed);
        if (borderPhase != null) {
            Location center = arena.getBorderCenter() != null ? arena.getBorderCenter() : arena.getSpawn();
            if (center != null && center.getWorld() != null) {
                WorldBorder border = center.getWorld().getWorldBorder();
                double newSize = borderPhase[0];
                long shrinkSeconds = (long) borderPhase[1];
                border.setSize(newSize, shrinkSeconds);

                for (UUID uuid : arena.getPlayers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        plugin.getMessageUtil().sendMultiline(p, "event-border-format",
                                "%size%", String.valueOf((int) newSize),
                                "%seconds%", String.valueOf(shrinkSeconds));
                    }
                }
            }
        }

        updateBossBar(arena, elapsed);

        // Winner
        if (!arena.isJoinPhase() && arena.getAlive().size() == 1 && arena.getPlayers().size() >= 2) {
            UUID winnerUuid = arena.getAlive().iterator().next();
            Player winner = Bukkit.getPlayer(winnerUuid);
            if (winner != null) {
                FFAPlayer fp = plugin.getPlayerManager().getPlayer(winner);
                fp.addWin();

                for (String cmd : arena.getWinnerRewardCommands()) {
                    String parsed = cmd.replace("%player%", winner.getName());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
                }

                for (Player p : Bukkit.getOnlinePlayers()) {
                    plugin.getMessageUtil().sendMultiline(p, "event-winner-format",
                            "%player%", winner.getName(),
                            "%arena%", arena.getName());
                }
                plugin.getMessageUtil().title(winner, "winner-title", "winner-subtitle");
            }
            stopArena(arena);
        }

        // No winner
        if (!arena.isJoinPhase() && arena.getAlive().isEmpty() && arena.getPlayers().size() >= 2) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                plugin.getMessageUtil().send(p, "arena-no-winner-broadcast",
                        "%arena%", arena.getName());
            }
            stopArena(arena);
        }
    }

    private void updateBossBar(Arena arena, int elapsed) {
        BossBar bar = arenaBossBars.get(arena.getName().toLowerCase());
        if (bar == null) return;
        if (!arena.isBossBarEnabled()) return;

        int alive = arena.getAlive().size();
        int total = arena.getPlayers().size();
        int remaining = Math.max(0, arena.getJoinPhaseSeconds() - elapsed);

        String title;
        if (arena.isJoinPhase()) {
            String raw = arena.getBossBarTitleJoin();
            if (raw == null) {
                raw = "<gradient:#00d4a0:#1a8c7a>%alive% players</gradient> <dark_gray>| <white>PvP in <yellow>%remaining%s";
            }
            raw = raw.replace("%alive%", String.valueOf(alive))
                    .replace("%players%", String.valueOf(total))
                    .replace("%remaining%", String.valueOf(remaining))
                    .replace("%arena%", arena.getName());
            title = ColorUtil.colorize(raw);
            bar.setProgress(Math.min(1.0, (double) remaining / Math.max(1, arena.getJoinPhaseSeconds())));
        } else {
            String raw = arena.getBossBarTitleFight();
            if (raw == null) {
                raw = "<gradient:#00d4a0:#1a8c7a>%alive% players alive</gradient>";
            }
            raw = raw.replace("%alive%", String.valueOf(alive))
                    .replace("%players%", String.valueOf(total))
                    .replace("%remaining%", "0")
                    .replace("%arena%", arena.getName());
            title = ColorUtil.colorize(raw);
            bar.setProgress(Math.min(1.0, alive / Math.max(1.0, total)));
        }
        bar.setTitle(title);

        // Color
        String colorStr = arena.getBossBarColor();
        if (colorStr != null) {
            try {
                bar.setColor(BarColor.valueOf(colorStr.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }

        // Style
        String styleStr = arena.getBossBarStyle();
        if (styleStr != null) {
            try {
                bar.setStyle(BarStyle.valueOf(styleStr.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }

        // Make sure all arena players are in the bar
        for (UUID uuid : arena.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && !bar.getPlayers().contains(p)) {
                bar.addPlayer(p);
            }
        }
    }

    private void applyEffect(Player p, String effectStr) {
        try {
            String[] parts = effectStr.split(":");
            PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
            if (type == null) return;
            int amplifier = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int duration = parts.length > 2 ? Integer.parseInt(parts[2]) : 999999;
            p.addPotionEffect(new PotionEffect(type, duration, amplifier, false, false));
        } catch (Exception ignored) {}
    }

    public void stopArena(Arena arena) {
        if (!arena.isRunning()) return;
        arena.setRunning(false);
        arena.setJoinPhase(true);

        if (arena.getTaskId() != -1) {
            Bukkit.getScheduler().cancelTask(arena.getTaskId());
            arena.setTaskId(-1);
        }

        if (arena.getSpawn() != null) {
            World world = arena.getSpawn().getWorld();
            if (world != null) {
                world.getWorldBorder().reset();
            }
        }

        BossBar bar = arenaBossBars.remove(arena.getName().toLowerCase());
        if (bar != null) {
            bar.removeAll();
        }

        for (UUID uuid : new HashSet<>(arena.getPlayers())) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            removePlayerFromArena(arena, p);
        }

        arena.getPlayers().clear();
        arena.getAlive().clear();

        for (Player p : Bukkit.getOnlinePlayers()) {
            plugin.getMessageUtil().send(p, "arena-stopped-broadcast",
                    "%arena%", arena.getName());
        }
    }

    public void stopAllArenas() {
        for (Arena a : new ArrayList<>(arenas.values())) {
            if (a.isRunning()) stopArena(a);
        }
    }

    // ==================================================
    //  JOIN / LEAVE
    // ==================================================

    public boolean joinArena(Arena arena, Player p) {
        if (!arena.isRunning()) return false;
        if (!arena.hasKit()) return false;
        if (arena.getPlayers().contains(p.getUniqueId())) return false;

        FFAPlayer fp = plugin.getPlayerManager().getPlayer(p);

        fp.saveInventory(
                p.getInventory().getContents().clone(),
                p.getInventory().getArmorContents().clone(),
                p.getInventory().getItemInOffHand(),
                p.getLocation().clone(),
                p.getHealth(),
                p.getFoodLevel()
        );
        fp.setSavedGameMode(p.getGameMode());

        p.getInventory().clear();
        if (arena.getKitContents() != null) p.getInventory().setContents(arena.getKitContents());
        if (arena.getKitArmor() != null) p.getInventory().setArmorContents(arena.getKitArmor());
        p.getInventory().setItemInOffHand(arena.getKitOffhand());

        String gm = plugin.getConfig().getString("arena-gamemode", "SURVIVAL");
        try {
            p.setGameMode(GameMode.valueOf(gm.toUpperCase()));
        } catch (IllegalArgumentException ignored) {
            p.setGameMode(GameMode.SURVIVAL);
        }

        p.teleport(arena.getSpawn());
        p.setHealth(20);
        p.setFoodLevel(20);
        p.setFireTicks(0);

        for (PotionEffect eff : p.getActivePotionEffects()) {
            p.removePotionEffect(eff.getType());
        }
        for (String eff : arena.getDefaultEffects()) {
            applyEffect(p, eff);
        }

        applyVanish(p);

        arena.getPlayers().add(p.getUniqueId());
        arena.getAlive().add(p.getUniqueId());
        fp.setCurrentArena(arena.getName());

        // Force bossbar add
        BossBar bar = arenaBossBars.get(arena.getName().toLowerCase());
        if (bar != null) {
            bar.addPlayer(p);
            updateBossBar(arena, arena.getElapsedSeconds());
        }

        // Force scoreboard update
        plugin.getScoreboardManager().updateScoreboard(p);
        return true;
    }

    public void removePlayerFromArena(Arena arena, Player p) {
        FFAPlayer fp = plugin.getPlayerManager().getPlayer(p);

        arena.getPlayers().remove(p.getUniqueId());
        arena.getAlive().remove(p.getUniqueId());

        for (PotionEffect eff : p.getActivePotionEffects()) {
            p.removePotionEffect(eff.getType());
        }

        if (fp.hasSavedInventory()) {
            p.getInventory().clear();
            if (fp.getSavedContents() != null) p.getInventory().setContents(fp.getSavedContents());
            if (fp.getSavedArmor() != null) p.getInventory().setArmorContents(fp.getSavedArmor());
            p.getInventory().setItemInOffHand(fp.getSavedOffhand());
            if (fp.getSavedHealth() > 0) p.setHealth(Math.min(fp.getSavedHealth(), 20));
            p.setFoodLevel(fp.getSavedFood());
            fp.clearSavedInventory();
        } else {
            p.getInventory().clear();
            p.getInventory().setArmorContents(null);
            p.getInventory().setItemInOffHand(null);
            p.setHealth(20);
            p.setFoodLevel(20);
        }

        if (fp.getSavedGameMode() != null) {
            p.setGameMode(fp.getSavedGameMode());
        }

        removeVanish(p);

        Location exit = arena.getExitLocation();
        if (exit != null) {
            p.teleport(exit);
        } else {
            p.teleport(p.getWorld().getSpawnLocation());
        }

        BossBar bar = arenaBossBars.get(arena.getName().toLowerCase());
        if (bar != null) bar.removePlayer(p);

        fp.setCurrentArena(null);
        plugin.getScoreboardManager().removeScoreboard(p);
    }

    public void playerDied(Arena arena, Player p) {
        arena.getAlive().remove(p.getUniqueId());
    }

    public Arena getArenaOfPlayer(Player p) {
        if (p == null) return null;
        for (Arena a : arenas.values()) {
            if (a.getPlayers().contains(p.getUniqueId())) return a;
        }
        return null;
    }

    // ===== Vanish helpers =====

    private void applyVanish(Player p) {
        if (plugin.getConfig().getBoolean("vanish.invisibility-effect", true)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999, 0, false, false));
        }
        if (plugin.getConfig().getBoolean("vanish.hide-from-others", true)) {
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.equals(p)) {
                    other.hidePlayer(plugin, p);
                }
            }
        }
    }

    private void removeVanish(Player p) {
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(p)) {
                other.showPlayer(plugin, p);
            }
        }
    }
}