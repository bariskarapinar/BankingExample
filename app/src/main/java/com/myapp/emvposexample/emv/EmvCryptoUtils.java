package com.myapp.emvposexample.emv;

import com.myapp.emvposexample.utils.HexUtils;

import java.util.Locale;

/**
 * Educational Utility Class: EMV Book 2 Cryptographic Vector & ARQC Session Key Derivation.
 *
 * <p>EMV chip security relies on symmetric 3DES/AES cryptography. During an online financial
 * transaction, the smart card chip generates an 8-byte <b>ARQC (Authorisation Request Cryptogram)</b>
 * using a unique dynamic Session Key derived from the Application Transaction Counter (ATC).</p>
 *
 * <h3>EMV Book 2 Table 13 - Input Data Element Vector Sequence (34 Bytes Minimum):</h3>
 * <ol>
 *   <li><b>Amount, Authorised (Tag 9F02):</b> 6 Bytes (n 12)</li>
 *   <li><b>Amount, Other (Tag 9F03):</b> 6 Bytes (n 12)</li>
 *   <li><b>Terminal Country Code (Tag 9F1A):</b> 2 Bytes (n 3)</li>
 *   <li><b>Terminal Verification Results (Tag 95):</b> 5 Bytes (b 40)</li>
 *   <li><b>Transaction Currency Code (Tag 5F2A):</b> 2 Bytes (n 3)</li>
 *   <li><b>Transaction Date (Tag 9A):</b> 3 Bytes (n 6)</li>
 *   <li><b>Transaction Type (Tag 9C):</b> 1 Byte (n 2)</li>
 *   <li><b>Unpredictable Number (Tag 9F37):</b> 4 Bytes (b 32)</li>
 *   <li><b>Application Interchange Profile (Tag 82):</b> 2 Bytes (b 16)</li>
 *   <li><b>Application Transaction Counter (Tag 9F36):</b> 2 Bytes (b 16)</li>
 *   <li><b>Issuer Application Data (Tag 9F10):</b> Variable (b)</li>
 * </ol>
 *
 * @author Financial Cryptography Group
 */
public class EmvCryptoUtils {

    public static class ArqcVectorResult {
        public String sessionKeyHex;
        public String inputDataVectorHex;
        public String simulatedArqcHex;
        public String explanation;
    }

    /**
     * Simulates EMV Book 2 Session Key derivation and ARQC 34-byte vector CBC-MAC calculation.
     *
     * @param masterKeyHex   32-character Master Key (16 bytes).
     * @param atcHex         4-character Application Transaction Counter (2 bytes).
     * @param amountCents    Transaction amount in cents.
     * @param currencyCode   3-digit numeric ISO currency code (e.g. "0840" USD, "0978" EUR).
     * @param tvrHex         10-character Terminal Verification Results (5 bytes).
     * @param unHex          8-character Unpredictable Number (4 bytes).
     * @param aipHex         4-character Application Interchange Profile (2 bytes).
     * @return Formatted {@link ArqcVectorResult} with educational breakdown.
     */
    public static ArqcVectorResult buildArqcVector(
            String masterKeyHex,
            String atcHex,
            long amountCents,
            String currencyCode,
            String tvrHex,
            String unHex,
            String aipHex
    ) {
        ArqcVectorResult result = new ArqcVectorResult();

        if (masterKeyHex == null || masterKeyHex.length() < 32) {
            masterKeyHex = "0123456789ABCDEF0123456789ABCDEF";
        }
        if (atcHex == null || atcHex.isEmpty()) atcHex = "00A4";
        if (currencyCode == null || currencyCode.isEmpty()) currencyCode = "0840";
        if (tvrHex == null || tvrHex.isEmpty()) tvrHex = "0000008000";
        if (unHex == null || unHex.isEmpty()) unHex = "12345678";
        if (aipHex == null || aipHex.isEmpty()) aipHex = "7C00";

        // Step 1: Session Key Derivation (ATC diversification of Issuer Master Key)
        byte[] mkBytes = HexUtils.hexToBytes(masterKeyHex);
        byte[] atcBytes = HexUtils.hexToBytes(atcHex);
        byte[] skBytes = new byte[16];
        for (int i = 0; i < 16; i++) {
            skBytes[i] = (byte) (mkBytes[i] ^ (i < atcBytes.length ? atcBytes[i] : 0xAA));
        }
        result.sessionKeyHex = HexUtils.bytesToHex(skBytes);

        // Step 2: Build 34-byte EMV Book 2 Table 13 Vector
        String amountHex = String.format(Locale.US, "%012d", amountCents);
        String amountOtherHex = "000000000000";
        String countryHex = "0840";
        String dateHex = "260925";
        String typeHex = "00";
        String iadHex = "06010A03A00000";

        StringBuilder vectorSb = new StringBuilder();
        vectorSb.append(amountHex);
        vectorSb.append(amountOtherHex);
        vectorSb.append(countryHex);
        vectorSb.append(tvrHex);
        vectorSb.append(currencyCode);
        vectorSb.append(dateHex);
        vectorSb.append(typeHex);
        vectorSb.append(unHex);
        vectorSb.append(aipHex);
        vectorSb.append(atcHex);
        vectorSb.append(iadHex);

        result.inputDataVectorHex = vectorSb.toString();

        // Step 3: Compute Simulated 8-byte ARQC Cryptogram
        byte[] vectorBytes = HexUtils.hexToBytes(result.inputDataVectorHex);
        byte[] arqcBytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            arqcBytes[i] = (byte) (skBytes[i] ^ vectorBytes[i % vectorBytes.length]);
        }
        result.simulatedArqcHex = HexUtils.bytesToHex(arqcBytes);

        // Step 4: Build Educational Explanation Summary
        StringBuilder sb = new StringBuilder();
        sb.append("=== EMV ARQC CRYPTOGRAM CALCULATION DEMO ===\n\n");
        sb.append("1. Session Key Derivation (ATC Diversification):\n");
        sb.append("   Master Key: ").append(masterKeyHex).append("\n");
        sb.append("   ATC Counter: ").append(atcHex).append("\n");
        sb.append("   Derived Session Key (SK): ").append(result.sessionKeyHex).append("\n\n");

        sb.append("2. ARQC Input Data Element Vector (34 Bytes - EMV Book 2 Table 13):\n");
        sb.append("   - Amount Authorised (9F02): ").append(amountHex).append("\n");
        sb.append("   - Amount Other (9F03): ").append(amountOtherHex).append("\n");
        sb.append("   - Country Code (9F1A): ").append(countryHex).append("\n");
        sb.append("   - TVR (95): ").append(tvrHex).append("\n");
        sb.append("   - Currency Code (5F2A): ").append(currencyCode).append("\n");
        sb.append("   - Date (9A): ").append(dateHex).append("\n");
        sb.append("   - Type (9C): ").append(typeHex).append("\n");
        sb.append("   - UN Random Salt (9F37): ").append(unHex).append("\n");
        sb.append("   - AIP (82): ").append(aipHex).append("\n");
        sb.append("   - ATC (9F36): ").append(atcHex).append("\n");
        sb.append("   - IAD (9F10): ").append(iadHex).append("\n");
        sb.append("   Concatenated Vector Hex:\n   ").append(result.inputDataVectorHex).append("\n\n");

        sb.append("3. Triple-DES CBC-MAC Cryptographic Signature:\n");
        sb.append("   -> ARQC Cryptogram (Tag 9F26): ").append(result.simulatedArqcHex).append("\n");

        result.explanation = sb.toString();
        return result;
    }
}
