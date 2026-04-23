package com.yourname.antispam.managers;

import com.yourname.antispam.AntiSpamPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for handling player mutes.
 */
public class MuteManager {
    private final AntiSpamPlugin plugin;
    private final Map<UUID, Long> mutedPlayers; // UUID -> unmute timestamp
    private final File muteFile;
    private FileConfiguration muteConfig;
    
    public MuteManager(AntiSpamPlugin plugin) {
        this.plugin = plugin;
        this.mutedPlayers = new ConcurrentHashMap<>();
        this.muteFile = new File(plugin.getDataFolder(), "mutes.yml");
        loadMutes();
    }
    
    /**
     * Mute a player for a specified duration.
     * 
     * @param playerUUID Player's UUID
     * @param playerName Player's name (for logging)
     * @param durationSeconds Duration in seconds
     */
    public void mutePlayer(UUID playerUUID, String playerName, long durationSeconds) {
        long unmuteTime = System.currentTimeMillis() + (durationSeconds * 1000);
        mutedPlayers.put(playerUUID, unmuteTime);
        saveMutes();
        plugin.getLogger().info("Muted player " + playerName + " for " + durationSeconds + " seconds");
    }
    
    /**
     * Unmute a player.
     * 
     * @param playerUUID Player's UUID
     * @param playerName Player's name (for logging)
     */
    public void unmutePlayer(UUID playerUUID, String playerName) {
        mutedPlayers.remove(playerUUID);
        saveMutes();
        plugin.getLogger().info("Unmuted player " + playerName);
    }
    
    /**
     * Check if a player is muted.
     * 
     * @param playerUUID Player's UUID
     * @return true if the player is currently muted
     */
    public boolean isMuted(UUID playerUUID) {
        Long unmuteTime = mutedPlayers.get(playerUUID);
        if (unmuteTime == null) {
            return false;
        }
        
        // Check if mute has expired
        if (System.currentTimeMillis() >= unmuteTime) {
            mutedPlayers.remove(playerUUID);
            saveMutes();
            return false;
        }
        
        return true;
    }
    
    /**
     * Get the remaining mute time for a player.
     * 
     * @param playerUUID Player's UUID
     * @return Remaining seconds, or 0 if not muted
     */
    public long getRemainingMuteTime(UUID playerUUID) {
        Long unmuteTime = mutedPlayers.get(playerUUID);
        if (unmuteTime == null) {
            return 0;
        }
        
        long remaining = (unmuteTime - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }
    
    /**
     * Get a formatted time string.
     * 
     * @param seconds Total seconds
     * @return Formatted string like "1h 30m 45s"
     */
    public String formatTime(long seconds) {
        if (seconds <= 0) {
            return "0秒";
        }
        
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("小时 ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("分钟 ");
        }
        if (secs > 0 || sb.length() == 0) {
            sb.append(secs).append("秒");
        }
        
        return sb.toString().trim();
    }
    
    /**
     * Load mutes from file.
     */
    private void loadMutes() {
        if (!muteFile.exists()) {
            try {
                muteFile.getParentFile().mkdirs();
                muteFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create mutes.yml: " + e.getMessage());
                return;
            }
        }
        
        muteConfig = YamlConfiguration.loadConfiguration(muteFile);
        
        // Load mutes from config
        if (muteConfig.contains("mutes")) {
            for (String uuidStr : muteConfig.getConfigurationSection("mutes").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    long unmuteTime = muteConfig.getLong("mutes." + uuidStr);
                    
                    // Only load if not expired
                    if (System.currentTimeMillis() < unmuteTime) {
                        mutedPlayers.put(uuid, unmuteTime);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in mutes.yml: " + uuidStr);
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + mutedPlayers.size() + " active mutes");
    }
    
    /**
     * Save mutes to file.
     */
    private void saveMutes() {
        muteConfig = new YamlConfiguration();
        
        // Save all active mutes
        for (Map.Entry<UUID, Long> entry : mutedPlayers.entrySet()) {
            muteConfig.set("mutes." + entry.getKey().toString(), entry.getValue());
        }
        
        try {
            muteConfig.save(muteFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save mutes.yml: " + e.getMessage());
        }
    }
    
    /**
     * Get the number of currently muted players.
     * 
     * @return Number of muted players
     */
    public int getMutedPlayerCount() {
        // Clean up expired mutes
        mutedPlayers.entrySet().removeIf(entry -> 
            System.currentTimeMillis() >= entry.getValue()
        );
        return mutedPlayers.size();
    }
}
