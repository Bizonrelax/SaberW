package com.saberw.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.saberw.core.PhraseFinder;
import com.saberw.core.Tokenizer;
import com.saberw.model.Candidate;
import com.saberw.model.Token;

public class Benchmark {
    
    public static void runPerformanceTest(String text, int iterations) {
        System.out.println("=== Производительность поиска фраз ===\n");
        
        Tokenizer tokenizer = new Tokenizer();
        PhraseFinder phraseFinder = new PhraseFinder();
        
        // Токенизация
        long startTime = System.currentTimeMillis();
        List<Token> tokens = tokenizer.tokenize(text);
        long tokenizeTime = System.currentTimeMillis() - startTime;
        
        System.out.printf("Токенизация: %,d токенов за %d мс%n", 
                         tokens.size(), tokenizeTime);
        
        // Старый алгоритм (если нужно сравнить)
        System.out.println("\n--- Старый алгоритм O(n²) ---");
        long oldTotalTime = 0;
        
        for (int i = 0; i < Math.min(iterations, 3); i++) { // Ограничим итерации для старого
            startTime = System.currentTimeMillis();
            List<Candidate> oldResult = findPhrasesOld(tokens, 5, 2, 7);
            long time = System.currentTimeMillis() - startTime;
            oldTotalTime += time;
            System.out.printf("Итерация %d: %d мс, найдено %d фраз%n", 
                            i + 1, time, oldResult.size());
        }
        
        // Новый оптимизированный алгоритм
        System.out.println("\n--- Новый оптимизированный алгоритм ---");
        long newTotalTime = 0;
        
        for (int i = 0; i < iterations; i++) {
            startTime = System.currentTimeMillis();
            List<Candidate> newResult = phraseFinder.findPhrasesOptimized(tokens, 5, 2, 7);
            long time = System.currentTimeMillis() - startTime;
            newTotalTime += time;
            System.out.printf("Итерация %d: %d мс, найдено %d фраз%n", 
                            i + 1, time, newResult.size());
        }
        
        // Сравнение
        System.out.println("\n=== Результаты ===");
        System.out.printf("Среднее время старого алгоритма: %.1f мс%n", 
                         (double)oldTotalTime / Math.min(iterations, 3));
        System.out.printf("Среднее время нового алгоритма: %.1f мс%n", 
                         (double)newTotalTime / iterations);
        System.out.printf("Ускорение: %.1f раз%n", 
                         (double)oldTotalTime / newTotalTime * iterations / Math.min(iterations, 3));
    }
    
    // Метод для старого алгоритма (для сравнения)
    private static List<Candidate> findPhrasesOld(List<Token> tokens, 
                                                  int maxPhraseTokens, 
                                                  int minFrequency, 
                                                  int minPhraseLength) {
        Map<String, Integer> phraseFrequency = new HashMap<>();
        int n = tokens.size();
        
        for (int i = 0; i < n; i++) {
            for (int len = 2; len <= maxPhraseTokens && i + len <= n; len++) {
                StringBuilder phraseText = new StringBuilder();
                for (int j = i; j < i + len; j++) {
                    phraseText.append(tokens.get(j).getValue());
                }
                
                String phrase = phraseText.toString();
                
                if (phrase.length() < minPhraseLength) {
                    continue;
                }
                
                phraseFrequency.put(phrase, phraseFrequency.getOrDefault(phrase, 0) + 1);
            }
        }
        
        List<Candidate> candidates = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : phraseFrequency.entrySet()) {
            if (entry.getValue() >= minFrequency) {
                candidates.add(new Candidate(entry.getKey(), entry.getValue()));
            }
        }
        
        candidates.sort((a, b) -> {
            int scoreA = a.getText().length() * a.getFrequency();
            int scoreB = b.getText().length() * b.getFrequency();
            return Integer.compare(scoreB, scoreA);
        });
        
        return candidates;
    }
    
    public static void testWithLargeText() {
        System.out.println("\n=== Тест с большим текстом ===");
        
        // Создаем большой текст для теста
        StringBuilder largeText = new StringBuilder();
        String baseText = "Программа SaberW предназначена для сжатия текста. ";
        
        // Увеличиваем текст в 100 раз
        for (int i = 0; i < 100; i++) {
            largeText.append(baseText);
        }
        
        System.out.printf("Размер текста: %,d символов%n", largeText.length());
        
        runPerformanceTest(largeText.toString(), 5);
    }
}