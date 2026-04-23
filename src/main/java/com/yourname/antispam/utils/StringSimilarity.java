package com.yourname.antispam.utils;

/**
 * Utility class for calculating string similarity using Levenshtein distance.
 */
public class StringSimilarity {
    
    /**
     * Calculate the Levenshtein distance between two strings.
     * This represents the minimum number of single-character edits needed to change one string into the other.
     * 
     * @param s1 First string
     * @param s2 Second string
     * @return The Levenshtein distance
     */
    public static int levenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return -1;
        }
        
        int len1 = s1.length();
        int len2 = s2.length();
        
        // Quick checks
        if (len1 == 0) return len2;
        if (len2 == 0) return len1;
        if (s1.equals(s2)) return 0;
        
        // Create distance matrix
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        // Initialize first column and row
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        
        // Calculate distances
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        
        return dp[len1][len2];
    }
    
    /**
     * Calculate similarity ratio between two strings.
     * Returns a value between 0.0 (completely different) and 1.0 (identical).
     * 
     * @param s1 First string
     * @param s2 Second string
     * @return Similarity ratio (0.0 to 1.0)
     */
    public static double similarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        
        if (s1.equals(s2)) {
            return 1.0;
        }
        
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) {
            return 1.0;
        }
        
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }
    
    /**
     * Check if two strings are similar based on a threshold.
     * 
     * @param s1 First string
     * @param s2 Second string
     * @param threshold Similarity threshold (0.0 to 1.0)
     * @return true if similarity >= threshold
     */
    public static boolean isSimilar(String s1, String s2, double threshold) {
        return similarity(s1, s2) >= threshold;
    }
}
