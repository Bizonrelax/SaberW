import com.saberw.core.SaberWCompressor;
import com.saberw.model.CompressionResult;
import com.saberw.model.DictionaryEntry;
import com.saberw.util.Benchmark;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== SaberW Compressor Test (Optimized Phrase Search) ===\n");
        
        // Тест производительности
        Benchmark.testWithLargeText();
        
        System.out.println("\n" + "=".repeat(50) + "\n");
        
        // Тестовый текст
        String testText = """
            Программа SaberW предназначена для сжатия текста.
            Сжатие текста позволяет экономить место в контекстном окне.
            Контекстное окно ограничено, поэтому сжатие текста очень важно.
            Программа SaberW анализирует текст и находит повторяющиеся слова.
            В этой фазе мы добавили сжатие фраз. Сжатие фраз работает отлично!
            Сжатие текста и сжатие фраз - это мощная комбинация.
            Оптимизация поиска фраз ускорила работу программы в несколько раз.
            Теперь программа может обрабатывать большие тексты быстро.
            Большие тексты сжимаются быстро благодаря оптимизации.
            """;
        
        System.out.println("Исходный текст (сокращенный вывод):");
        System.out.println(testText.substring(0, Math.min(200, testText.length())) + "...");
        System.out.println("Длина: " + testText.length() + " символов\n");
        
        // Сжатие с оптимизированным поиском фраз
        SaberWCompressor compressor = new SaberWCompressor();
        
        long startTime = System.currentTimeMillis();
        CompressionResult result = compressor.compress(testText, 2, 5, 7);
        long compressTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Время сжатия: " + compressTime + " мс");
        System.out.println();
        
        System.out.println("Сжатый текст (первые 300 символов):");
        String compressed = result.getCompressedText();
        System.out.println(compressed.substring(0, Math.min(300, compressed.length())) + "...");
        System.out.println();
        
        System.out.println("Статистика:");
        System.out.println(result.getStats());
        System.out.println();
        
        System.out.println("Словарь (первые 10 записей):");
        List<DictionaryEntry> dictionary = result.getDictionary();
        int limit = Math.min(10, dictionary.size());
        for (int i = 0; i < limit; i++) {
            System.out.println(dictionary.get(i));
        }
        if (dictionary.size() > 10) {
            System.out.println("... и ещё " + (dictionary.size() - 10) + " записей");
        }
        System.out.println();
        
        // Декомпрессия
        startTime = System.currentTimeMillis();
        String decompressed = compressor.decompress(compressed, dictionary);
        long decompressTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Время декомпрессии: " + decompressTime + " мс");
        
        // Проверка
        boolean success = testText.equals(decompressed);
        System.out.println("\nТест пройден: " + (success ? "ДА ✓" : "НЕТ ✗"));
        
        if (!success) {
            System.out.println("Ошибка: тексты не совпадают!");
        }
    }
}