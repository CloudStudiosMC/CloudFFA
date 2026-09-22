package com.cloudstudios.cloudffa.objects;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

public class Arena {

    private final String name;

    private Location spawn;
    private Location exitLocation;
    private Location borderCenter;

    // Kit
    private ItemStack[] kitContents;
    private ItemStack[] kitArmor;
    private ItemStack kitOffhand;

    // State
    private boolean running;
    private boolean joinPhase = true;
    private int taskId = -1;
    private final Set<UUID> players = new HashSet<>();
    private final Set<UUID> alive = new HashSet<>();

    // Join phase
    private int joinPhaseSeconds = 120;

    // Effects
    private List<String> defaultEffects = new ArrayList<>();
    private final Map<Integer, List<String>> timedEffects = new TreeMap<>();

    // Border
    private double borderDefaultSize = 150;
    private double borderFinalSize = 20;
    private final Map<Integer, double[]> borderPhases = new TreeMap<>();

    // Winner rewards
    private List<String> winnerRewardCommands = new ArrayList<>();

    // Scoreboard
    private String scoreboardTitle;
    private List<String> scoreboardLines;

    // BossBar
    private boolean bossBarEnabled = true;
    private String bossBarTitleJoin;
    private String bossBarTitleFight;
    private String bossBarColor = "GREEN";
    private String bossBarStyle = "SOLID";

    // Stats
    private long startedAt;

    public Arena(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public Location getSpawn() { return spawn; }
    public void setSpawn(Location spawn) { this.spawn = spawn; }

    public Location getExitLocation() { return exitLocation; }
    public void setExitLocation(Location exitLocation) { this.exitLocation = exitLocation; }

    public Location getBorderCenter() { return borderCenter; }
    public void setBorderCenter(Location borderCenter) { this.borderCenter = borderCenter; }

    public ItemStack[] getKitContents() { return kitContents; }
    public void setKitContents(ItemStack[] kitContents) { this.kitContents = kitContents; }

    public ItemStack[] getKitArmor() { return kitArmor; }
    public void setKitArmor(ItemStack[] kitArmor) { this.kitArmor = kitArmor; }

    public ItemStack getKitOffhand() { return kitOffhand; }
    public void setKitOffhand(ItemStack kitOffhand) { this.kitOffhand = kitOffhand; }

    public boolean hasKit() {
        return kitContents != null || kitArmor != null || kitOffhand != null;
    }

    public boolean isRunning() { return running; }
    public void setRunning(boolean running) {
        this.running = running;
        if (running) this.startedAt = System.currentTimeMillis();
    }

    public boolean isJoinPhase() { return joinPhase; }
    public void setJoinPhase(boolean joinPhase) { this.joinPhase = joinPhase; }

    public int getJoinPhaseSeconds() { return joinPhaseSeconds; }
    public void setJoinPhaseSeconds(int s) { this.joinPhaseSeconds = s; }

    public int getTaskId() { return taskId; }
    public void setTaskId(int taskId) { this.taskId = taskId; }

    public long getStartedAt() { return startedAt; }
    public int getElapsedSeconds() {
        if (!running) return 0;
        return (int) ((System.currentTimeMillis() - startedAt) / 1000L);
    }

    public Set<UUID> getPlayers() { return players; }
    public Set<UUID> getAlive() { return alive; }

    public List<String> getDefaultEffects() { return defaultEffects; }
    public void setDefaultEffects(List<String> e) { this.defaultEffects = e; }

    public Map<Integer, List<String>> getTimedEffects() { return timedEffects; }

    public double getBorderDefaultSize() { return borderDefaultSize; }
    public void setBorderDefaultSize(double s) { this.borderDefaultSize = s; }

    public double getBorderFinalSize() { return borderFinalSize; }
    public void setBorderFinalSize(double s) { this.borderFinalSize = s; }

    public Map<Integer, double[]> getBorderPhases() { return borderPhases; }

    public List<String> getWinnerRewardCommands() { return winnerRewardCommands; }
    public void setWinnerRewardCommands(List<String> c) { this.winnerRewardCommands = c; }

    // ===== Scoreboard =====
    public String getScoreboardTitle() { return scoreboardTitle; }
    public void setScoreboardTitle(String t) { this.scoreboardTitle = t; }

    public List<String> getScoreboardLines() { return scoreboardLines; }
    public void setScoreboardLines(List<String> l) { this.scoreboardLines = l; }

    // ===== BossBar =====
    public boolean isBossBarEnabled() { return bossBarEnabled; }
    public void setBossBarEnabled(boolean b) { this.bossBarEnabled = b; }

    public String getBossBarTitleJoin() { return bossBarTitleJoin; }
    public void setBossBarTitleJoin(String t) { this.bossBarTitleJoin = t; }

    public String getBossBarTitleFight() { return bossBarTitleFight; }
    public void setBossBarTitleFight(String t) { this.bossBarTitleFight = t; }

    public String getBossBarColor() { return bossBarColor; }
    public void setBossBarColor(String c) { this.bossBarColor = c; }

    public String getBossBarStyle() { return bossBarStyle; }
    public void setBossBarStyle(String s) { this.bossBarStyle = s; }
}