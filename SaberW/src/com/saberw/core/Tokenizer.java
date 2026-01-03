package com.saberw.core;

import com.saberw.model.Token;
import com.saberw.model.Candidate;
import com.saberw.util.ValidationHelper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
    
    private static final Pattern TOKEN_PATTERN = 
        Pattern.compile("([\\p{L}\\p{N}_]+|[^\\p{L}\\p{N}_]+)");
    private final PhraseFinder phraseFinder;
    
    public Tokenizer() {
        this.phraseFinder = new PhraseFinder();
    }
    
    public List<Token> tokenize(String text) {
        List<Token> tokens = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return tokens;
        }
        
        Matcher matcher = TOKEN_PATTERN.matcher(text);
        int position = 0;
        
        while (matcher.find()) {
            String value = matcher.group();
            Token.Type type = isWord(value) ? Token.Type.WORD : Token.Type.SEPARATOR;
            
            tokens.add(new Token(value, type, position));
            position += value.length();
        }
        
        return tokens;
    }
    
    
    private boolean isWord(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        char firstChar = token.charAt(0);
        return Character.isLetterOrDigit(firstChar) || firstChar == '_';
    }
    
   
    
    public List<Candidate> findPhraseCandidates(List<Token> tokens, 
                                                int maxPhraseTokens, 
                                                int minFrequency, 
                                                int minPhraseLength) {
        
        return phraseFinder.findPhrasesOptimized(tokens, maxPhraseTokens, minFrequency, minPhraseLength);
        
        // ИЛИ для очень больших текстов можно использовать:
        // return phraseFinder.findPhrasesWithSuffixArray(tokens, maxPhraseTokens, minFrequency, minPhraseLength);
    }
 // Метод для поиска слов для кодирования (как раньше)
    public List<String> findWordsToEncode(String text, int minFrequency) {
        List<Token> tokens = tokenize(text);
        
        Map<String, Integer> wordFrequency = new HashMap<>();
        
        for (Token token : tokens) {
            if (token.isWord() && ValidationHelper.shouldEncodeWord(token.getValue())) {  // Используем старый метод
                String word = token.getValue();
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }
        
        List<String> wordsToEncode = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
            if (entry.getValue() >= minFrequency) {
                wordsToEncode.add(entry.getKey());
            }
        }
        
        return wordsToEncode;
    }
}