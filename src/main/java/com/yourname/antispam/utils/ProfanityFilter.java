package com.yourname.antispam.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for filtering profanity and blocked words from messages.
 */
public class ProfanityFilter {
    private final Set<String> blockedWords;
    
    public ProfanityFilter(List<String> blockedWordsList) {
        this.blockedWords = new HashSet<>();
        if (blockedWordsList != null) {
            for (String word : blockedWordsList) {
                if (word != null && !word.trim().isEmpty()) {
                    // Store in lowercase for case-insensitive matching
                    this.blockedWords.add(word.toLowerCase().trim());
                }
            }
        }
    }
    
    /**
     * Check if a message contains any blocked words.
     * 
     * @param message The message to check
     * @return true if the message contains blocked words
     */
    public boolean containsProfanity(String message) {
        if (message == null || message.isEmpty()) {
            return false;
        }
        
        String lowerMessage = message.toLowerCase();
        
        // Check each blocked word
        for (String blockedWord : blockedWords) {
            if (lowerMessage.contains(blockedWord)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Find the first blocked word in a message.
     * 
     * @param message The message to check
     * @return The first blocked word found, or null if none found
     */
    public String findBlockedWord(String message) {
        if (message == null || message.isEmpty()) {
            return null;
        }
        
        String lowerMessage = message.toLowerCase();
        
        for (String blockedWord : blockedWords) {
            if (lowerMessage.contains(blockedWord)) {
                return blockedWord;
            }
        }
        
        return null;
    }
    
    /**
     * Get the number of blocked words in the filter.
     * 
     * @return The count of blocked words
     */
    public int getBlockedWordCount() {
        return blockedWords.size();
    }
    
    /**
     * Update the blocked words list.
     * 
     * @param blockedWordsList New list of blocked words
     */
    public void updateBlockedWords(List<String> blockedWordsList) {
        blockedWords.clear();
        if (blockedWordsList != null) {
            for (String word : blockedWordsList) {
                if (word != null && !word.trim().isEmpty()) {
                    blockedWords.add(word.toLowerCase().trim());
                }
            }
        }
    }
}
