package com.myapp.bankingexample.utils;

import java.util.Locale;

/**
 * Utility class providing high-performance Hexadecimal, Binary, ASCII, and Byte manipulation algorithms.
 *
 * <p>Crucial for payment protocol engines where byte arrays (APDUs, BER-TLV payloads, ISO 8583 bitmaps)
 * must be converted to/from human-readable Hex strings and binary bit representations.</p>
 */
public class HexUtils {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * Converts a byte array into an uppercase Hexadecimal string.
     *
     * @param bytes Byte array input
     * @return Uppercase Hex string representation
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        return bytesToHex(bytes, 0, bytes.length);
    }

    /**
     * Converts a sub-range of a byte array into an uppercase Hexadecimal string.
     *
     * @param bytes  Byte array input
     * @param offset Starting array index
     * @param length Number of bytes to convert
     * @return Uppercase Hex string representation
     */
    public static String bytesToHex(byte[] bytes, int offset, int length) {
        if (bytes == null || length <= 0 || offset < 0 || offset + length > bytes.length) {
            return "";
        }
        char[] hexChars = new char[length * 2];
        for (int j = 0; j < length; j++) {
            int v = bytes[offset + j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];      // High nibble (bits 7..4)
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F]; // Low nibble (bits 3..0)
        }
        return new String(hexChars);
    }

    /**
     * Converts a Hexadecimal string into its corresponding raw byte array.
     *
     * @param hexString Hexadecimal string (ignores whitespace or non-hex chars)
     * @return Raw byte array
     */
    public static byte[] hexToBytes(String hexString) {
        if (hexString == null) return new byte[0];
        // Clean any accidental whitespace or non-hex characters
        String cleaned = hexString.replaceAll("[^0-9A-Fa-f]", "");
        // Pad with leading zero if odd length
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

    /**
     * Generates a formatted Hex Dump string with offset addresses and ASCII representation.
     *
     * @param bytes Raw byte array payload
     * @return Formatted multi-line hex dump
     */
    public static String formatHexDump(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        int rows = (bytes.length + 15) / 16;
        for (int r = 0; r < rows; r++) {
            sb.append(String.format(Locale.US, "%04X  ", r * 16)); // Address offset
            int start = r * 16;
            int count = Math.min(16, bytes.length - start);
            for (int i = 0; i < 16; i++) {
                if (i < count) {
                    int v = bytes[start + i] & 0xFF;
                    sb.append(String.format(Locale.US, "%02X ", v));
                } else {
                    sb.append("   "); // Padding spaces for incomplete rows
                }
                if (i == 7) sb.append(" "); // Column divider space
            }
            sb.append(" |");
            for (int i = 0; i < count; i++) {
                char c = (char) (bytes[start + i] & 0xFF);
                if (c >= 32 && c <= 126) {
                    sb.append(c); // Printable ASCII character
                } else {
                    sb.append('.'); // Non-printable character replacement
                }
            }
            sb.append("|\n");
        }
        return sb.toString();
    }

    /**
     * Converts a single byte into an 8-bit binary string (e.g. "10000000").
     *
     * @param b Single byte
     * @return 8-character binary string
     */
    public static String byteToBinaryString(byte b) {
        return String.format(Locale.US, "%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
    }

    /**
     * Converts a byte array into a space-separated 8-bit binary string.
     *
     * @param bytes Byte array
     * @return Formatted binary bit string
     */
    public static String bytesToBinaryString(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(byteToBinaryString(bytes[i]));
        }
        return sb.toString();
    }

    /**
     * Converts ASCII text into its Hexadecimal string equivalent.
     *
     * @param ascii ASCII input string
     * @return Hexadecimal string representation
     */
    public static String asciiToHex(String ascii) {
        if (ascii == null) return "";
        return bytesToHex(ascii.getBytes());
    }

    /**
     * Converts a Hexadecimal string into printable ASCII characters.
     *
     * @param hex Hexadecimal input string
     * @return Printable ASCII string
     */
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
