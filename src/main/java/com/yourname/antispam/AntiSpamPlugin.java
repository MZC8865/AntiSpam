package com.yourname.antispam;

import com.yourname.antispam.commands.AntiSpamCommand;
import com.yourname.antispam.listeners.ChatLimiter;
import com.yourname.antispam.managers.MuteManager;
import com.yourname.antispam.utils.ProfanityFilter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Basic Paper/Spigot plugin scaffold for anti-spam.
 */
public class AntiSpamPlugin extends JavaPlugin {
    // Configurable anti-spam delay in milliseconds (default 20ms)
    private volatile long antiSpamDelayMs = 20L;
    // Similarity check configuration
    private volatile boolean similarityCheckEnabled = true;
    private volatile double similarityThreshold = 0.8;
    private volatile int minLengthForCheck = 3;
    // Profanity filter configuration
    private volatile boolean profanityFilterEnabled = true;
    private ProfanityFilter profanityFilter;
    // Whisper check configuration
    private volatile boolean whisperCheckEnabled = true;
    // Mute manager
    private MuteManager muteManager;
    
    @Override
    public void onEnable() {
        getLogger().info("AntiSpam enabled");
        // Diagnostic: log a snippet of plugin.yml from the JAR to verify packaging
        try (InputStream in = getResource("plugin.yml")) {
            if (in != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                String line;
                int maxLines = 5;
                int count = 0;
                while ((line = br.readLine()) != null && count < maxLines) {
                    getLogger().info("[plugin.yml preview] " + line);
                    count++;
                }
            } else {
                getLogger().warning("plugin.yml not found inside JAR.");
            }
        } catch (Exception e) {
            getLogger().warning("Failed to read plugin.yml from JAR: " + e.getMessage());
        }
        // Register listeners
        getServer().getPluginManager().registerEvents(new ChatLimiter(this), this);
        getServer().getPluginManager().registerEvents(new com.yourname.antispam.listeners.WhisperLimiter(this), this);
        getLogger().info("AntiSpam: Registered chat and whisper listeners");
        
        // Load initial config
        saveDefaultConfig();
        loadFromConfig();
        
        // Initialize mute manager
        this.muteManager = new MuteManager(this);
        getLogger().info("AntiSpam: Mute manager initialized");
        
        // Register command directly with CommandMap for Paper plugins
        try {
            AntiSpamCommand cmdExecutor = new AntiSpamCommand(this);
            Command cmd = new Command("chat") {
                @Override
                public boolean execute(CommandSender sender, String label, String[] args) {
                    return cmdExecutor.onCommand(sender, this, label, args);
                }
                
                @Override
                public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
                    return cmdExecutor.onTabComplete(sender, this, alias, args);
                }
            };
            cmd.setDescription("Chat management commands");
            cmd.setUsage("/chat antispam <subcommand>");
            cmd.setPermission("antispam.admin");
            getServer().getCommandMap().register("chat", cmd);
            getLogger().info("AntiSpam: command '/chat' registered with tab completion");
        } catch (Exception e) {
            getLogger().warning("Failed to register command: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("AntiSpam disabled");
    }

    // Getter/setter for configurable anti-spam delay
    public synchronized long getAntiSpamDelayMs() {
        return antiSpamDelayMs;
    }

    public synchronized void setAntiSpamDelayMs(long ms) {
        if (ms < 0) ms = 0;
        this.antiSpamDelayMs = ms;
        getLogger().info("AntiSpam delay set to " + ms + " ms");
    }

    // Getters/setters for similarity check configuration
    public synchronized boolean isSimilarityCheckEnabled() {
        return similarityCheckEnabled;
    }

    public synchronized void setSimilarityCheckEnabled(boolean enabled) {
        this.similarityCheckEnabled = enabled;
        getLogger().info("Similarity check " + (enabled ? "enabled" : "disabled"));
    }

    public synchronized double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public synchronized void setSimilarityThreshold(double threshold) {
        if (threshold < 0.0) threshold = 0.0;
        if (threshold > 1.0) threshold = 1.0;
        this.similarityThreshold = threshold;
        getLogger().info("Similarity threshold set to " + threshold);
    }

    public synchronized int getMinLengthForCheck() {
        return minLengthForCheck;
    }

    public synchronized void setMinLengthForCheck(int length) {
        if (length < 0) length = 0;
        this.minLengthForCheck = length;
        getLogger().info("Min length for similarity check set to " + length);
    }

    // Getters/setters for profanity filter configuration
    public synchronized boolean isProfanityFilterEnabled() {
        return profanityFilterEnabled;
    }

    public synchronized void setProfanityFilterEnabled(boolean enabled) {
        this.profanityFilterEnabled = enabled;
        getLogger().info("Profanity filter " + (enabled ? "enabled" : "disabled"));
    }

    public synchronized ProfanityFilter getProfanityFilter() {
        return profanityFilter;
    }

    // Getters/setters for whisper check configuration
    public synchronized boolean isWhisperCheckEnabled() {
        return whisperCheckEnabled;
    }

    public synchronized void setWhisperCheckEnabled(boolean enabled) {
        this.whisperCheckEnabled = enabled;
        getLogger().info("Whisper check " + (enabled ? "enabled" : "disabled"));
    }

    // Getter for mute manager
    public MuteManager getMuteManager() {
        return muteManager;
    }

    // Reload config from disk and apply values
    public synchronized void reloadFromConfig() {
        reloadConfig();
        loadFromConfig();
        getLogger().info("AntiSpam config reloaded");
    }

    // Load config values into fields
    public synchronized void loadFromConfig() {
        this.antiSpamDelayMs = getConfig().getLong("anti-spam.delay-ms", 20L);
        this.similarityCheckEnabled = getConfig().getBoolean("anti-spam.similarity-check", true);
        this.similarityThreshold = getConfig().getDouble("anti-spam.similarity-threshold", 0.8);
        this.minLengthForCheck = getConfig().getInt("anti-spam.min-length-for-check", 3);
        
        // Load profanity filter settings
        this.profanityFilterEnabled = getConfig().getBoolean("anti-spam.profanity-filter", true);
        List<String> blockedWords = getConfig().getStringList("anti-spam.blocked-words");
        this.profanityFilter = new ProfanityFilter(blockedWords);
        
        // Load whisper check setting
        this.whisperCheckEnabled = getConfig().getBoolean("anti-spam.whisper-check", true);
        
        getLogger().info("AntiSpam config loaded: delayMs=" + antiSpamDelayMs + 
            ", similarityCheck=" + similarityCheckEnabled + 
            ", threshold=" + similarityThreshold + 
            ", minLength=" + minLengthForCheck +
            ", profanityFilter=" + profanityFilterEnabled +
            ", blockedWords=" + profanityFilter.getBlockedWordCount() +
            ", whisperCheck=" + whisperCheckEnabled);
    }
    
    // Save a config value to disk
    public synchronized void saveConfigValue(String path, Object value) {
        getConfig().set(path, value);
        saveConfig();
        getLogger().info("Saved config: " + path + " = " + value);
    }
}
