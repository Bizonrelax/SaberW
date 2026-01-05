// SimpleTest.java
import com.saberw.core.SaberWCore;
import com.saberw.model.CompressionResult;

public class SimpleTest {
    public static void main(String[] args) {
        System.out.println("=== Простой тест SaberWCore ===\n");
        
        String text = "тест тест тестирование тестирование программа программа";
        
        SaberWCore core = new SaberWCore(text);
        CompressionResult result = core.compress();
        
        System.out.println("Исходный текст: " + text);
        System.out.println("Сжатый текст: " + result.getCompressedText());
        System.out.println("Статистика: " + result.getStats());
        
        // Декомпрессия
        String decompressed = core.decompress(result.getCompressedText(), 
                                            result.getDictionary());
        System.out.println("\nДекомпрессия успешна: " + text.equals(decompressed));
    }
}