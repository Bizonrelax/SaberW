package com.saberw.model;

public class DictionaryEntry {
    private final String word;
    private final String code;
    private final int frequency;
    
    public DictionaryEntry(String word, String code, int frequency) {
        this.word = word;
        this.code = code;
        this.frequency = frequency;
    }
    
    public String getWord() { return word; }
    public String getCode() { return code; }
    public int getFrequency() { return frequency; }
    
    @Override
    public String toString() {
        return String.format("%s=%s (freq: %d)", code, word, frequency);
    }
}