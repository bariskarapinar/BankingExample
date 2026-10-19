package com.myapp.bankingexample.utils;

import java.util.Locale;

public class PinBlockCalculator {

    public static class PinBlockResult {
        public String pinBlockHex;
        public String clearPinFieldHex;
        public String panFieldHex;
        public String explanation;
    }

    public static PinBlockResult calculateIso0(String pin, String pan) {
        PinBlockResult result = new PinBlockResult();

        if (pin == null) pin = "1234";
        if (pan == null || pan.length() < 13) pan = "4543123456789012";

        // 1. PIN Field (16 hex nibbles = 8 bytes)
        // Format: '0' + Length (1 hex) + PIN digits + padded 'F's
        StringBuilder pinSb = new StringBuilder();
        pinSb.append("0").append(String.format(Locale.US, "%X", pin.length())).append(pin);
        while (pinSb.length() < 16) {
            pinSb.append("F");
        }
        result.clearPinFieldHex = pinSb.toString();

        // 2. PAN Field (16 hex nibbles = 8 bytes)
        // Format: '0000' + rightmost 12 digits of PAN excluding check digit
        String pan12 = pan.substring(pan.length() - 13, pan.length() - 1);
        result.panFieldHex = "0000" + pan12;

        // 3. XOR (PIN Field XOR PAN Field)
        byte[] pinBytes = HexUtils.hexToBytes(result.clearPinFieldHex);
        byte[] panBytes = HexUtils.hexToBytes(result.panFieldHex);
        byte[] xorBytes = new byte[8];

        for (int i = 0; i < 8; i++) {
            xorBytes[i] = (byte) (pinBytes[i] ^ panBytes[i]);
        }
        result.pinBlockHex = HexUtils.bytesToHex(xorBytes);

        StringBuilder sb = new StringBuilder();
        sb.append("--- ISO 9564-1 Format 0 (ISO-0 / ANSI X9.8) PIN Block Hesaplama ---\n");
        sb.append("Girdiler: PIN = ").append(pin).append(" | PAN = ").append(pan).append("\n\n");
        sb.append("1. Temiz PIN Alanı (16 Hex Hane):\n   ").append(result.clearPinFieldHex).append(" (0 + Boy + PIN + 'F' dolguları)\n");
        sb.append("2. PAN Alanı (16 Hex Hane):\n   ").append(result.panFieldHex).append(" (0000 + PAN sağdan 12 hane)\n");
        sb.append("3. Bitwise XOR İşlemi (1. XOR 2.):\n   ").append(result.clearPinFieldHex).append("\n");
        sb.append(" X ").append(result.panFieldHex).append("\n");
        sb.append(" = ").append(result.pinBlockHex).append(" (8 Bayt Şifrelenecek PIN Bloğu)\n");

        result.explanation = sb.toString();
        return result;
    }

    public static PinBlockResult calculateIso1(String pin) {
        PinBlockResult result = new PinBlockResult();
        if (pin == null) pin = "1234";

        // Format 1: '1' + Length (1 hex) + PIN digits + Random hex digits
        StringBuilder sb = new StringBuilder();
        sb.append("1").append(String.format(Locale.US, "%X", pin.length())).append(pin);
        String randomPad = "A1B2C3D4E5F67890";
        sb.append(randomPad.substring(0, 16 - sb.length()));

        result.pinBlockHex = sb.toString();
        result.clearPinFieldHex = result.pinBlockHex;
        result.panFieldHex = "Uygulanmaz (Format 1 PAN içermez)";

        result.explanation = "--- ISO 9564-1 Format 1 PIN Block ---\n" +
                "Format: 1 + Boy + PIN + Rastgele Doldurma\n" +
                "Üretilen PIN Bloğu: " + result.pinBlockHex + "\n";

        return result;
    }
}
