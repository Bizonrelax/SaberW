package com.saberw.util;

public class UnicodeHelper {
    // Маркер для кодирования
    public static final char MARKER = '\u25CC'; // ◌
    
    // Начальный символ для кодов (CJK Extension A)
    public static final char CODE_START = '\u3400';
    
    // Проверяем, является ли символ валидным кодом из нашего диапазона
    public static boolean isValidCodeChar(char c) {
        return c >= CODE_START && c < '\u4DBF'; // CJK Extension A диапазон
    }
    
    // Генератор последовательных кодов
    public static class CodeGenerator {
        private char currentCode;
        
        public CodeGenerator() {
            this.currentCode = CODE_START;
        }
        
        public String nextCode() {
            if (currentCode > '\u4DBF') {
                throw new IllegalStateException("No more available code characters!");
            }
            return String.valueOf(currentCode++);
        }
        
        public char getCurrentCode() {
            return currentCode;
        }
        
        public void reset() {
            this.currentCode = CODE_START;
        }
    }
}