package com.saberw.model;

import java.util.List;

public class CompressionResult {
    private final String compressedText;
    private final List<DictionaryEntry> dictionary;
    private final Statistics stats;
    
    public CompressionResult(String compressedText, 
                           List<DictionaryEntry> dictionary,
                           Statistics stats) {
        this.compressedText = compressedText;
        this.dictionary = dictionary;
        this.stats = stats;
    }
    
    public String getCompressedText() { return compressedText; }
    public List<DictionaryEntry> getDictionary() { return dictionary; }
    public Statistics getStats() { return stats; }
    
    public static class Statistics {
        private final int originalLength;
        private final int compressedLength;
        private final int dictionaryEntries;
        private final double compressionRatio;
        
        public Statistics(int originalLength, int compressedLength, 
                         int dictionaryEntries) {
            this.originalLength = originalLength;
            this.compressedLength = compressedLength;
            this.dictionaryEntries = dictionaryEntries;
            this.compressionRatio = originalLength > 0 ? 
                (double) compressedLength / originalLength : 0.0;
        }
        
        public int getOriginalLength() { return originalLength; }
        public int getCompressedLength() { return compressedLength; }
        public int getDictionaryEntries() { return dictionaryEntries; }
        public double getCompressionRatio() { return compressionRatio; }
        
        @Override
        public String toString() {
            return String.format(
                "Original: %d chars, Compressed: %d chars, " +
                "Dictionary: %d entries, Ratio: %.2f",
                originalLength, compressedLength, 
                dictionaryEntries, compressionRatio
            );
        }
    }
}