package com.saberw.model;

public class Candidate {
    private final String text;
    private final int frequency;
    private String code;
    
    public Candidate(String text, int frequency) {
        this.text = text;
        this.frequency = frequency;
    }
    
    public String getText() { return text; }
    public int getFrequency() { return frequency; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    @Override
    public String toString() {
        return String.format("Candidate[text='%s', freq=%d, code=%s]", 
            text, frequency, code != null ? code : "null");
    }
}