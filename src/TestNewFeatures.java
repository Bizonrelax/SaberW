import com.saberw.core.SaberWCore;
import com.saberw.model.CompressionResult;
import com.saberw.model.DictionaryEntry;
import com.saberw.util.ImprovedCodeGenerator;

import java.util.List;

public class TestNewFeatures {
    public static void main(String[] args) {
        System.out.println("=== Тест новых функций SaberW (Сессия 3) ===\n");
        
        // Тест 1: 2-символьные слова с высокой частотой
        testTwoCharWords();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Тест 2: Нормализация регистра
        testCaseNormalization();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Тест 3: Групповое кодирование
        testGroupCoding();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Тест 4: Реальный Java код
        testJavaCode();
    }
    
    private static void testTwoCharWords() {
        System.out.println("Тест 1: 2-символьные слова (да, не, за)");
        
        String text = "Да, не за что, да не говори, да не беспокойся, " +
                     "за это да, не то чтобы да, за всё да, не только да.";
        
        System.out.println("Исходный текст: " + text);
        System.out.println("Длина: " + text.length() + " символов");
        
        SaberWCore core = new SaberWCore(text, false, false);
        CompressionResult result = core.compress(2, 2, 3, 5, 7);
        // Минимум 3 повторения для 2-символьных слов (для теста)
        
        System.out.println("\nСжатый текст: " + result.getCompressedText());
        System.out.println("\nСтатистика: " + result.getStats());
        
        System.out.println("\nСловарь:");
        for (DictionaryEntry entry : result.getDictionary()) {
            System.out.println("  " + entry);
        }
        
        // Декомпрессия
        String decompressed = core.decompress(result.getCompressedText(), 
                                            result.getDictionary());
        System.out.println("\nДекомпрессия успешна: " + 
                          text.equalsIgnoreCase(decompressed));
    }
    
    private static void testCaseNormalization() {
        System.out.println("Тест 2: Нормализация регистра");
        
        String text = "Программа SaberW сжимает Текст. " +
                     "Текст становится меньше. Текст удобнее.";
        
        System.out.println("Исходный текст: " + text);
        
        // Без нормализации
        SaberWCore core1 = new SaberWCore(text, false, false);
        CompressionResult result1 = core1.compress();
        
        // С нормализацией
        SaberWCore core2 = new SaberWCore(text, true, false);
        CompressionResult result2 = core2.compress();
        
        System.out.println("\nБез нормализации:");
        System.out.println("  Сжатый: " + result1.getCompressedText().substring(0, 
            Math.min(50, result1.getCompressedText().length())) + "...");
        System.out.println("  Экономия: " + 
                          String.format("%.1f%%", (1 - result1.getStats().getCompressionRatio()) * 100));
        
        System.out.println("\nС нормализацией:");
        System.out.println("  Сжатый: " + result2.getCompressedText().substring(0, 
            Math.min(50, result2.getCompressedText().length())) + "...");
        System.out.println("  Экономия: " + 
                          String.format("%.1f%%", (1 - result2.getStats().getCompressionRatio()) * 100));
    }
    
    private static void testGroupCoding() {
        System.out.println("Тест 3: Групповое кодирование");
        
        String text = "программа" + "программа" + "программа" + "программа" + 
                     " текст" + " текст" + " текст";
        
        System.out.println("Исходный текст (без пробелов): " + text);
        
        SaberWCore core = new SaberWCore(text, false, false);
        CompressionResult result = core.compress();
        
        System.out.println("\nСжатый текст: " + result.getCompressedText());
        
        // Проверяем наличие группового кодирования
        boolean hasGroups = result.getCompressedText().contains(
            String.valueOf(ImprovedCodeGenerator.GROUP_MARKER));
        System.out.println("Групповое кодирование применено: " + 
                          (hasGroups ? "ДА ✓" : "НЕТ ✗"));
        
        // Декомпрессия
        String decompressed = core.decompress(result.getCompressedText(), 
                                            result.getDictionary());
        System.out.println("\nТочное восстановление: " + 
                          text.equals(decompressed));
    }
    
    private static void testJavaCode() {
        System.out.println("Тест 4: Сжатие Java кода");
        
        String code = """
            public class Test {
                public static void main(String[] args) {
                    System.out.println("Hello, World!");
                    System.out.println("Hello, World!");
                    System.out.println("Hello, World!");
                    String message = "Hello, World!";
                    System.out.println(message);
                }
            }
            """;
        
        System.out.println("Исходный код (первые 100 символов):");
        System.out.println(code.substring(0, Math.min(100, code.length())) + "...");
        
        // Не нормализуем регистр для кода
        SaberWCore core = new SaberWCore(code, false, true);
        CompressionResult result = core.compress(2, 2, 5, 5, 10);
        
        System.out.println("\nСжатый текст (первые 100 символов):");
        System.out.println(result.getCompressedText().substring(0, 
            Math.min(100, result.getCompressedText().length())) + "...");
        
        System.out.println("\nСтатистика:");
        System.out.println("  " + result.getStats());
        
        // Проверяем, что System.out.println попал в словарь
        boolean hasSystemOut = false;
        for (DictionaryEntry entry : result.getDictionary()) {
            if (entry.getWord().contains("System.out")) {
                hasSystemOut = true;
                System.out.println("\nНайдено в словаре: " + entry);
            }
        }
        
        // Декомпрессия
        String decompressed = core.decompress(result.getCompressedText(), 
                                            result.getDictionary());
        System.out.println("\nТочное восстановление кода: " + 
                          code.equals(decompressed));
    }
}