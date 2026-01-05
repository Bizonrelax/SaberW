// TestSaberW.java
import com.saberw.core.SaberWCore;
import com.saberw.model.CompressionResult;
import com.saberw.model.DictionaryEntry;

import java.util.List;

public class TestSaberW {
    public static void main(String[] args) {
        System.out.println("=== Тест исправленных классов SaberW ===\n");
        
        // Простой тест
        String text = "Программа программа программа тестирует тестирует работу работы.";
        
        System.out.println("Исходный текст: " + text);
        System.out.println("Длина: " + text.length() + " символов\n");
        
        // Используем новое ядро
        SaberWCore core = new SaberWCore(text, false, false);
        CompressionResult result = core.compress();
        
        System.out.println("Сжатый текст: " + result.getCompressedText());
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
        
        if (!text.equalsIgnoreCase(decompressed)) {
            System.out.println("\nРазличия:");
            System.out.println("Оригинал:  " + text);
            System.out.println("Результат: " + decompressed);
        }
    }
}