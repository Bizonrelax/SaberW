package com.saberw.util;

import java.util.List;

public class DualFormatBuilder {
    
    /**
     * Создает сообщение в двойном формате:
     * 1. Верхняя часть: сжатый текст (для экономии места)
     * 2. Нижняя часть: мини-словарь для быстрой расшифровки
     * 3. При желании: полный текст (закомментирован)
     */
    public static String buildDualMessage(String compressedText, 
                                         List<com.saberw.model.DictionaryEntry> dictionary,
                                         String fullText) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("╔══════════════════════════════════════════════╗\n");
        sb.append("║           СЖАТЫЙ ТЕКСТ (экономия ").append(calculateSavings(fullText, compressedText)).append("%)          ║\n");
        sb.append("╚══════════════════════════════════════════════╝\n");
        sb.append(compressedText).append("\n\n");
        
        sb.append("╔══════════════════════════════════════════════╗\n");
        sb.append("║             МИНИ-СЛОВАРЬ (ключ)              ║\n");
        sb.append("╚══════════════════════════════════════════════╝\n");
        
        // Только самые важные замены (первые 10)
        int limit = Math.min(10, dictionary.size());
        for (int i = 0; i < limit; i++) {
            com.saberw.model.DictionaryEntry entry = dictionary.get(i);
            sb.append(entry.getCode()).append(" = ").append(entry.getWord());
            if (i < limit - 1) sb.append(" | ");
            if ((i + 1) % 5 == 0) sb.append("\n");
        }
        
        sb.append("\n\n");
        sb.append("╔══════════════════════════════════════════════╗\n");
        sb.append("║        ПОЛНЫЙ ТЕКСТ (для справки)            ║\n");
        sb.append("╚══════════════════════════════════════════════╝\n");
        // Комментируем полный текст, чтобы он не занимал токены при анализе
        sb.append("/*\n").append(fullText).append("\n*/\n");
        
        return sb.toString();
    }
    
    /**
     * Формат для быстрой переписки (только самое важное)
     */
    public static String buildQuickMessage(String compressedText,
                                          List<com.saberw.model.DictionaryEntry> dictionary) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("【СЖАТО】: ").append(compressedText).append("\n");
        sb.append("【СЛОВАРЬ】: ");
        
        // Только 5 самых частых замен
        int limit = Math.min(5, dictionary.size());
        for (int i = 0; i < limit; i++) {
            com.saberw.model.DictionaryEntry entry = dictionary.get(i);
            sb.append(entry.getCode()).append("→").append(entry.getWord());
            if (i < limit - 1) sb.append(", ");
        }
        
        return sb.toString();
    }
    
    private static int calculateSavings(String original, String compressed) {
        if (original == null || compressed == null) return 0;
        double savings = (1 - (double)compressed.length() / original.length()) * 100;
        return (int) Math.round(savings);
    }
}