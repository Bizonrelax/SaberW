package com.saberw.util;

public class ValidationHelper {
    
    // СТАРЫЙ МЕТОД для совместимости с существующим кодом
    public static boolean shouldEncodeWord(String word) {
        if (word == null || word.length() < 4) {
            return false;
        }
        
        for (char c : word.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        
        return true;
    }
    
    // НОВЫЙ МЕТОД с расширенными параметрами
    public static boolean shouldEncodeWord(String word, int frequency, 
                                          int minLength, int minFrequency,
                                          int minLengthForTwoChars) {
        if (word == null || word.length() < 2) {
            return false;
        }
        
        // Для 2-символьных слов нужна высокая частота
        if (word.length() == 2) {
            return frequency >= minLengthForTwoChars;
        }
        
        // Для слов >= minLength проверяем частоту
        if (word.length() >= minLength) {
            return frequency >= minFrequency;
        }
        
        return false;
    }
    
    // Проверяем, состоит ли слово из букв/цифр
    public static boolean isAlphanumericWord(String word) {
        if (word == null) return false;
        
        for (char c : word.toCharArray()) {
            if (!Character.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }
    
    // Проверяем, содержит ли текст специальные символы SaberW
    public static boolean containsSaberWSymbols(String text) {
        if (text == null) return false;
        
        char groupMarker = '●';  // Временно, пока не исправим ImprovedCodeGenerator
        char lowerMarker = '↓';
        
        return text.indexOf(groupMarker) >= 0 ||
               text.indexOf(lowerMarker) >= 0;
    }
    
    // Экранирование специальных символов
    public static String escapeSaberWSymbols(String text) {
        if (text == null) return null;
        
        char groupMarker = '●';
        char lowerMarker = '↓';
        
        String result = text;
        result = result.replace(String.valueOf(groupMarker), 
                              String.valueOf(groupMarker) + groupMarker);
        result = result.replace(String.valueOf(lowerMarker), 
                              String.valueOf(lowerMarker) + lowerMarker);
        
        return result;
    }
    
    // Проверяем, является ли текст кодом (посторонние символы)
    public static boolean isLikelySourceCode(String text) {
        if (text == null || text.length() < 10) return false;
        
        // Эвристики для определения кода
        String[] codeIndicators = {
            "public ", "private ", "class ", "void ", "return ",
            "import ", "package ", "System.out", "for (", "if (",
            "while (", "try {", "catch (", "def ", "func ",
            "<?php", "<html", "<script", "#include", "using "
        };
        
        for (String indicator : codeIndicators) {
            if (text.contains(indicator)) {
                return true;
            }
        }
        
        return false;
    }
}