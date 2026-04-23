package com.yourname.antispam.commands;

import com.yourname.antispam.AntiSpamPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command handler for /chat antispam commands with tab completion support.
 * Usage: /chat antispam <subcommand> [args]
 */
public class AntiSpamCommand implements CommandExecutor, TabCompleter {
    private final AntiSpamPlugin plugin;

    public AntiSpamCommand(AntiSpamPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Debug: log invocation details
        if (sender != null) {
            String who = (sender.getName() != null) ? sender.getName() : "console";
            plugin.getLogger().info("ChatCommand invoked by " + who + " label=" + label + " args=" + Arrays.toString(args));
        }
        
        // Check if this is /chat antispam command
        if (args.length < 1 || !args[0].equalsIgnoreCase("antispam")) {
            return false; // Let other handlers deal with it
        }
        
        // Check permission
        if (!sender.hasPermission("antispam.admin")) {
            sender.sendMessage("你没有权限使用此命令。");
            return true;
        }

        // Remove "antispam" from args
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        
        // /chat antispam delay <ms>
        if (subArgs.length >= 2 && subArgs[0].equalsIgnoreCase("delay")) {
            try {
                String raw = subArgs[1];
                String cleaned = raw.replaceAll("[^0-9]", "");
                if (cleaned.isEmpty()) {
                    throw new NumberFormatException("No digits found");
                }
                long ms = Long.parseLong(cleaned);
                plugin.setAntiSpamDelayMs(ms);
                plugin.saveConfigValue("anti-spam.delay-ms", ms);
                sender.sendMessage("AntiSpam 延迟已设置为 " + ms + " ms 并保存到配置文件");
            } catch (NumberFormatException e) {
                sender.sendMessage("用法错误: /chat antispam delay <毫秒数>");
            }
            return true;
        }
        
        // /chat antispam similarity <0.0-1.0>
        if (subArgs.length >= 2 && subArgs[0].equalsIgnoreCase("similarity")) {
            try {
                double threshold = Double.parseDouble(subArgs[1]);
                if (threshold < 0.0 || threshold > 1.0) {
                    sender.sendMessage("相似度阈值必须在 0.0 到 1.0 之间");
                    return true;
                }
                plugin.setSimilarityThreshold(threshold);
                plugin.saveConfigValue("anti-spam.similarity-threshold", threshold);
                sender.sendMessage("相似度阈值已设置为 " + String.format("%.2f", threshold * 100) + "% 并保存到配置文件");
            } catch (NumberFormatException e) {
                sender.sendMessage("用法错误: /chat antispam similarity <0.0-1.0>");
            }
            return true;
        }
        
        // /chat antispam toggle similarity
        if (subArgs.length >= 2 && subArgs[0].equalsIgnoreCase("toggle") && subArgs[1].equalsIgnoreCase("similarity")) {
            boolean newState = !plugin.isSimilarityCheckEnabled();
            plugin.setSimilarityCheckEnabled(newState);
            plugin.saveConfigValue("anti-spam.similarity-check", newState);
            sender.sendMessage("相似度检测已" + (newState ? "启用" : "禁用") + " 并保存到配置文件");
            return true;
        }
        
        // /chat antispam toggle profanity
        if (subArgs.length >= 2 && subArgs[0].equalsIgnoreCase("toggle") && subArgs[1].equalsIgnoreCase("profanity")) {
            boolean newState = !plugin.isProfanityFilterEnabled();
            plugin.setProfanityFilterEnabled(newState);
            plugin.saveConfigValue("anti-spam.profanity-filter", newState);
            sender.sendMessage("违规词过滤已" + (newState ? "启用" : "禁用") + " 并保存到配置文件");
            return true;
        }
        
        // /chat antispam status
        if (subArgs.length >= 1 && subArgs[0].equalsIgnoreCase("status")) {
            sender.sendMessage("AntiSpam Status:");
            sender.sendMessage("- delayMs: " + plugin.getAntiSpamDelayMs() + " ms");
            sender.sendMessage("- similarityCheck: " + (plugin.isSimilarityCheckEnabled() ? "启用" : "禁用"));
            sender.sendMessage("- similarityThreshold: " + String.format("%.2f", plugin.getSimilarityThreshold() * 100) + "%");
            sender.sendMessage("- minLengthForCheck: " + plugin.getMinLengthForCheck());
            sender.sendMessage("- profanityFilter: " + (plugin.isProfanityFilterEnabled() ? "启用" : "禁用"));
            sender.sendMessage("- blockedWords: " + plugin.getProfanityFilter().getBlockedWordCount() + " 个");
            return true;
        }
        
        // /chat antispam reload
        if (subArgs.length >= 1 && subArgs[0].equalsIgnoreCase("reload")) {
            plugin.reloadFromConfig();
            sender.sendMessage("AntiSpam config reloaded");
            return true;
        }

        // Show usage
        sender.sendMessage("AntiSpam 命令:");
        sender.sendMessage("/chat antispam delay <毫秒数> - 设置消息间隔延迟");
        sender.sendMessage("/chat antispam similarity <0.0-1.0> - 设置相似度阈值");
        sender.sendMessage("/chat antispam toggle similarity - 开关相似度检测");
        sender.sendMessage("/chat antispam toggle profanity - 开关违规词过滤");
        sender.sendMessage("/chat antispam status - 查看当前配置");
        sender.sendMessage("/chat antispam reload - 重新加载配置");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // /chat <tab> -> suggest "antispam"
        if (args.length == 1) {
            if ("antispam".startsWith(args[0].toLowerCase())) {
                completions.add("antispam");
            }
            return completions;
        }
        
        // Only handle if first arg is "antispam"
        if (!args[0].equalsIgnoreCase("antispam")) {
            return completions;
        }
        
        // /chat antispam <tab> -> suggest subcommands
        if (args.length == 2) {
            String[] subcommands = {"delay", "similarity", "toggle", "status", "reload"};
            for (String sub : subcommands) {
                if (sub.startsWith(args[1].toLowerCase())) {
                    completions.add(sub);
                }
            }
            return completions;
        }
        
        // /chat antispam delay <tab> -> suggest common values
        if (args.length == 3 && args[1].equalsIgnoreCase("delay")) {
            String[] suggestions = {"20", "50", "100", "500", "1000"};
            for (String s : suggestions) {
                if (s.startsWith(args[2])) {
                    completions.add(s);
                }
            }
            return completions;
        }
        
        // /chat antispam similarity <tab> -> suggest common values
        if (args.length == 3 && args[1].equalsIgnoreCase("similarity")) {
            String[] suggestions = {"0.5", "0.6", "0.7", "0.8", "0.9", "1.0"};
            for (String s : suggestions) {
                if (s.startsWith(args[2])) {
                    completions.add(s);
                }
            }
            return completions;
        }
        
        // /chat antispam toggle <tab> -> suggest "similarity"
        if (args.length == 3 && args[1].equalsIgnoreCase("toggle")) {
            String[] options = {"similarity", "profanity"};
            for (String opt : options) {
                if (opt.startsWith(args[2].toLowerCase())) {
                    completions.add(opt);
                }
            }
            return completions;
        }
        
        return completions;
    }
}
