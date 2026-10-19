package com.myapp.bankingexample.emv;

import com.myapp.bankingexample.utils.HexUtils;

import java.util.ArrayList;
import java.util.List;

public class EmvTransactionEngine {

    public static class TransactionResult {
        public String aid;
        public String cardName;
        public String pan;
        public String expiryDate;
        public String atcHex;
        public String arqcHex;
        public String tvrHex;
        public String aipHex;
        public String field55Hex;
        public boolean isApproved;
        public List<String> logs = new ArrayList<>();
    }

    public static TransactionResult runSimulatedEmvTransaction(String cardScheme, long amountInCents, String entryMode) {
        TransactionResult result = new TransactionResult();
        List<String> log = result.logs;

        log.add("=== EMV İŞLEM SİMÜLASYONU BAŞLATILDI ===");
        log.add("Kart Tipi: " + cardScheme + " | Tutar: " + (amountInCents / 100.0) + " TL | POS Giriş Modu: " + entryMode);

        // Step 1: Application Selection
        log.add("\n[ADIM 1] Uygulama Seçimi (Application Selection)");
        String aidHex;
        String aidName;
        if ("Visa".equalsIgnoreCase(cardScheme)) {
            aidHex = "A0000000031010";
            aidName = "Visa Credit/Debit";
        } else if ("Mastercard".equalsIgnoreCase(cardScheme)) {
            aidHex = "A0000000041010";
            aidName = "Mastercard Credit/Debit";
        } else if ("Troy".equalsIgnoreCase(cardScheme)) {
            aidHex = "A0000000651010";
            aidName = "Troy Yerli Kart";
        } else {
            aidHex = "A0000000250000";
            aidName = "American Express";
        }
        result.aid = aidHex;
        result.cardName = aidName;

        ApduCommand selectAid = EmvApduFactory.createSelectAid(aidHex, aidName);
        log.add("-> APDU TX: " + selectAid.toHex() + " (" + selectAid.getDescription() + ")");
        log.add("<- APDU RX: 6F1E8407" + aidHex + "A513500B" + HexUtils.bytesToHex(aidName.getBytes()) + "8701019000 (SW1 SW2: 9000 OK)");

        // Step 2: Initiate Application Processing (GPO)
        log.add("\n[ADIM 2] İşlem Başlatma (Initiate Application Processing - GPO)");
        ApduCommand gpo = EmvApduFactory.createGpo(null);
        log.add("-> APDU TX: " + gpo.toHex() + " (" + gpo.getDescription() + ")");
        result.aipHex = "7C00"; // SDA, DDA, Terminal Risk Mgmt, CVM
        String aflHex = "08010100"; // SFI 1, Record 1 to 1
        log.add("<- APDU RX: 770E8202" + result.aipHex + "9404" + aflHex + "9000 (SW1 SW2: 9000 OK)");
        log.add("   [AIP]: " + result.aipHex + " (SDA & DDA Destekleniyor)");
        log.add("   [AFL]: " + aflHex + " (SFI 1, Rekor 1 oku)");

        // Step 3: Read Application Data
        log.add("\n[ADIM 3] Kart Verilerinin Okunması (Read Application Data)");
        ApduCommand readRec = EmvApduFactory.createReadRecord(1, 1);
        log.add("-> APDU TX: " + readRec.toHex() + " (" + readRec.getDescription() + ")");

        if ("Visa".equalsIgnoreCase(cardScheme)) {
            result.pan = "4543123456789012";
        } else if ("Mastercard".equalsIgnoreCase(cardScheme)) {
            result.pan = "5412751234567890";
        } else if ("Troy".equalsIgnoreCase(cardScheme)) {
            result.pan = "9792001234567890";
        } else {
            result.pan = "378282123456789";
        }
        result.expiryDate = "281231"; // YYMMDD

        String track2Hex = result.pan + "D" + result.expiryDate + "201000000000";
        if (track2Hex.length() % 2 != 0) track2Hex += "F";
        int track2Len = track2Hex.length() / 2;

        log.add("<- APDU RX: 702B57" + String.format("%02X", track2Len) + track2Hex + "5F2403" + result.expiryDate + "5A08" + result.pan.substring(0, 16) + "9000");
        log.add("   [PAN]: " + result.pan);
        log.add("   [Son Kullanma Tarihi]: " + result.expiryDate);

        // Step 4: Offline Data Authentication
        log.add("\n[ADIM 4] Çevrimdışı Veri Doğrulama (Offline Data Authentication)");
        log.add("-> fDDA / CDA İmzası Kart Çip Sertifikalarıyla Terminal Tarafından Doğrulandı (DDA SUCCESS).");

        // Step 5: Processing Restrictions & CVM Selection
        log.add("\n[ADIM 5] İşlem Kısıtlamaları & Kart Sahibi Doğrulama (CVM)");
        log.add("-> Kart Son Kullanma Tarihi Kontrol Edildi: GEÇERLİ (" + result.expiryDate + ")");
        log.add("-> CVM Listesi Değerlendirildi: Online PIN / CDCVM / No CVM");
        log.add("   [CVM Sonucu - Tag 9F34]: 020002 (Online PIN Uygulandı ve Başarılı)");

        // Step 6: Terminal Risk Management & TVR Evaluation
        log.add("\n[ADIM 6] Terminal Risk Yönetimi & TVR Değerlendirmesi");
        result.tvrHex = "0000008000"; // Bitwise TVR: Transaction exceeds floor limit, force online
        log.add("   [TVR - Tag 95]: " + result.tvrHex + " (Tutar Limit Üstü, Online Yetkilendirme İsteniyor)");

        // Step 7: First Generate AC (ARQC Generation)
        log.add("\n[ADIM 7] 1. Kriptogram Üretimi (First Generate AC -> ARQC)");
        byte[] cdol1 = new byte[29]; // Simulated CDOL1 data payload
        ApduCommand genAc1 = EmvApduFactory.createFirstGenerateAc((byte) 0x80, cdol1); // 0x80 = ARQC
        log.add("-> APDU TX: " + genAc1.toHex() + " (" + genAc1.getDescription() + ")");

        result.atcHex = "00A4";
        result.arqcHex = "A1B2C3D4E5F67890";
        String iadHex = "06010A03A00000";
        String unHex = "12345678";

        log.add("<- APDU RX: 801280" + result.atcHex + result.arqcHex + iadHex + "9000 (SW1 SW2: 9000 OK)");
        log.add("   [CID - Tag 9F27]: 80 (ARQC - Online Request)");
        log.add("   [ATC - Tag 9F36]: " + result.atcHex + " (İşlem Sayacı: " + Integer.parseInt(result.atcHex, 16) + ")");
        log.add("   [ARQC - Tag 9F26]: " + result.arqcHex + " (Çip Kriptografik İmzası)");

        // Step 8: Construct Field 55 EMV Data Payload
        log.add("\n[ADIM 8] ISO 8583 Saha 55 (Field 55) Oluşturulması");
        StringBuilder f55 = new StringBuilder();
        f55.append("9F2608").append(result.arqcHex);
        f55.append("9F270180");
        f55.append("9F1007").append(iadHex);
        f55.append("9F3704").append(unHex);
        f55.append("9F3602").append(result.atcHex);
        f55.append("9505").append(result.tvrHex);
        f55.append("9A03260925");
        f55.append("9C0100");
        f55.append("9F0206").append(String.format("%012d", amountInCents));
        f55.append("5F2A020792");
        f55.append("8202").append(result.aipHex);
        f55.append("9F1A020792");

        result.field55Hex = f55.toString();
        log.add("   [Field 55 Hex (" + (result.field55Hex.length() / 2) + " Bayt)]:\n   " + result.field55Hex);

        result.isApproved = true;
        log.add("\n=== EMV KART TARAFINDAKİ İLK AŞAMA TAMAMLAMDI ===");

        return result;
    }
}
