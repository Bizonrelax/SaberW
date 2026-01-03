package com.saberw.core;

import com.saberw.model.*;
import com.saberw.util.SmartCodeGenerator;
import com.saberw.util.ValidationHelper;

import java.util.*;

public class SaberWCompressor {
    
    public CompressionResult compress(String text) {
        return compress(text, 2, 5, 7);
    }
    
    public CompressionResult compress(String text, int minFrequency, 
                                     int maxPhraseTokens, int minPhraseLength) {
        if (text == null || text.isEmpty()) {
            return new CompressionResult("", Collections.emptyList(), 
                new CompressionResult.Statistics(0, 0, 0));
        }
        
        // 1. Токенизация
        Tokenizer tokenizer = new Tokenizer();
        List<Token> tokens = tokenizer.tokenize(text);
        
        // 2. Анализ частотности слов
        Map<String, Integer> wordFrequency = new HashMap<>();
        for (Token token : tokens) {
            if (token.isWord() && ValidationHelper.shouldEncodeWord(token.getValue())) {  // Используем старый метод
                String word = token.getValue();
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }
        
        // 3. Создание кандидатов для слов (только слова, фразы временно убираем)
        List<Candidate> candidates = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
            if (entry.getValue() >= minFrequency) {
                candidates.add(new Candidate(entry.getKey(), entry.getValue()));
            }
        }
        
        // Сортируем по приоритету (длина * частота)
        candidates.sort((a, b) -> {
            int scoreA = a.getText().length() * a.getFrequency();
            int scoreB = b.getText().length() * b.getFrequency();
            return Integer.compare(scoreB, scoreA);
        });
        
        // 4. Назначаем коды
        SmartCodeGenerator codeGen = new SmartCodeGenerator(text);
        Map<String, String> wordToCode = new HashMap<>();
        List<DictionaryEntry> dictionary = new ArrayList<>();
        
        for (Candidate candidate : candidates) {
            String code = codeGen.nextCode();
            if (code != null && codeGen.isSafeToInsert(code)) {
                candidate.setCode(code);
                wordToCode.put(candidate.getText(), code);
                dictionary.add(new DictionaryEntry(candidate.getText(), code, candidate.getFrequency()));
            }
        }
        
        // 5. Кодирование текста
        String compressedText = encodeTextSimple(text, wordToCode);
        
        // 6. Группировка последовательных кодов (без маркера!)
        String groupedText = groupCodesWithoutMarker(compressedText, dictionary);
        
        // 7. Подсчёт статистики
        CompressionResult.Statistics stats = new CompressionResult.Statistics(
            text.length(),
            groupedText.length(),
            dictionary.size()
        );
        
        return new CompressionResult(groupedText, dictionary, stats);
    }
    
    // Простое кодирование текста
    private String encodeTextSimple(String text, Map<String, String> wordToCode) {
        // Сортируем слова по длине (от длинных к коротким)
        List<Map.Entry<String, String>> entries = new ArrayList<>(wordToCode.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));
        
        StringBuilder result = new StringBuilder(text);
        
        for (Map.Entry<String, String> entry : entries) {
            String word = entry.getKey();
            String code = entry.getValue();
            
            int index = 0;
            while ((index = result.indexOf(word, index)) != -1) {
                result.replace(index, index + word.length(), code);
                index += code.length();
            }
        }
        
        return result.toString();
    }
    
    // Группировка кодов без маркера
    private String groupCodesWithoutMarker(String text, List<DictionaryEntry> dictionary) {
        // Создаем карту для быстрой проверки: символ → это код?
        Set<Character> codeChars = new HashSet<>();
        for (DictionaryEntry entry : dictionary) {
            String code = entry.getCode();
            if (code != null && code.length() == 1) {
                codeChars.add(code.charAt(0));
            }
        }
        
        StringBuilder result = new StringBuilder();
        int i = 0;
        
        while (i < text.length()) {
            char current = text.charAt(i);
            
            // Если это символ кода
            if (codeChars.contains(current)) {
                // Собираем последовательность кодов
                List<Character> codes = new ArrayList<>();
                int j = i;
                
                while (j < text.length() && codeChars.contains(text.charAt(j))) {
                    codes.add(text.charAt(j));
                    j++;
                }
                
                // Если нашли 2 или больше кодов подряд
                if (codes.size() >= 2) {
                    // Создаем группу: специальный символ + количество + коды
                    char groupMarker = '●'; // U+25CF, BLACK CIRCLE
                    int groupSize = Math.min(codes.size(), 9);
                    result.append(groupMarker).append((char)('0' + groupSize));
                    
                    for (int k = 0; k < groupSize; k++) {
                        result.append(codes.get(k));
                    }
                    
                    i = j;
                } else {
                    // Одиночный код
                    result.append(current);
                    i++;
                }
            } else {
                // Обычный символ
                result.append(current);
                i++;
            }
        }
        
        return result.toString();
    }
    
    // Декомпрессия
    public String decompress(String compressedText, List<DictionaryEntry> dictionary) {
        if (compressedText == null || dictionary == null) {
            return compressedText;
        }
        
        // Создаем карту для быстрого доступа: код → слово
        Map<String, String> codeToWord = new HashMap<>();
        for (DictionaryEntry entry : dictionary) {
            codeToWord.put(entry.getCode(), entry.getWord());
        }
        
        // Также создаем карту: символ → это код?
        Set<Character> codeChars = new HashSet<>();
        for (DictionaryEntry entry : dictionary) {
            String code = entry.getCode();
            if (code != null && code.length() == 1) {
                codeChars.add(code.charAt(0));
            }
        }
        
        StringBuilder result = new StringBuilder();
        int i = 0;
        
        while (i < compressedText.length()) {
            char current = compressedText.charAt(i);
            
            // Если это маркер группы (●)
            if (current == '●' && i + 1 < compressedText.length()) {
                char nextChar = compressedText.charAt(i + 1);
                
                if (Character.isDigit(nextChar)) {
                    int groupSize = nextChar - '0';
                    i += 2; // Пропускаем маркер и цифру
                    
                    // Обрабатываем группу кодов
                    for (int j = 0; j < groupSize && i < compressedText.length(); j++) {
                        char codeChar = compressedText.charAt(i++);
                        String code = String.valueOf(codeChar);
                        
                        if (codeToWord.containsKey(code)) {
                            result.append(codeToWord.get(code));
                        } else {
                            result.append(codeChar);
                        }
                    }
                } else {
                    result.append(current);
                    i++;
                }
            } else if (codeChars.contains(current)) {
                // Одиночный код
                String code = String.valueOf(current);
                
                if (codeToWord.containsKey(code)) {
                    result.append(codeToWord.get(code));
                } else {
                    result.append(current);
                }
                i++;
            } else {
                // Обычный символ
                result.append(current);
                i++;
            }
        }
        
        return result.toString();
    }
}