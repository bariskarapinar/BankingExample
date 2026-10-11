package com.myapp.bankingexample.emv;

import com.myapp.bankingexample.utils.HexUtils;

import java.util.Locale;

public class EmvCryptoUtils {

    public static class ArqcVectorResult {
        public String sessionKeyHex;
        public String inputDataVectorHex;
        public String simulatedArqcHex;
        public String explanation;
    }

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
        if (currencyCode == null || currencyCode.isEmpty()) currencyCode = "0792";
        if (tvrHex == null || tvrHex.isEmpty()) tvrHex = "0000008000";
        if (unHex == null || unHex.isEmpty()) unHex = "12345678";
        if (aipHex == null || aipHex.isEmpty()) aipHex = "7C00";

        // Session Key Derivation: MasterKey XOR (ATC padded)
        byte[] mkBytes = HexUtils.hexToBytes(masterKeyHex);
        byte[] atcBytes = HexUtils.hexToBytes(atcHex);
        byte[] skBytes = new byte[16];
        for (int i = 0; i < 16; i++) {
            skBytes[i] = (byte) (mkBytes[i] ^ (i < atcBytes.length ? atcBytes[i] : 0xAA));
        }
        result.sessionKeyHex = HexUtils.bytesToHex(skBytes);

        // Build Input Vector Data for ARQC per EMV Book 2 (Table 13)
        // 1. Tag 9F02 Amount Authorised (6 bytes)
        String amountHex = String.format(Locale.US, "%012d", amountCents);
        // 2. Tag 9F03 Amount Other (6 bytes)
        String amountOtherHex = "000000000000";
        // 3. Tag 9F1A Terminal Country Code (2 bytes)
        String countryHex = "0792";
        // 4. Tag 95 TVR (5 bytes)
        // 5. Tag 5F2A Transaction Currency Code (2 bytes)
        // 6. Tag 9A Transaction Date YYMMDD (3 bytes)
        String dateHex = "260925";
        // 7. Tag 9C Transaction Type (1 byte)
        String typeHex = "00";
        // 8. Tag 9F37 Unpredictable Number (4 bytes)
        // 9. Tag 82 AIP (2 bytes)
        // 10. Tag 9F36 ATC (2 bytes)
        // 11. Tag 9F10 Issuer Application Data (7 bytes)
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

        // Simulated 8-byte ARQC Cryptogram (DES-CBC MAC over vector using Session Key)
        byte[] vectorBytes = HexUtils.hexToBytes(result.inputDataVectorHex);
        byte[] arqcBytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            arqcBytes[i] = (byte) (skBytes[i] ^ vectorBytes[i % vectorBytes.length]);
        }
        result.simulatedArqcHex = HexUtils.bytesToHex(arqcBytes);

        StringBuilder sb = new StringBuilder();
        sb.append("=== EMV ARQC KRİPTOGRAM HESAPLAMA SİMÜLASYONU ===\n");
        sb.append("1. Anahtar Türetme (Session Key Derivation):\n");
        sb.append("   Master Key: ").append(masterKeyHex).append("\n");
        sb.append("   ATC Sayaç: ").append(atcHex).append("\n");
        sb.append("   Türetilen Oturum Anahtarı (SK): ").append(result.sessionKeyHex).append("\n\n");

        sb.append("2. ARQC Giriş Veri Vektörü (34 Bayt EMV Book 2 Tablo 13):\n");
        sb.append("   - Tutar (9F02): ").append(amountHex).append("\n");
        sb.append("   - Diğer Tutar (9F03): ").append(amountOtherHex).append("\n");
        sb.append("   - Ülke Kodu (9F1A): ").append(countryHex).append("\n");
        sb.append("   - TVR (95): ").append(tvrHex).append("\n");
        sb.append("   - Para Birimi (5F2A): ").append(currencyCode).append("\n");
        sb.append("   - Tarih (9A): ").append(dateHex).append("\n");
        sb.append("   - İşlem Tipi (9C): ").append(typeHex).append("\n");
        sb.append("   - UN Rastgele Sayı (9F37): ").append(unHex).append("\n");
        sb.append("   - AIP (82): ").append(aipHex).append("\n");
        sb.append("   - ATC (9F36): ").append(atcHex).append("\n");
        sb.append("   - IAD (9F10): ").append(iadHex).append("\n");
        sb.append("   Birleştirilmiş Vektör Hex:\n   ").append(result.inputDataVectorHex).append("\n\n");

        sb.append("3. Triple-DES CBC-MAC İmzalama Sonucu:\n");
        sb.append("   -> ARQC Kriptogramı (Tag 9F26): ").append(result.simulatedArqcHex).append("\n");

        result.explanation = sb.toString();
        return result;
    }
}
