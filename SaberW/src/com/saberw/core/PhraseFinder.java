package com.saberw.core;

import com.saberw.model.Token;
import com.saberw.model.Candidate;
import com.saberw.util.ValidationHelper;

import java.util.*;

public class PhraseFinder {
    
    // Основной метод поиска фраз с использованием хэш-таблиц
    public List<Candidate> findPhrasesOptimized(List<Token> tokens, 
                                                int maxPhraseTokens, 
                                                int minFrequency, 
                                                int minPhraseLength) {
        
        List<Candidate> candidates = new ArrayList<>();
        
        if (tokens == null || tokens.size() < 2) {
            return candidates;
        }
        
        // Создаем список значений токенов для удобства
        List<String> tokenValues = new ArrayList<>();
        for (Token token : tokens) {
            tokenValues.add(token.getValue());
        }
        
        // Мапа для хранения фраз и их частот
        Map<String, Integer> phraseFrequencies = new HashMap<>();
        
        // Хэш-таблица для быстрого поиска дубликатов
        Map<Integer, List<PhrasePosition>> hashToPositions = new HashMap<>();
        
        // Перебираем все возможные длины фраз
        for (int phraseLength = 2; phraseLength <= maxPhraseTokens; phraseLength++) {
            if (phraseLength > tokenValues.size()) break;
            
            // Очищаем хэш-таблицу для новой длины
            hashToPositions.clear();
            
            // Вычисляем хэш для первой фразы
            StringBuilder firstPhraseBuilder = new StringBuilder();
            for (int i = 0; i < phraseLength; i++) {
                firstPhraseBuilder.append(tokenValues.get(i));
            }
            String firstPhrase = firstPhraseBuilder.toString();
            int firstHash = firstPhrase.hashCode();
            
            // Добавляем первую позицию
            List<PhrasePosition> positions = new ArrayList<>();
            positions.add(new PhrasePosition(0, firstPhrase));
            hashToPositions.put(firstHash, positions);
            
            // Используем rolling hash для эффективного вычисления
            for (int i = 1; i <= tokenValues.size() - phraseLength; i++) {
                // Быстрое обновление фразы (удаляем первый токен, добавляем последний)
                String previousFirstToken = tokenValues.get(i - 1);
                String newLastToken = tokenValues.get(i + phraseLength - 1);
                
                // Получаем предыдущую фразу
                String prevPhrase = getPhrase(tokenValues, i - 1, phraseLength);
                String currentPhrase = prevPhrase.substring(previousFirstToken.length()) + newLastToken;
                
                int currentHash = currentPhrase.hashCode();
                
                // Добавляем в хэш-таблицу
                positions = hashToPositions.get(currentHash);
                if (positions == null) {
                    positions = new ArrayList<>();
                    hashToPositions.put(currentHash, positions);
                }
                positions.add(new PhrasePosition(i, currentPhrase));
            }
            
            // Теперь обрабатываем найденные фразы с одинаковым хэшем
            for (List<PhrasePosition> phrasePositions : hashToPositions.values()) {
                if (phrasePositions.size() < minFrequency) {
                    continue;
                }
                
                // Группируем идентичные фразы (на случай коллизий хэша)
                Map<String, Integer> exactMatches = new HashMap<>();
                for (PhrasePosition pos : phrasePositions) {
                    exactMatches.put(pos.phrase, exactMatches.getOrDefault(pos.phrase, 0) + 1);
                }
                
                // Добавляем фразы, удовлетворяющие условиям
                for (Map.Entry<String, Integer> entry : exactMatches.entrySet()) {
                    if (entry.getValue() >= minFrequency && entry.getKey().length() >= minPhraseLength) {
                        candidates.add(new Candidate(entry.getKey(), entry.getValue()));
                    }
                }
            }
        }
        
        // Удаляем дубликаты (фразы могут быть найдены для разных длин)
        candidates = removeDuplicatePhrases(candidates);
        
        // Сортируем по приоритету (длина * частота)
        candidates.sort((a, b) -> {
            int scoreA = a.getText().length() * a.getFrequency();
            int scoreB = b.getText().length() * b.getFrequency();
            return Integer.compare(scoreB, scoreA);
        });
        
        return candidates;
    }
    
    // Вспомогательный метод для получения фразы
    private String getPhrase(List<String> tokenValues, int start, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + length && i < tokenValues.size(); i++) {
            sb.append(tokenValues.get(i));
        }
        return sb.toString();
    }
    
    // Удаляем дубликаты и подфразы
    private List<Candidate> removeDuplicatePhrases(List<Candidate> candidates) {
        List<Candidate> result = new ArrayList<>();
        Set<String> seenPhrases = new HashSet<>();
        
        // Сортируем по длине (от длинных к коротким)
        candidates.sort((a, b) -> Integer.compare(b.getText().length(), a.getText().length()));
        
        for (Candidate candidate : candidates) {
            String phrase = candidate.getText();
            boolean isSubphrase = false;
            
            // Проверяем, не является ли эта фраза подфразой уже добавленной более длинной фразы
            for (String existingPhrase : seenPhrases) {
                if (existingPhrase.contains(phrase)) {
                    isSubphrase = true;
                    break;
                }
            }
            
            if (!isSubphrase) {
                result.add(candidate);
                seenPhrases.add(phrase);
            }
        }
        
        return result;
    }
    
    // Внутренний класс для хранения позиции фразы
    private static class PhrasePosition {
        final int startIndex;
        final String phrase;
        
        PhrasePosition(int startIndex, String phrase) {
            this.startIndex = startIndex;
            this.phrase = phrase;
        }
    }
    
    // Альтернативный метод: использование суффиксного массива (более эффективный для очень больших текстов)
    public List<Candidate> findPhrasesWithSuffixArray(List<Token> tokens, 
                                                      int maxPhraseTokens, 
                                                      int minFrequency, 
                                                      int minPhraseLength) {
        List<Candidate> candidates = new ArrayList<>();
        
        // Создаем строковое представление токенов с разделителями
        StringBuilder textBuilder = new StringBuilder();
        for (Token token : tokens) {
            textBuilder.append(token.getValue());
        }
        String text = textBuilder.toString();
        
        // Создаем массив начальных позиций токенов
        int[] tokenStarts = new int[tokens.size()];
        int position = 0;
        for (int i = 0; i < tokens.size(); i++) {
            tokenStarts[i] = position;
            position += tokens.get(i).getValue().length();
        }
        
        // Находим все повторяющиеся подстроки минимальной длины
        Map<String, Integer> phraseMap = new HashMap<>();
        
        // Используем окно для поиска повторов
        for (int windowSize = minPhraseLength; windowSize <= maxPhraseTokens * 10; windowSize++) {
            // Это упрощенный алгоритм - в реальности нужно реализовать суффиксный массив
            // или использовать библиотеку
            
            Map<String, Integer> windowPhrases = new HashMap<>();
            
            for (int i = 0; i <= text.length() - windowSize; i++) {
                String substring = text.substring(i, i + windowSize);
                
                // Проверяем, что подстрока начинается и заканчивается на границе токена
                if (isOnTokenBoundary(i, windowSize, tokenStarts)) {
                    windowPhrases.put(substring, windowPhrases.getOrDefault(substring, 0) + 1);
                }
            }
            
            // Добавляем фразы с достаточной частотой
            for (Map.Entry<String, Integer> entry : windowPhrases.entrySet()) {
                if (entry.getValue() >= minFrequency) {
                    phraseMap.put(entry.getKey(), entry.getValue());
                }
            }
        }
        
        // Конвертируем в кандидаты
        for (Map.Entry<String, Integer> entry : phraseMap.entrySet()) {
            candidates.add(new Candidate(entry.getKey(), entry.getValue()));
        }
        
        return candidates;
    }
    
    private boolean isOnTokenBoundary(int start, int length, int[] tokenStarts) {
        // Проверяем, что start и start+length находятся на границах токенов
        boolean startOk = false;
        boolean endOk = false;
        
        for (int tokenStart : tokenStarts) {
            if (tokenStart == start) startOk = true;
            if (tokenStart == start + length) endOk = true;
        }
        
        return startOk && endOk;
    }
}