package com.saberw.core;

import com.saberw.model.*;
import com.saberw.util.ImprovedCodeGenerator;
import com.saberw.util.ValidationHelper;

import java.util.*;

public class SaberWCore {
    
    private final ImprovedCodeGenerator codeGenerator;
    private final boolean normalizeToLowercase;
    private final boolean aggressiveCompression;
    private final String originalText;
    
    // Конфигурация по умолчанию
    private static final int MIN_WORD_LENGTH = 3;
    private static final int MIN_FREQUENCY = 2;
    private static final int MIN_FREQUENCY_FOR_2CHARS = 6;
    private static final int MAX_PHRASE_TOKENS = 5;
    private static final int MIN_PHRASE_LENGTH = 7;
    
    public SaberWCore(String originalText) {
        this(originalText, false, false);
    }
    
    public SaberWCore(String originalText, boolean normalizeToLowercase, 
                     boolean aggressiveCompression) {
        this.normalizeToLowercase = normalizeToLowercase;
        this.aggressiveCompression = aggressiveCompression;
        this.originalText = originalText;
        
        // Нормализуем текст если нужно
        String processedText = originalText;
        if (normalizeToLowercase && !ValidationHelper.isLikelySourceCode(originalText)) {
            processedText = ImprovedCodeGenerator.normalizeText(originalText, false);
        }
        
        this.codeGenerator = new ImprovedCodeGenerator(processedText);
    }
    
    /**
     * Основной метод сжатия с улучшенной логикой
     */
    public CompressionResult compress() {
        return compress(MIN_WORD_LENGTH, MIN_FREQUENCY, MIN_FREQUENCY_FOR_2CHARS,
                       MAX_PHRASE_TOKENS, MIN_PHRASE_LENGTH);
    }
    
    public CompressionResult compress(int minWordLength, int minFrequency,
                                     int minFrequencyFor2Chars, int maxPhraseTokens,
                                     int minPhraseLength) {
        String textToProcess = getTextToProcess();
        
        if (textToProcess == null || textToProcess.isEmpty()) {
            return new CompressionResult("", Collections.emptyList(),
                new CompressionResult.Statistics(0, 0, 0));
        }
        
        // 1. Токенизация
        Tokenizer tokenizer = new Tokenizer();
        List<Token> tokens = tokenizer.tokenize(textToProcess);
        
        // 2. Анализ частотности с улучшенной логикой
        Map<String, Integer> wordFrequency = analyzeWordFrequency(tokens);
        
        // 3. Отбор кандидатов с учетом пользы
        List<Candidate> candidates = selectCandidates(wordFrequency, 
                                                     minWordLength, minFrequency,
                                                     minFrequencyFor2Chars);
        
        // 4. Назначение кодов
        assignCodes(candidates);
        
        // 5. Создание словаря
        List<DictionaryEntry> dictionary = createDictionary(candidates);
        
        // 6. Кодирование текста
        String encodedText = encodeText(textToProcess, dictionary);
        
        // 7. Групповое кодирование (если выгодно)
        String finalText = applyGroupCoding(encodedText, dictionary);
        
        // 8. Добавляем маркер нормализации если нужно
        if (normalizeToLowercase && !ValidationHelper.isLikelySourceCode(textToProcess)) {
            finalText = ImprovedCodeGenerator.LOWER_CASE_MARKER + finalText;
        }
        
        // 9. Статистика
        CompressionResult.Statistics stats = calculateStatistics(
            textToProcess, finalText, dictionary);
        
        return new CompressionResult(finalText, dictionary, stats);
    }
    
    private String getTextToProcess() {
        String text = originalText;
        if (normalizeToLowercase && !ValidationHelper.isLikelySourceCode(originalText)) {
            text = ImprovedCodeGenerator.normalizeText(originalText, false);
        }
        return text;
    }
    
    private Map<String, Integer> analyzeWordFrequency(List<Token> tokens) {
        Map<String, Integer> frequency = new HashMap<>();
        
        for (Token token : tokens) {
            if (token.isWord() && ValidationHelper.isAlphanumericWord(token.getValue())) {
                String word = token.getValue();
                frequency.put(word, frequency.getOrDefault(word, 0) + 1);
            }
        }
        
        return frequency;
    }
    
    private List<Candidate> selectCandidates(Map<String, Integer> wordFrequency,
                                            int minWordLength, int minFrequency,
                                            int minFrequencyFor2Chars) {
        List<Candidate> candidates = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
            String word = entry.getKey();
            int freq = entry.getValue();
            
            // Используем новый метод с пятью параметрами
            if (ValidationHelper.shouldEncodeWord(word, freq, minWordLength, 
                                                 minFrequency, minFrequencyFor2Chars)) {
                candidates.add(new Candidate(word, freq));
            }
        }
        
        // Сортируем по потенциальной выгоде (частота * длина)
        candidates.sort((a, b) -> {
            int benefitA = ImprovedCodeGenerator.calculateBenefit(a.getText(), 
                a.getFrequency(), "?"); // код пока неизвестен
            int benefitB = ImprovedCodeGenerator.calculateBenefit(b.getText(), 
                b.getFrequency(), "?");
            return Integer.compare(benefitB, benefitA);
        });
        
        return candidates;
    }
    
    private void assignCodes(List<Candidate> candidates) {
        for (Candidate candidate : candidates) {
            try {
                String code = codeGenerator.nextCode();
                if (codeGenerator.isCodeSafe(code)) {
                    candidate.setCode(code);
                } else {
                    candidate.setCode(null); // Не удалось назначить безопасный код
                }
            } catch (IllegalStateException e) {
                System.err.println("Не удалось назначить код для: " + 
                                 candidate.getText() + " - " + e.getMessage());
                candidate.setCode(null);
            }
        }
        
        // Удаляем кандидаты без кодов
        candidates.removeIf(c -> c.getCode() == null);
    }
    
    private List<DictionaryEntry> createDictionary(List<Candidate> candidates) {
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
    
    private String encodeText(String text, List<DictionaryEntry> dictionary) {
        // Сортируем слова по длине (от длинных к коротким)
        List<DictionaryEntry> sortedDict = new ArrayList<>(dictionary);
        sortedDict.sort((a, b) -> Integer.compare(b.getWord().length(), 
                                                 a.getWord().length()));
        
        StringBuilder result = new StringBuilder(text);
        
        for (DictionaryEntry entry : sortedDict) {
            String word = entry.getWord();
            String code = entry.getCode();
            
            int index = 0;
            while ((index = result.indexOf(word, index)) != -1) {
                // Проверяем границы слова
                if (isWholeWord(result, index, word.length())) {
                    result.replace(index, index + word.length(), code);
                    index += code.length();
                } else {
                    index += 1;
                }
            }
        }
        
        return result.toString();
    }
    
    private boolean isWholeWord(StringBuilder text, int start, int length) {
        // Проверяем, что слева и справа от слова не буквы/цифры
        if (start > 0 && Character.isLetterOrDigit(text.charAt(start - 1))) {
            return false;
        }
        if (start + length < text.length() && 
            Character.isLetterOrDigit(text.charAt(start + length))) {
            return false;
        }
        return true;
    }
    
    private String applyGroupCoding(String text, List<DictionaryEntry> dictionary) {
        // Собираем односимвольные коды
        Set<Character> singleCharCodes = new HashSet<>();
        for (DictionaryEntry entry : dictionary) {
            if (entry.getCode().length() == 1) {
                singleCharCodes.add(entry.getCode().charAt(0));
            }
        }
        
        if (singleCharCodes.isEmpty()) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        int i = 0;
        
        while (i < text.length()) {
            char current = text.charAt(i);
            
            // Если это одиночный код
            if (singleCharCodes.contains(current)) {
                // Ищем последовательность таких кодов
                int sequenceStart = i;
                while (i < text.length() && singleCharCodes.contains(text.charAt(i))) {
                    i++;
                }
                int sequenceLength = i - sequenceStart;
                
                // Если последовательность >=2, применяем групповое кодирование
                if (sequenceLength >= 2 && sequenceLength <= 9) {
                    result.append(ImprovedCodeGenerator.GROUP_MARKER)
                          .append((char)('0' + sequenceLength))
                          .append(text.substring(sequenceStart, i));
                } else {
                    result.append(text.substring(sequenceStart, i));
                }
            } else {
                result.append(current);
                i++;
            }
        }
        
        return result.toString();
    }
    
    private CompressionResult.Statistics calculateStatistics(
            String original, String compressed, List<DictionaryEntry> dictionary) {
        
        int originalLength = original.length();
        int compressedLength = compressed.length();
        int dictEntries = dictionary.size();
        
        // Учитываем размер словаря в сжатом результате
        int dictSize = 0;
        for (DictionaryEntry entry : dictionary) {
            dictSize += entry.getCode().length() + entry.getWord().length() + 3; // "= \n"
        }
        
        return new CompressionResult.Statistics(
            originalLength, compressedLength + dictSize, dictEntries);
    }
    
    /**
     * Декомпрессия с поддержкой новых функций
     */
    public String decompress(String compressedText, List<DictionaryEntry> dictionary) {
        if (compressedText == null || dictionary == null) {
            return compressedText;
        }
        
        // Проверяем маркер нижнего регистра
        boolean wasLowercased = false;
        if (compressedText.length() > 0 && 
            compressedText.charAt(0) == ImprovedCodeGenerator.LOWER_CASE_MARKER) {
            wasLowercased = true;
            compressedText = compressedText.substring(1);
        }
        
        // Создаем карты для декодирования
        Map<String, String> codeToWord = new HashMap<>();
        Set<Character> singleCharCodes = new HashSet<>();
        
        for (DictionaryEntry entry : dictionary) {
            codeToWord.put(entry.getCode(), entry.getWord());
            if (entry.getCode().length() == 1) {
                singleCharCodes.add(entry.getCode().charAt(0));
            }
        }
        
        StringBuilder result = new StringBuilder();
        int i = 0;
        
        while (i < compressedText.length()) {
            char current = compressedText.charAt(i);
            
            // Групповое кодирование
            if (current == ImprovedCodeGenerator.GROUP_MARKER && i + 1 < compressedText.length()) {
                char countChar = compressedText.charAt(i + 1);
                if (Character.isDigit(countChar)) {
                    int groupSize = countChar - '0';
                    i += 2; // Пропускаем маркер и цифру
                    
                    // Декодируем группу
                    for (int j = 0; j < groupSize && i < compressedText.length(); j++) {
                        char codeChar = compressedText.charAt(i++);
                        String code = String.valueOf(codeChar);
                        if (codeToWord.containsKey(code)) {
                            result.append(codeToWord.get(code));
                        } else {
                            result.append(codeChar);
                        }
                    }
                    continue;
                }
            }
            
            // Одиночные коды (1 символ)
            if (singleCharCodes.contains(current)) {
                String code = String.valueOf(current);
                if (codeToWord.containsKey(code)) {
                    result.append(codeToWord.get(code));
                } else {
                    result.append(current);
                }
                i++;
            } 
            // Двухсимвольные коды
            else if (i + 1 < compressedText.length()) {
                String potentialCode = compressedText.substring(i, i + 2);
                if (codeToWord.containsKey(potentialCode)) {
                    result.append(codeToWord.get(potentialCode));
                    i += 2;
                } else {
                    result.append(current);
                    i++;
                }
            } else {
                result.append(current);
                i++;
            }
        }
        
        String finalText = result.toString();
        
        // Если текст был приведен к нижнему регистру, восстанавливаем регистр
        // (упрощенная версия - просто возвращаем как есть)
        if (wasLowercased) {
            // В реальности здесь может быть более сложная логика восстановления
            return finalText;
        }
        
        return finalText;
    }
}