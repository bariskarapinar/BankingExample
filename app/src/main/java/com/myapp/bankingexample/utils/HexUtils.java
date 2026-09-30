package com.myapp.bankingexample.utils;

import java.util.Locale;

public class HexUtils {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        return bytesToHex(bytes, 0, bytes.length);
    }

    public static String bytesToHex(byte[] bytes, int offset, int length) {
        if (bytes == null || length <= 0 || offset < 0 || offset + length > bytes.length) {
            return "";
        }
        char[] hexChars = new char[length * 2];
        for (int j = 0; j < length; j++) {
            int v = bytes[offset + j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hexString) {
        if (hexString == null) return new byte[0];
        String cleaned = hexString.replaceAll("[^0-9A-Fa-f]", "");
        if (cleaned.length() % 2 != 0) {
            cleaned = "0" + cleaned;
        }
        int len = cleaned.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(cleaned.charAt(i), 16) << 4)
                    + Character.digit(cleaned.charAt(i + 1), 16));
        }
        return data;
    }

    public static String formatHexDump(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        int rows = (bytes.length + 15) / 16;
        for (int r = 0; r < rows; r++) {
            sb.append(String.format(Locale.US, "%04X  ", r * 16));
            int start = r * 16;
            int count = Math.min(16, bytes.length - start);
            for (int i = 0; i < 16; i++) {
                if (i < count) {
                    int v = bytes[start + i] & 0xFF;
                    sb.append(String.format(Locale.US, "%02X ", v));
                } else {
                    sb.append("   ");
                }
                if (i == 7) sb.append(" ");
            }
            sb.append(" |");
            for (int i = 0; i < count; i++) {
                char c = (char) (bytes[start + i] & 0xFF);
                if (c >= 32 && c <= 126) {
                    sb.append(c);
                } else {
                    sb.append('.');
                }
            }
            sb.append("|\n");
        }
        return sb.toString();
    }

    public static String byteToBinaryString(byte b) {
        return String.format(Locale.US, "%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }

    public static String bytesToBinaryString(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(byteToBinaryString(bytes[i]));
        }
        return sb.toString();
    }

    public static String asciiToHex(String ascii) {
        if (ascii == null) return "";
        return bytesToHex(ascii.getBytes());
    }

    public static String hexToAscii(String hex) {
        byte[] bytes = hexToBytes(hex);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            char c = (char) (b & 0xFF);
            if (c >= 32 && c <= 126) {
                sb.append(c);
            } else {
                sb.append('.');
            }
        }
        return sb.toString();
    }
}
