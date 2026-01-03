package com.saberw.util;

import java.util.*;

public class ImprovedCodeGenerator {
    // Основной набор символов CJK Extension A (3400-4DBF)
    private static final char[] PRIMARY_CODES = {
        '㐀', '㐁', '㐂', '㐃', '㐄', '㐅', '㐆', '㐇', '㐈', '㐉',
        '㐊', '㐋', '㐌', '㐍', '㐎', '㐏', '㐐', '㐑', '㐒', '㐓',
        '㐔', '㐕', '㐖', '㐗', '㐘', '㐙', '㐚', '㐛', '㐜', '㐝',
        '㐞', '㐟', '㐠', '㐡', '㐢', '㐣', '㐤', '㐥', '㐦', '㐧',
        '㐨', '㐩', '㐪', '㐫', '㐬', '㐭', '㐮', '㐯', '㐰', '㐱',
        '㐲', '㐳', '㐴', '㐵', '㐶', '㐷', '㐸', '㐹', '㐺', '㐻',
        '㐼', '㐽', '㐾', '㐿', '㑀', '㑁', '㑂', '㑃', '㑄', '㑅',
        '㑆', '㑇', '㑈', '㑉', '㑊', '㑋', '㑌', '㑍', '㑎', '㑏',
        '㑐', '㑑', '㑒', '㑓', '㑔', '㑕', '㑖', '㑗', '㑘', '㑙',
        '㑚', '㑛', '㑜', '㑝', '㑞', '㑟', '㑠', '㑡', '㑢', '㑣'
    };
    
    // УПРОЩЕННЫЙ расширенный набор (без проблемных символов)
    private static final char[] EXTENDED_CODES = {
        '㑤', '㑥', '㑦', '㑧', '㑨', '㑩', '㑪', '㑫', '㑬', '㑭',
        '㑮', '㑯', '㑰', '㑱', '㑲', '㑳', '㑴', '㑵', '㑶', '㑷',
        '㑸', '㑹', '㑺', '㑻', '㑼', '㑽', '㑾', '㑿', '㒀', '㒁',
        '㒂', '㒃', '㒄', '㒅', '㒆', '㒇', '㒈', '㒉', '㒊', '㒋'
    };
    
    // Специальные символы для группового кодирования
    public static final char GROUP_MARKER = '●';      // U+25CF BLACK CIRCLE
    public static final char LOWER_CASE_MARKER = '↓'; // U+2193 DOWNWARDS ARROW
    
    private final String originalText;
    private final Set<Character> textCharacters;
    private final Set<String> textBigrams;
    private int primaryIndex;
    private int extendedIndex;
    private int bigramIndex1;
    private int bigramIndex2;
    
    public ImprovedCodeGenerator(String originalText) {
        this.originalText = originalText;
        this.textCharacters = collectCharacters(originalText);
        this.textBigrams = collectBigrams(originalText);
        this.primaryIndex = 0;
        this.extendedIndex = 0;
        this.bigramIndex1 = 0;
        this.bigramIndex2 = 0;
        
        // Убедимся, что маркеры не встречаются в тексте
        ensureMarkerSafety();
    }
    
    private Set<Character> collectCharacters(String text) {
        Set<Character> chars = new HashSet<>();
        for (char c : text.toCharArray()) {
            chars.add(c);
        }
        return chars;
    }
    
    private Set<String> collectBigrams(String text) {
        Set<String> bigrams = new HashSet<>();
        for (int i = 0; i < text.length() - 1; i++) {
            bigrams.add(text.substring(i, i + 2));
        }
        return bigrams;
    }
    
    private void ensureMarkerSafety() {
        // Если маркеры уже есть в тексте, заменяем их
        if (textCharacters.contains(GROUP_MARKER)) {
            System.err.println("Предупреждение: символ '" + GROUP_MARKER + 
                             "' уже есть в тексте. Групповое кодирование может работать некорректно.");
        }
        if (textCharacters.contains(LOWER_CASE_MARKER)) {
            System.err.println("Предупреждение: символ '" + LOWER_CASE_MARKER + 
                             "' уже есть в тексте.");
        }
    }
    
    /**
     * Получить следующий безопасный код (1 или 2 символа)
     */
    public String nextCode() {
        // 1. Пробуем основной набор (1 символ)
        while (primaryIndex < PRIMARY_CODES.length) {
            char candidate = PRIMARY_CODES[primaryIndex++];
            if (!textCharacters.contains(candidate)) {
                return String.valueOf(candidate);
            }
        }
        
        // 2. Пробуем расширенный набор (1 символ)
        while (extendedIndex < EXTENDED_CODES.length) {
            char candidate = EXTENDED_CODES[extendedIndex++];
            if (!textCharacters.contains(candidate)) {
                return String.valueOf(candidate);
            }
        }
        
        // 3. Генерируем 2-символьные коды
        return generateBigramCode();
    }
    
    private String generateBigramCode() {
        // Используем символы из основного набора для создания пар
        while (bigramIndex1 < PRIMARY_CODES.length) {
            char first = PRIMARY_CODES[bigramIndex1];
            while (bigramIndex2 < PRIMARY_CODES.length) {
                char second = PRIMARY_CODES[bigramIndex2++];
                String candidate = String.valueOf(first) + String.valueOf(second);
                
                // Проверяем, что эта пара не встречается в тексте
                if (!textBigrams.contains(candidate) && 
                    !textCharacters.contains(first) && 
                    !textCharacters.contains(second)) {
                    return candidate;
                }
            }
            bigramIndex1++;
            bigramIndex2 = 0;
        }
        
        // Если все комбинации исчерпаны (крайне маловероятно)
        throw new IllegalStateException("Нет доступных кодов для кодирования!");
    }
    
    /**
     * Проверить, безопасен ли код для вставки без маркера
     */
    public boolean isCodeSafe(String code) {
        if (code == null || code.isEmpty()) return false;
        
        if (code.length() == 1) {
            return !textCharacters.contains(code.charAt(0));
        } else if (code.length() == 2) {
            return !textBigrams.contains(code) &&
                   !textCharacters.contains(code.charAt(0)) &&
                   !textCharacters.contains(code.charAt(1));
        }
        return false;
    }
    
    /**
     * Получить все доступные символы для кодирования
     */
    public static List<Character> getAvailableCodeChars() {
        List<Character> chars = new ArrayList<>();
        for (char c : PRIMARY_CODES) chars.add(c);
        for (char c : EXTENDED_CODES) chars.add(c);
        return chars;
    }
    
    /**
     * Оптимизировать текст: привести к нижнему регистру (кроме кода)
     */
    public static String normalizeText(String text, boolean preserveCodeCase) {
        if (text == null) return null;
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            
            // Пропускаем уже закодированные символы
            if (preserveCodeCase && isLikelyCodeChar(c)) {
                result.append(c);
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        
        return result.toString();
    }
    
    private static boolean isLikelyCodeChar(char c) {
        // Проверяем, находится ли символ в диапазоне наших кодов
        return (c >= PRIMARY_CODES[0] && c <= PRIMARY_CODES[PRIMARY_CODES.length - 1]) ||
               (c >= EXTENDED_CODES[0] && c <= EXTENDED_CODES[EXTENDED_CODES.length - 1]);
    }
    
    /**
     * Оценить выгоду от кодирования слова
     * @return экономия в символах при кодировании этого слова
     */
    public static int calculateBenefit(String word, int frequency, String code) {
        if (word == null || code == null) return 0;
        
        int originalChars = word.length() * frequency;
        int encodedChars = code.length() * frequency + code.length() + word.length() + 3; // +3 для "=" и \n в словаре
        
        return originalChars - encodedChars;
    }
}