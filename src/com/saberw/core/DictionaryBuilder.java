package com.saberw.core;

import com.saberw.model.DictionaryEntry;
import com.saberw.model.Candidate;
import com.saberw.util.UnicodeHelper;
import com.saberw.util.ValidationHelper;

import java.util.*;

public class DictionaryBuilder {
    
    // Старый метод для совместимости
    public List<DictionaryEntry> buildDictionary(List<String> words, 
                                                Map<String, Integer> wordFrequency) {
        List<DictionaryEntry> dictionary = new ArrayList<>();
        
        if (words == null || words.isEmpty()) {
            return dictionary;
        }
        
        words.sort((w1, w2) -> {
            int freq1 = wordFrequency.getOrDefault(w1, 0);
            int freq2 = wordFrequency.getOrDefault(w2, 0);
            int score1 = freq1 * w1.length();
            int score2 = freq2 * w2.length();
            return Integer.compare(score2, score1);
        });
        
        UnicodeHelper.CodeGenerator codeGen = new UnicodeHelper.CodeGenerator();
        
        for (String word : words) {
            try {
                String code = codeGen.nextCode();
                int frequency = wordFrequency.getOrDefault(word, 0);
                dictionary.add(new DictionaryEntry(word, code, frequency));
            } catch (IllegalStateException e) {
                System.err.println("Warning: Cannot assign more codes. " + 
                                 "Dictionary limited to available characters.");
                break;
            }
        }
        
        return dictionary;
    }
    
    // НОВЫЙ МЕТОД: Назначение кодов кандидатам
    public static void assignCodes(List<Candidate> candidates) {
        UnicodeHelper.CodeGenerator codeGen = new UnicodeHelper.CodeGenerator();
        
        for (Candidate candidate : candidates) {
            try {
                candidate.setCode(codeGen.nextCode());
            } catch (IllegalStateException e) {
                System.err.println("Warning: Cannot assign more codes. " +
                                 "Dictionary limited to available characters.");
                break;
            }
        }
    }
    
    // НОВЫЙ МЕТОД: Построение словаря из кандидатов
    public static List<DictionaryEntry> buildDictionaryFromCandidates(List<Candidate> candidates) {
        List<DictionaryEntry> dictionary = new ArrayList<>();
        
        for (Candidate candidate : candidates) {
            dictionary.add(new DictionaryEntry(
                candidate.getText(), 
                candidate.getCode(), 
                candidate.getFrequency()
            ));
        }
        
        return dictionary;
    }
    
    // Форматирование словаря в текстовый вид
    public String formatDictionary(List<DictionaryEntry> dictionary) {
        StringBuilder sb = new StringBuilder();
        sb.append("# SABER_W DICTIONARY v1 #\n");
        
        for (DictionaryEntry entry : dictionary) {
            sb.append(entry.getCode())
              .append("=")
              .append(entry.getWord())
              .append("\n");
        }
        
        sb.append("# END #");
        return sb.toString();
    }
}