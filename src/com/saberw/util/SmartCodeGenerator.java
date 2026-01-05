package com.saberw.util;

import java.util.*;

public class SmartCodeGenerator {
    // Набор символов, которые мы будем использовать для кодирования
    private static final char[] CODE_CHARS = {
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
    
    private final String originalText;
    private final Set<Character> textChars;
    private int currentIndex;
    
    public SmartCodeGenerator(String originalText) {
        this.originalText = originalText;
        this.textChars = collectUniqueChars(originalText);
        this.currentIndex = 0;
    }
    
    // Собираем уникальные символы из исходного текста
    private Set<Character> collectUniqueChars(String text) {
        Set<Character> chars = new HashSet<>();
        for (char c : text.toCharArray()) {
            chars.add(c);
        }
        return chars;
    }
    
    // Получаем следующий код, которого нет в исходном тексте
    public String nextCode() {
        if (currentIndex >= CODE_CHARS.length) {
            // Если символы закончились, используем двухсимвольные коды
            return getFallbackCode();
        }
        
        char candidate = CODE_CHARS[currentIndex++];
        
        // Проверяем, есть ли этот символ в исходном тексте
        if (textChars.contains(candidate)) {
            // Если есть, ищем следующий доступный символ
            return findAvailableCode();
        }
        
        return String.valueOf(candidate);
    }
    
    // Ищем доступный символ, которого нет в тексте
    private String findAvailableCode() {
        for (int i = currentIndex; i < CODE_CHARS.length; i++) {
            char candidate = CODE_CHARS[i];
            if (!textChars.contains(candidate)) {
                currentIndex = i + 1;
                return String.valueOf(candidate);
            }
        }
        
        // Если все символы уже есть в тексте, используем fallback
        return getFallbackCode();
    }
    
    // Fallback: двухсимвольные коды с префиксом
    private String getFallbackCode() {
        // Создаем двухсимвольные коды вида "①", "②" и т.д.
        // Но для простоты пока возвращаем null (потом доработаем)
        return null;
    }
    
    // Проверяем, безопасно ли вставлять код без маркера
    public boolean isSafeToInsert(String code) {
        if (code == null || code.length() != 1) {
            return false;
        }
        
        char c = code.charAt(0);
        return !textChars.contains(c);
    }
    
    // Получить список всех используемых символов для кодов
    public static List<Character> getAllCodeChars() {
        List<Character> chars = new ArrayList<>();
        for (char c : CODE_CHARS) {
            chars.add(c);
        }
        return chars;
    }
}