package com.saberw.core;

import com.saberw.model.CompressionResult;

public class HumanReadableCompressor {
    
    public static CompressionResult compressForAI(String text) {
        // Пока возвращаем простое сжатие
        SaberWCore core = new SaberWCore(text, true, false);
        return core.compress(8, 2, 999, 5, 15); // Только длинные слова
    }
    
    public static CompressionResult compressReadable(String text) {
        // Пока возвращаем стандартное сжатие
        SaberWCore core = new SaberWCore(text, true, true);
        return core.compress(3, 2, 4, 5, 7);
    }
}