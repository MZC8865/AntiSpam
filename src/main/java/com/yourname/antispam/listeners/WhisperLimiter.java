package com.yourname.antispam.listeners;

import com.yourname.antispam.AntiSpamPlugin;
import com.yourname.antispam.utils.StringSimilarity;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener for whisper/private message commands.
 * Monitors commands like /w, /msg, /tell, /whisper for anti-spam checks.
 */
public class WhisperLimiter implements Listener {
    private final AntiSpamPlugin plugin;
    
    // Track last message timestamp per user
    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    
    // Track last message content per user
    private final Map<UUID, String> lastMessageContent = new ConcurrentHashMap<>();
    
    public WhisperLimiter(AntiSpamPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        // Check if whisper check is enabled
        if (!plugin.isWhisperCheckEnabled()) {
            return;
        }
        
        String message = event.getMessage().toLowerCase();
        
        // Check if this is a whisper command
        if (!isWhisperCommand(message)) {
            return;
        }
        
        // Extract the message content from the command
        String whisperContent = extractWhisperContent(event.getMessage());
        if (whisperContent == null || whisperContent.isEmpty()) {
            return;
        }
        
        UUID id = event.getPlayer().getUniqueId();
        long now = System.currentTimeMillis();
        
        // Mute check: highest priority
        if (plugin.getMuteManager().isMuted(id)) {
            event.setCancelled(true);
            long remaining = plugin.getMuteManager().getRemainingMuteTime(id);
            String timeStr = plugin.getMuteManager().formatTime(remaining);
            event.getPlayer().sendMessage(org.bukkit.ChatColor.RED + "你已被禁言，剩余时间：" + timeStr);
            return;
        }
        
        // Profanity filter: check for blocked words first (highest priority)
        if (plugin.isProfanityFilterEnabled()) {
            if (plugin.getProfanityFilter().containsProfanity(whisperContent)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "有违规文字");
                String blockedWord = plugin.getProfanityFilter().findBlockedWord(whisperContent);
                plugin.getLogger().info("AntiSpam: Blocked whisper from " + event.getPlayer().getName() + 
                    " (profanity detected: " + blockedWord + ")");
                return;
            }
        }
        
        // Fast-send detection
        long currentDelay = plugin.getAntiSpamDelayMs();
        Long last = lastMessageTime.get(id);
        if (last != null && (now - last) < currentDelay) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "请勿刷屏");
            plugin.getLogger().info("AntiSpam: Blocked whisper from " + event.getPlayer().getName() + 
                " (sent too fast, gap=" + (now - last) + "ms)");
            return;
        }
        
        // Similarity detection
        if (plugin.isSimilarityCheckEnabled()) {
            String lastMsg = lastMessageContent.get(id);
            int minLength = plugin.getMinLengthForCheck();
            
            if (lastMsg != null && whisperContent.length() >= minLength && lastMsg.length() >= minLength) {
                double similarity = StringSimilarity.similarity(whisperContent, lastMsg);
                double threshold = plugin.getSimilarityThreshold();
                
                if (similarity >= threshold) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "消息过于相似");
                    plugin.getLogger().info("AntiSpam: Blocked whisper from " + event.getPlayer().getName() + 
                        " (similar message, similarity=" + String.format("%.2f", similarity * 100) + "%)");
                    return;
                }
            }
        }
        
        // Update last message time and content for successful messages
        lastMessageTime.put(id, now);
        lastMessageContent.put(id, whisperContent);
    }
    
    /**
     * Check if the command is a whisper command.
     */
    private boolean isWhisperCommand(String command) {
        // Remove leading slash
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        // Check common whisper command aliases
        String[] whisperCommands = {"w ", "msg ", "tell ", "whisper ", "m ", "t ", "pm "};
        for (String cmd : whisperCommands) {
            if (command.startsWith(cmd)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Extract the message content from a whisper command.
     * Format: /w <player> <message>
     */
    private String extractWhisperContent(String command) {
        // Remove leading slash
        if (command.startsWith("/")) {
            command = command.substring(1);
        }
        
        // Split by spaces
        String[] parts = command.split(" ", 3);
        
        // Need at least: command, player, message
        if (parts.length < 3) {
            return null;
        }
        
        // Return the message part
        return parts[2];
    }
}
