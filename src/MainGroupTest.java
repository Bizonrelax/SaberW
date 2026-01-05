import com.saberw.core.SaberWCompressor;
import com.saberw.model.CompressionResult;
import com.saberw.model.DictionaryEntry;

import java.util.List;

public class MainGroupTest {
    public static void main(String[] args) {
        System.out.println("=== Тест новой системы (без маркера) ===\n");
        
        // Тест 1: Простые слова
        System.out.println("Тест 1: Пять одинаковых слов");
        String testText1 = "программа программа программа программа программа";
        runTest(testText1, 2);
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Тест 2: Разные слова с пробелами
        System.out.println("Тест 2: Разные слова");
        String testText2 = "один два три четыре пять один два три четыре пять";
        runTest(testText2, 2);
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Тест 3: Текст с разными символами
        System.out.println("Тест 3: Текст с CJK символами");
        String testText3 = "Программа сжатия 㐀 текста. Символ 㐁 уже есть в тексте.";
        runTest(testText3, 2);
    }
    
    private static void runTest(String text, int minFreq) {
        System.out.println("Исходный текст:");
        System.out.println(text);
        System.out.println("Длина: " + text.length() + " символов\n");
        
        SaberWCompressor compressor = new SaberWCompressor();
        CompressionResult result = compressor.compress(text, minFreq, 1, 10);
        
        System.out.println("Сжатый текст:");
        String compressed = result.getCompressedText();
        System.out.println(compressed);
        System.out.println();
        
        System.out.println("Статистика:");
        System.out.println(result.getStats());
        System.out.println();
        
        System.out.println("Словарь:");
        List<DictionaryEntry> dictionary = result.getDictionary();
        for (DictionaryEntry entry : dictionary) {
            System.out.println(entry);
        }
        System.out.println();
        
        // Проверяем наличие группового кодирования
        boolean hasGroups = compressed.contains("●");
        System.out.println("Групповое кодирование обнаружено: " + (hasGroups ? "ДА ✓" : "НЕТ ✗"));
        
        // Декомпрессия
        String decompressed = compressor.decompress(compressed, dictionary);
        
        System.out.println("\nДекомпрессированный текст:");
        System.out.println(decompressed);
        
        // Проверка
        boolean success = text.equals(decompressed);
        System.out.println("\nТест пройден: " + (success ? "ДА ✓" : "НЕТ ✗"));
        
        if (!success) {
            System.out.println("\nРазличия:");
            for (int i = 0; i < Math.min(text.length(), decompressed.length()); i++) {
                if (text.charAt(i) != decompressed.charAt(i)) {
                    System.out.printf("  Позиция %d: оригинал '%c' (0x%04X) -> результат '%c' (0x%04X)%n",
                        i, text.charAt(i), (int)text.charAt(i),
                        decompressed.charAt(i), (int)decompressed.charAt(i));
                }
            }
        }
    }
}