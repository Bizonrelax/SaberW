package com.saberw.core.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.saberw.core.SaberWCore;
import com.saberw.core.SimpleZipper;
import com.saberw.model.CompressionResult;
import com.saberw.model.DictionaryEntry;

public class SaberWGUI extends JFrame {
    
    private JTextArea inputTextArea;
    private JTextArea outputTextArea;
    private JTextArea dictionaryTextArea;
    private JLabel statsLabel;
    
    // Настройки
    private JCheckBox normalizeCaseCheckbox;
    private JCheckBox aggressiveCompressionCheckbox;
    private JSpinner minWordLengthSpinner;
    private JSpinner minFrequencySpinner;
    private JSpinner minFrequency2CharsSpinner;
    private JSpinner fontSizeSpinner;
    private JComboBox<String> compressionModeCombo;
    
    public SaberWGUI() {
        super("SaberW Компрессор текста v1.1");
        initializeUI();
    }
    
    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Панель ввода
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Исходный текст"));
        inputTextArea = new JTextArea(10, 60);
        inputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        inputPanel.add(new JScrollPane(inputTextArea), BorderLayout.CENTER);
        
        // Панель настроек
        JPanel settingsPanel = createSettingsPanel();
        
        // Панель кнопок
        JPanel buttonPanel = createButtonPanel();
        
        // Панель вывода
        JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.setBorder(BorderFactory.createTitledBorder("Сжатый текст"));
        outputTextArea = new JTextArea(10, 60);
        outputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputTextArea.setEditable(false);
        outputPanel.add(new JScrollPane(outputTextArea), BorderLayout.CENTER);
        
        // Панель статистики
        statsLabel = new JLabel("Готов к сжатию...");
        statsLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Панель словаря
        JPanel dictionaryPanel = new JPanel(new BorderLayout());
        dictionaryPanel.setBorder(BorderFactory.createTitledBorder("Словарь (код=слово)"));
        dictionaryTextArea = new JTextArea(8, 60);
        dictionaryTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        dictionaryTextArea.setEditable(false);
        dictionaryPanel.add(new JScrollPane(dictionaryTextArea), BorderLayout.CENTER);
        
     // Жёлтая зона справки
        JPanel helpPanel = new JPanel();
        helpPanel.setBackground(new Color(255, 255, 200)); // Светло-жёлтый
        helpPanel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
        helpPanel.setPreferredSize(new Dimension(40, 40));

        JLabel helpLabel = new JLabel("?");
        helpLabel.setFont(new Font("Arial", Font.BOLD, 20));
        helpLabel.setForeground(Color.ORANGE);

        // Всплывающая подсказка при наведении
        helpLabel.setToolTipText("<html><b>SaberW помощь</b><br>"
                + "• Сжатие: Ctrl+Enter<br>"
                + "• Распаковка: Ctrl+D<br>"
                + "• Очистка: Ctrl+L<br>"
                + "• Кликните для подробной справки</html>");

        // Обработчик клика для открытия справки
        helpLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showHelpWindow();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                helpPanel.setBackground(new Color(255, 255, 150)); // Более яркий при наведении
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                helpPanel.setBackground(new Color(255, 255, 200)); // Возвращаем цвет
            }
        });

        helpPanel.add(helpLabel);

        // Добавляем зону справки в правый верхний угол
        JPanel topRightPanel = new JPanel(new BorderLayout());
        topRightPanel.add(helpPanel, BorderLayout.EAST);
        
        // Собираем интерфейс
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.CENTER);
        topPanel.add(settingsPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(outputPanel, BorderLayout.NORTH);
        bottomPanel.add(dictionaryPanel, BorderLayout.CENTER);
        bottomPanel.add(statsLabel, BorderLayout.SOUTH);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(null);
    }
    
   private JPanel createSettingsPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(2, 5, 2, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    
    // Минимальная длина слова
    gbc.gridx = 0; gbc.gridy = 0;
    panel.add(new JLabel("Мин. длина слова:"), gbc);
    
    minWordLengthSpinner = new JSpinner(new SpinnerNumberModel(3, 2, 10, 1));
    gbc.gridx = 1;
    panel.add(minWordLengthSpinner, gbc);
    
    // Минимальная частота
    gbc.gridx = 2; gbc.gridy = 0;
    panel.add(new JLabel("Мин. частота:"), gbc);
    
    minFrequencySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));
    gbc.gridx = 3;
    panel.add(minFrequencySpinner, gbc);
    
    // Размер шрифта
    gbc.gridx = 4; gbc.gridy = 0;
    panel.add(new JLabel("Размер шрифта:"), gbc);
    
    fontSizeSpinner = new JSpinner(new SpinnerNumberModel(14, 8, 24, 1));
    fontSizeSpinner.addChangeListener(e -> updateFontSize());
    gbc.gridx = 5;
    panel.add(fontSizeSpinner, gbc);
    
    return panel;
}
    private JPanel createButtonPanel() {
    JPanel panel = new JPanel(new FlowLayout());
    
    JButton compressButton = new JButton("Сжать (Ctrl+Enter)");
    compressButton.addActionListener(e -> compressText());
    
    JButton decompressButton = new JButton("Распаковать (Ctrl+D)");
    decompressButton.addActionListener(e -> decompressText());
    
    JButton copyResultButton = new JButton("Копировать результат");
    copyResultButton.addActionListener(e -> copyToClipboard(false));
    
    JButton copyWithDictButton = new JButton("Копировать со словарём");
    copyWithDictButton.addActionListener(e -> copyToClipboard(true));
    
    JButton clearButton = new JButton("Очистить (Ctrl+L)");
    clearButton.addActionListener(e -> clearAll());
    
    JButton exampleButton = new JButton("Пример текста");
    exampleButton.addActionListener(e -> loadExample());
    
    JButton zipButton = new JButton("SaberW+ZIP");
    zipButton.addActionListener(e -> compressWithZip());
    
    panel.add(compressButton);
    panel.add(decompressButton);
    panel.add(copyResultButton);
    panel.add(copyWithDictButton);
    panel.add(clearButton);
    panel.add(exampleButton);
    panel.add(zipButton);
    
    return panel;
}
    
    private void updateFontSize() {
        int size = (int) fontSizeSpinner.getValue();
        Font newFont = new Font("Monospaced", Font.PLAIN, size);
        
        inputTextArea.setFont(newFont);
        outputTextArea.setFont(newFont);
        dictionaryTextArea.setFont(newFont);
    }
    
    private void compressText() {
        String inputText = inputTextArea.getText();
        
        if (inputText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите текст для сжатия");
            return;
        }
        
        try {
            // ВСЕГДА используем максимальное сжатие CJK
            SaberWCore core = new SaberWCore(inputText, false, false);
            
            int minLength = (int) minWordLengthSpinner.getValue();
            int minFreq = (int) minFrequencySpinner.getValue();
            
            // Фиксированные настройки для максимального сжатия
            CompressionResult result = core.compress(minLength, minFreq, 6, 5, 7);
            
            // Отображаем результаты
            outputTextArea.setText(result.getCompressedText());
            displayDictionary(result.getDictionary());
            displayStatistics(result.getStats());
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Ошибка при сжатии: " + ex.getMessage(),
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
   
    
    private void decompressText() {
        String compressedText = outputTextArea.getText();
        String dictionaryText = dictionaryTextArea.getText();
        
        if (compressedText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Нет сжатого текста для распаковки");
            return;
        }
        
        try {
            // Парсим словарь из текстового представления
            List<DictionaryEntry> dictionary = parseDictionary(dictionaryText);
            
            // Создаем ядро (для декомпрессии параметры не важны)
            SaberWCore core = new SaberWCore("", false, false);
            String decompressed = core.decompress(compressedText, dictionary);
            
            inputTextArea.setText(decompressed);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Ошибка при распаковке: " + ex.getMessage(),
                "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private List<DictionaryEntry> parseDictionary(String dictText) {
        // Простой парсинг словаря из текста
        List<DictionaryEntry> dictionary = new java.util.ArrayList<>();
        
        String[] lines = dictText.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    // Убираем частоту из строки если есть
                    String word = parts[1].trim();
                    if (word.contains(" ")) {
                        word = word.substring(0, word.indexOf(" "));
                    }
                    dictionary.add(new DictionaryEntry(word, parts[0].trim(), 0));
                }
            }
        }
        
        return dictionary;
    }
    
    private void displayDictionary(List<DictionaryEntry> dictionary) {
        StringBuilder sb = new StringBuilder();
        for (DictionaryEntry entry : dictionary) {
            sb.append(entry.getCode())
              .append("=")
              .append(entry.getWord())
              .append("\n");
        }
        dictionaryTextArea.setText(sb.toString());
    }
    
    private void displayStatistics(CompressionResult.Statistics stats) {
        String statsText = String.format(
            "Исходно: %d символов | Сжато: %d символов | Словарь: %d записей | " +
            "Экономия: %.1f%% | Коэффициент: %.2f",
            stats.getOriginalLength(),
            stats.getCompressedLength(),
            stats.getDictionaryEntries(),
            (1 - stats.getCompressionRatio()) * 100,
            stats.getCompressionRatio()
        );
        statsLabel.setText(statsText);
    }
    
    private void copyToClipboard(boolean withDictionary) {
        String textToCopy;
        
        if (withDictionary) {
            // Копируем сжатый текст + словарь
            StringBuilder sb = new StringBuilder();
            sb.append("=== Сжатый текст ===\n");
            sb.append(outputTextArea.getText());
            sb.append("\n\n=== Словарь ===\n");
            sb.append(dictionaryTextArea.getText());
            textToCopy = sb.toString();
        } else {
            // Копируем только сжатый текст
            textToCopy = outputTextArea.getText();
        }
        
        if (!textToCopy.trim().isEmpty()) {
            StringSelection selection = new StringSelection(textToCopy);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
            JOptionPane.showMessageDialog(this, "Текст скопирован в буфер обмена");
        }
    }
    
    private void clearAll() {
        inputTextArea.setText("");
        outputTextArea.setText("");
        dictionaryTextArea.setText("");
        statsLabel.setText("Готов к сжатию...");
    }
    
    private void loadExample() {
        String example = """
            Программа SaberW предназначена для сжатия текста.
            Сжатие текста позволяет экономить место в контекстном окне.
            Контекстное окно ограничено, поэтому сжатие текста очень важно.
            Программа SaberW анализирует текст и находит повторяющиеся слова.
            В этой фазе мы добавили сжатие фраз. Сжатие фраз работает отлично!
            Сжатие текста и сжатие фраз - это мощная комбинация.
            
            Пример 2-символьных слов:
            Да, не за что. Да, не беспокойся. Да, не переживай.
            За это да, не то чтобы да, за всё да, не только да.
            
            Пример для чата:
            Привет! Как дела? Что нового? 
            Расскажи о своих успехах в программировании.
            """;
        inputTextArea.setText(example);
    }
    
    public static void main(String[] args) {
        // Устанавливаем Look and Feel для более современного вида
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            SaberWGUI gui = new SaberWGUI();
            gui.setVisible(true);
        });
    }
    private void compressWithZip() {
        String inputText = inputTextArea.getText();
        if (inputText.trim().isEmpty()) return;
        
        try {
            // 1. Сначала SaberW
            SaberWCore core = new SaberWCore(inputText, false, true);
            CompressionResult saberResult = core.compress();
            String saberCompressed = saberResult.getCompressedText();
            
            // 2. Потом ZIP
            String zipCompressed = SimpleZipper.compress(saberCompressed);
            
            // 3. Формируем итоговое сообщение
            String finalMessage = "=== ДВОЙНОЕ СЖАТИЕ (SaberW + ZIP) ===\n" +
                                 "ZIP-сжатый текст (Base64):\n" +
                                 zipCompressed + "\n\n" +
                                 "Словарь SaberW:\n";
            
            for (DictionaryEntry entry : saberResult.getDictionary()) {
                finalMessage += entry.getCode() + "=" + entry.getWord() + " ";
            }
            
            outputTextArea.setText(finalMessage);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
        }
    }
 // Класс для сохранения/загрузки словарей
    private void saveDictionary(String name, List<DictionaryEntry> dictionary) {
        try (PrintWriter out = new PrintWriter("dict_" + name + ".txt")) {
            for (DictionaryEntry entry : dictionary) {
                out.println(entry.getCode() + "=" + entry.getWord());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<DictionaryEntry> loadDictionary(String filename) {
        List<DictionaryEntry> dict = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("=")) {
                    String[] parts = line.split("=", 2);
                    dict.add(new DictionaryEntry(parts[1].trim(), parts[0].trim(), 0));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dict;
    }

    // Кнопки в GUI:
    // "Сохранить словарь" и "Загрузить словарь"
   
    
    
    public static List<DictionaryEntry> mergeDictionaries(
    	    List<DictionaryEntry> dict1, 
    	    List<DictionaryEntry> dict2) {
    	    
    	    Map<String, DictionaryEntry> merged = new HashMap<>();
    	    
    	    // Сначала добавляем все из первого
    	    for (DictionaryEntry entry : dict1) {
    	        merged.put(entry.getWord(), entry);
    	    }
    	    
    	    // Добавляем из второго, избегая конфликтов
    	    for (DictionaryEntry entry : dict2) {
    	        if (!merged.containsKey(entry.getWord())) {
    	            merged.put(entry.getWord(), entry);
    	        }
    	    }
    	    
    	    return new ArrayList<>(merged.values());
    	}
    public static List<DictionaryEntry> optimizeDictionary(
    	    List<DictionaryEntry> dictionary, 
    	    String text) {
    	    
    	    // Удаляем слова, которых нет в тексте
    	    return dictionary.stream()
    	        .filter(entry -> text.contains(entry.getWord()))
    	        .collect(Collectors.toList());
    	}
    public static String exportForBot(List<DictionaryEntry> dictionary, 
            String compressedText) {
StringBuilder sb = new StringBuilder();

// Словарь без частот
for (DictionaryEntry entry : dictionary) {
sb.append(entry.getCode())
.append("=")
.append(entry.getWord())
.append("\n");
}

sb.append("___\n")
.append(compressedText);

return sb.toString();
}
    private void addTooltips() {
        compressionModeCombo.setToolTipText(
            "Максимальное сжатие: заменяет слова на CJK символы\n" +
            "Быстрая переписка: баланс сжатия и читаемости\n" +
            "Программный код: сохраняет регистр и структуру\n" +
            "Только длинные слова: для текстов с редкими длинными словами"
        );
        
        normalizeCaseCheckbox.setToolTipText(
            "Приводит текст к нижнему регистру перед сжатием\n" +
            "Увеличивает эффективность сжатия на 5-15%\n" +
            "Не используйте для программного кода!"
        );
        
        aggressiveCompressionCheckbox.setToolTipText(
            "Агрессивное сжатие: кодирует слова от 2 символов\n" +
            "Требует высокой частоты повторений\n" +
            "Может ухудшить читаемость"
        );
        
        minWordLengthSpinner.setToolTipText("Минимальная длина слова для кодирования (2-10 символов)");
        minFrequencySpinner.setToolTipText("Минимальная частота повторения слова (1-20 раз)");
        minFrequency2CharsSpinner.setToolTipText("Для 2-символьных слов нужна большая частота (3-20 раз)");
        fontSizeSpinner.setToolTipText("Размер шрифта в текстовых полях (8-24 пункта)");
    }
    private void autoConfigure() {
        String text = inputTextArea.getText();
        
        if (text == null || text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите текст для автонастройки");
            return;
        }
        
        if (text.length() < 100) {
            compressionModeCombo.setSelectedIndex(2); // Быстрая переписка
            minWordLengthSpinner.setValue(3);
            if (normalizeCaseCheckbox != null) normalizeCaseCheckbox.setSelected(true);
        } 
        else if (com.saberw.util.ValidationHelper.isLikelySourceCode(text)) {
            compressionModeCombo.setSelectedIndex(3); // Программный код
            minWordLengthSpinner.setValue(4);
            if (normalizeCaseCheckbox != null) normalizeCaseCheckbox.setSelected(false);
            if (aggressiveCompressionCheckbox != null) aggressiveCompressionCheckbox.setSelected(true);
        }
        else {
            compressionModeCombo.setSelectedIndex(0); // Максимальное сжатие
            minWordLengthSpinner.setValue(3);
            if (normalizeCaseCheckbox != null) normalizeCaseCheckbox.setSelected(true);
        }
        
        JOptionPane.showMessageDialog(this, 
            "Настройки подобраны автоматически!\n" +
            "Длина текста: " + text.length() + " символов\n" +
            "Режим: " + compressionModeCombo.getSelectedItem());
    }

    private void copyDictionaryOnly() {
        String dictText = dictionaryTextArea.getText();
        if (dictText == null || dictText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Словарь пуст");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        String[] lines = dictText.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                String word = parts[1].trim();
                if (word.contains("(")) {
                    word = word.substring(0, word.indexOf("(")).trim();
                }
                sb.append(parts[0].trim())
                  .append("=")
                  .append(word)
                  .append("\n");
            }
        }
        
        if (sb.length() > 0) {
            StringSelection selection = new StringSelection(sb.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
            JOptionPane.showMessageDialog(this, "Словарь скопирован (без частот)");
        }
    }

    private void copyForBot() {
        String compressedText = outputTextArea.getText();
        String dictText = dictionaryTextArea.getText();
        
        if (compressedText.trim().isEmpty() || dictText.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Сначала сожми текст");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("СЛОВАРИК: символ=слово\n");
        
        // Обрабатываем словарь
        String[] lines = dictText.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.contains("=")) {
                String[] parts = line.split("=", 2);
                String word = parts[1].trim();
                if (word.contains("(")) {
                    word = word.substring(0, word.indexOf("(")).trim();
                }
                sb.append(parts[0].trim())
                  .append("=")
                  .append(word)
                  .append("\n");
            }
        }
        
        sb.append("___\n")
          .append(compressedText);
        
        StringSelection selection = new StringSelection(sb.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
        
        JOptionPane.showMessageDialog(this, 
            "Текст готов для отправки боту!\n" +
            "Словарь + разделитель + сжатый текст\n" +
            "Скопировано в буфер обмена");
    }
    private void showHelpWindow() {
        JFrame helpFrame = new JFrame("Справка SaberW");
        helpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JTextPane helpText = new JTextPane();
        helpText.setContentType("text/html");
        helpText.setEditable(false);
        helpText.setText("<html><body style='padding: 20px; font-family: Arial;'>"
                + "<h2 style='color: #336699;'>SaberW - сжатие текста</h2>"
                + "<h3>Основное использование:</h3>"
                + "<ol>"
                + "<li>Введите текст в поле <b>Исходный текст</b></li>"
                + "<li>Нажмите <b>Сжать</b> (Ctrl+Enter)</li>"
                + "<li>Сжатый текст появится ниже</li>"
                + "<li>Словарь создается автоматически</li>"
                + "</ol>"
                
                + "<h3>Настройки:</h3>"
                + "<ul>"
                + "<li><b>Мин. длина слова</b> - минимальная длина слов для сжатия</li>"
                + "<li><b>Мин. частота</b> - сколько раз слово должно повториться</li>"
                + "<li><b>Размер шрифта</b> - изменяет шрифт в текстовых полях</li>"
                + "</ul>"
                
                + "<h3>Советы:</h3>"
                + "<ul>"
                + "<li>Для длинных текстов сжатие эффективнее</li>"
                + "<li>Минимум 3 символа и 2 повторения - хорошая настройка</li>"
                + "<li>Для кода используйте длину слова 4-5 символов</li>"
                + "</ul>"
                
                + "<h3>Экспорт:</h3>"
                + "<p>Используйте <b>Копировать со словарём</b> чтобы отправить<br>"
                + "сжатый текст вместе со словарем для декодирования.</p>"
                
                + "<hr>"
                + "<p style='color: #666; font-size: 12px;'>"
                + "SaberW v1.1 | Максимальное сжатие CJK<br>"
                + "Символы замены: 㐀-㑣 (CJK Extension A)"
                + "</p>"
                + "</body></html>");
        
        JScrollPane scrollPane = new JScrollPane(helpText);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        helpFrame.add(scrollPane);
        helpFrame.pack();
        helpFrame.setLocationRelativeTo(this);
        helpFrame.setVisible(true);
    }
}