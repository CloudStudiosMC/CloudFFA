package com.cloudstudios.cloudffa.objects;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class FFAPlayer {

    private final UUID uuid;

    // Stats
    private int kills;
    private int deaths;
    private int wins;
    private int killstreak;
    private int bestKillstreak;

    // Arena
    private String currentArena;

    // Saved inventory
    private ItemStack[] savedContents;
    private ItemStack[] savedArmor;
    private ItemStack savedOffhand;
    private Location savedLocation;
    private double savedHealth;
    private int savedFood;
    private boolean hasSavedInventory;

    // Saved state
    private GameMode savedGameMode;

    public FFAPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() { return uuid; }

    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public void addKill() { this.kills++; }

    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public void addDeath() { this.deaths++; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public void addWin() { this.wins++; }

    public int getKillstreak() { return killstreak; }
    public void setKillstreak(int killstreak) { this.killstreak = killstreak; }
    public void incrementKillstreak() { this.killstreak++; }
    public void resetKillstreak() { this.killstreak = 0; }

    public int getBestKillstreak() { return bestKillstreak; }
    public void setBestKillstreak(int v) { this.bestKillstreak = v; }

    public String getCurrentArena() { return currentArena; }
    public void setCurrentArena(String a) { this.currentArena = a; }

    public double getKDR() {
        return deaths == 0 ? kills : (double) kills / deaths;
    }

    // ===== Inventory saving =====

    public void saveInventory(ItemStack[] contents, ItemStack[] armor, ItemStack offhand,
                              Location loc, double health, int food) {
        this.savedContents = contents;
        this.savedArmor = armor;
        this.savedOffhand = offhand;
        this.savedLocation = loc;
        this.savedHealth = health;
        this.savedFood = food;
        this.hasSavedInventory = true;
    }

    public ItemStack[] getSavedContents() { return savedContents; }
    public ItemStack[] getSavedArmor() { return savedArmor; }
    public ItemStack getSavedOffhand() { return savedOffhand; }
    public Location getSavedLocation() { return savedLocation; }
    public double getSavedHealth() { return savedHealth; }
    public int getSavedFood() { return savedFood; }
    public boolean hasSavedInventory() { return hasSavedInventory; }

    public GameMode getSavedGameMode() { return savedGameMode; }
    public void setSavedGameMode(GameMode gm) { this.savedGameMode = gm; }

    public void clearSavedInventory() {
        this.savedContents = null;
        this.savedArmor = null;
        this.savedOffhand = null;
        this.savedLocation = null;
        this.savedGameMode = null;
        this.hasSavedInventory = false;
    }
}