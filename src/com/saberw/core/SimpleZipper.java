package com.saberw.core;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public class SimpleZipper {
    
    public static String compress(String text) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos)) {
            dos.write(text.getBytes("UTF-8"));
        }
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }
    
    public static String decompress(String compressed) throws Exception {
        byte[] data = Base64.getDecoder().decode(compressed);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (InflaterOutputStream ios = new InflaterOutputStream(baos)) {
            ios.write(data);
        }
        return baos.toString("UTF-8");
    }
}