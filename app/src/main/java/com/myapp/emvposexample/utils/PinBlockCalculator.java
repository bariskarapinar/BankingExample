package com.myapp.emvposexample.utils;

import java.util.Locale;

/**
 * Educational Utility Class: ISO 9564-1 Financial PIN Block Calculator.
 *
 * <p>In payment card processing, Personal Identification Numbers (PINs) are encrypted
 * before transmission across security zones (POS Terminal -> Acquirer -> Gateway -> Issuer Host).
 * ISO 9564-1 defines standard PIN block formats to safely encapsulate PIN digits.</p>
 *
 * <h3>ISO 9564-1 Format 0 (ISO-0 / ANSI X9.8):</h3>
 * <ul>
 *   <li><b>Clear PIN Field (16 Hex Digits / 8 Bytes):</b> <code>'0' + Length (1 hex digit) + PIN Digits + 'F' padding</code>.</li>
 *   <li><b>PAN Field (16 Hex Digits / 8 Bytes):</b> <code>'0000' + PAN rightmost 12 digits (excluding check digit)</code>.</li>
 *   <li><b>PIN Block:</b> <code>Clear PIN Field XOR PAN Field</code>.</li>
 * </ul>
 *
 * @author Financial Security Engineering Group
 */
public class PinBlockCalculator {

    public static class PinBlockResult {
        public String pinBlockHex;
        public String clearPinFieldHex;
        public String panFieldHex;
        public String explanation;
    }

    /**
     * Calculates an ISO 9564-1 Format 0 (ISO-0) PIN block via bitwise XOR operations.
     *
     * @param pin Clear cardholder PIN (e.g. "1234").
     * @param pan Primary Account Number (e.g. "4543123456789012").
     * @return Formatted {@link PinBlockResult} with step-by-step cryptographic breakdown.
     */
    public static PinBlockResult calculateIso0(String pin, String pan) {
        PinBlockResult result = new PinBlockResult();

        if (pin == null) pin = "1234";
        if (pan == null || pan.length() < 13) pan = "4543123456789012";

        // Step 1: Format Clear PIN Field (0 + PIN Length + PIN Digits + 'F' Fill)
        StringBuilder pinSb = new StringBuilder();
        pinSb.append("0").append(String.format(Locale.US, "%X", pin.length())).append(pin);
        while (pinSb.length() < 16) {
            pinSb.append("F");
        }
        result.clearPinFieldHex = pinSb.toString();

        // Step 2: Format PAN Field (0000 + PAN rightmost 12 digits excluding Luhn check digit)
        String pan12 = pan.substring(pan.length() - 13, pan.length() - 1);
        result.panFieldHex = "0000" + pan12;

        // Step 3: Execute Bitwise XOR (Clear PIN Field XOR PAN Field)
        byte[] pinBytes = HexUtils.hexToBytes(result.clearPinFieldHex);
        byte[] panBytes = HexUtils.hexToBytes(result.panFieldHex);
        byte[] xorBytes = new byte[8];

        for (int i = 0; i < 8; i++) {
            xorBytes[i] = (byte) (pinBytes[i] ^ panBytes[i]);
        }
        result.pinBlockHex = HexUtils.bytesToHex(xorBytes);

        // Step 4: Build Educational Step-by-Step Explanation
        StringBuilder sb = new StringBuilder();
        sb.append("--- ISO 9564-1 Format 0 (ISO-0 / ANSI X9.8) PIN Block Calculation ---\n");
        sb.append("Inputs: PIN = ").append(pin).append(" | PAN = ").append(pan).append("\n\n");
        sb.append("1. Clear PIN Field (16 Hex Digits / 8 Bytes):\n   ").append(result.clearPinFieldHex).append(" (0 + Length + PIN + 'F' padding)\n");
        sb.append("2. PAN Field (16 Hex Digits / 8 Bytes):\n   ").append(result.panFieldHex).append(" (0000 + PAN rightmost 12 digits)\n");
        sb.append("3. Bitwise XOR Operation (Clear PIN XOR PAN):\n   ").append(result.clearPinFieldHex).append("\n");
        sb.append(" X ").append(result.panFieldHex).append("\n");
        sb.append(" = ").append(result.pinBlockHex).append(" (8-Byte Payload to Encrypt with Triple-DES / AES Working Key)\n");

        result.explanation = sb.toString();
        return result;
    }

    /**
     * Calculates an ISO 9564-1 Format 1 PIN block (used when PAN is unavailable, e.g. ATM transactions).
     *
     * @param pin Clear cardholder PIN (e.g. "1234").
     * @return Formatted {@link PinBlockResult}.
     */
    public static PinBlockResult calculateIso1(String pin) {
        PinBlockResult result = new PinBlockResult();
        if (pin == null) pin = "1234";

        StringBuilder sb = new StringBuilder();
        sb.append("1").append(String.format(Locale.US, "%X", pin.length())).append(pin);
        String randomPad = "A1B2C3D4E5F67890";
        sb.append(randomPad.substring(0, 16 - sb.length()));

        result.pinBlockHex = sb.toString();
        result.clearPinFieldHex = result.pinBlockHex;
        result.panFieldHex = "N/A (Format 1 does not use PAN XOR)";

        result.explanation = "--- ISO 9564-1 Format 1 PIN Block ---\n" +
                "Format: 1 + Length + PIN + Random Transaction Padding\n" +
                "Generated PIN Block: " + result.pinBlockHex + "\n";

        return result;
    }
}
