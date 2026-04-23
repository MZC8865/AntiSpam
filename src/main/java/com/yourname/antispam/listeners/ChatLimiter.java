package com.yourname.antispam.listeners;

import com.yourname.antispam.AntiSpamPlugin;
import com.yourname.antispam.utils.StringSimilarity;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very lightweight anti-spam: limits how often a player can chat.
 * If a player sends a message within the configured delay (default 20ms) after their last message,
 * the new message is blocked.
 * Also checks for message similarity to prevent repetitive spam.
 */
public class ChatLimiter implements Listener {
    private final AntiSpamPlugin plugin;

    // Track last message timestamp per user for fast-send detection
    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    
    // Track last message content per user for similarity detection
    private final Map<UUID, String> lastMessageContent = new ConcurrentHashMap<>();

    public ChatLimiter(AntiSpamPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        // If another plugin already canceled it, skip
        if (event.isCancelled()) return;

        UUID id = event.getPlayer().getUniqueId();
        long now = System.currentTimeMillis();
        String message = event.getMessage();

        // Mute check: highest priority
        if (plugin.getMuteManager().isMuted(id)) {
            event.setCancelled(true);
            long remaining = plugin.getMuteManager().getRemainingMuteTime(id);
            String timeStr = plugin.getMuteManager().formatTime(remaining);
            event.getPlayer().sendMessage(ChatColor.RED + "你已被禁言，剩余时间：" + timeStr);
            return;
        }

        // Profanity filter: check for blocked words first (highest priority)
        if (plugin.isProfanityFilterEnabled()) {
            if (plugin.getProfanityFilter().containsProfanity(message)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "有违规文字");
                String blockedWord = plugin.getProfanityFilter().findBlockedWord(message);
                plugin.getLogger().info("AntiSpam: Blocked chat from " + event.getPlayer().getName() + 
                    " (profanity detected: " + blockedWord + ")");
                return;
            }
        }

        // Fast-send detection: block if messages are sent too quickly
        long currentDelay = plugin.getAntiSpamDelayMs();
        Long last = lastMessageTime.get(id);
        if (last != null && (now - last) < currentDelay) {
            // Block the message immediately
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "请勿刷屏");
            plugin.getLogger().info("AntiSpam: Blocked chat from " + event.getPlayer().getName() + " (sent too fast, gap=" + (now - last) + "ms)");
            // Do NOT update lastMessageTime so the cooldown continues from the last successful message
            return;
        }
        
        // Similarity detection: check if message is too similar to the last one
        if (plugin.isSimilarityCheckEnabled()) {
            String lastMsg = lastMessageContent.get(id);
            int minLength = plugin.getMinLengthForCheck();
            
            // Only check similarity if both messages meet minimum length requirement
            if (lastMsg != null && message.length() >= minLength && lastMsg.length() >= minLength) {
                double similarity = StringSimilarity.similarity(message, lastMsg);
                double threshold = plugin.getSimilarityThreshold();
                
                if (similarity >= threshold) {
                    // Block similar message
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatColor.RED + "消息过于相似");
                    plugin.getLogger().info("AntiSpam: Blocked chat from " + event.getPlayer().getName() + 
                        " (similar message, similarity=" + String.format("%.2f", similarity * 100) + "%)");
                    return;
                }
                
                // Optional debug: log similarity when enabled
                if (Boolean.getBoolean("antispam.debug")) {
                    plugin.getLogger().info("AntiSpam debug: user=" + event.getPlayer().getName() + 
                        " similarity=" + String.format("%.2f", similarity * 100) + "% threshold=" + 
                        String.format("%.2f", threshold * 100) + "%");
                }
            }
        }
        
        // Optional debug: log gap between messages when enabled via -Dantispam.debug
        if (Boolean.getBoolean("antispam.debug") && last != null) {
            long gap = now - last;
            plugin.getLogger().info("AntiSpam debug: user=" + event.getPlayer().getName() + 
                " gap=" + gap + " ms delay=" + currentDelay + " ms");
        }
        
        // Update last message time and content only for successful messages
        lastMessageTime.put(id, now);
        lastMessageContent.put(id, message);
    }
}
