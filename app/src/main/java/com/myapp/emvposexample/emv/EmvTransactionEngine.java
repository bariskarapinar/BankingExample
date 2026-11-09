package com.myapp.emvposexample.emv;

import com.myapp.emvposexample.utils.HexUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Educational Protocol Engine: EMV Book 3 & 4 Smart Card 10-Step Transaction State Machine.
 *
 * <p>Executes the EMV L2 kernel transaction lifecycle for contact chip and contactless NFC transactions.
 * This state machine governs card reader communication, APDU command dispatch, offline data authentication,
 * floor limit risk management, and cryptogram generation.</p>

 * <h3>EMV 10-Step Transaction Lifecycle:</h3>
 * <ol>
 *   <li><b>Application Selection:</b> Select card application (AID) from PSE/PPSE directory.</li>
 *   <li><b>Initiate Application Processing:</b> Issue GPO command to receive AIP (Tag 82) &amp; AFL (Tag 94).</li>
 *   <li><b>Read Application Data:</b> Read SFI card records (PAN, Expiry Date, Certificates) guided by AFL.</li>
 *   <li><b>Offline Data Authentication:</b> Verify SDA, DDA, or CDA RSA public key signatures.</li>
 *   <li><b>Processing Restrictions:</b> Check Application Expiration Date and AUC restrictions.</li>
 *   <li><b>Cardholder Verification (CVM):</b> Process CVM rules (Online PIN, Offline PIN, CDCVM, Signature).</li>
 *   <li><b>Terminal Risk Management:</b> Evaluate Floor Limit, Random Selection, and Velocity limits.</li>
 *   <li><b>Terminal Action Analysis:</b> Evaluate TVR (Tag 95) bitmask against Terminal Action Codes (TAC).</li>
 *   <li><b>First Generate AC:</b> Issue 1st Generate AC command to request ARQC (Online) or TC (Offline).</li>
 *   <li><b>Field 55 Packaging:</b> Package chip TLV tags into ISO 8583 LLLVAR Field 55 payload.</li>
 * </ol>
 *
 * @author EMV L2 Kernel Engineering Group
 */
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

    /**
     * Runs an end-to-end simulated EMV smart card transaction lifecycle.
     *
     * @param cardScheme     Selected card scheme ("Visa", "Mastercard", "Troy", "American Express").
     * @param amountInCents  Transaction amount in cents.
     * @param entryMode      POSIX entry mode ("051" for Chip, "071" for Contactless NFC).
     * @return Formatted {@link TransactionResult} containing execution logs and Field 55 payload.
     */
    public static TransactionResult runSimulatedEmvTransaction(String cardScheme, long amountInCents, String entryMode) {
        TransactionResult result = new TransactionResult();
        List<String> log = result.logs;

        log.add("=== EMV SMART CARD TRANSACTION ENGINE INITIATED ===");
        log.add("Scheme: " + cardScheme + " | Amount: " + String.format(Locale.US, "$%.2f", amountInCents / 100.0) + " | POS Entry Mode: " + entryMode);

        // Step 1: Application Selection
        log.add("\n[STEP 1] Application Selection (PSE / PPSE Directory Read)");
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
            aidName = "Troy Global Card";
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
        log.add("\n[STEP 2] Initiate Application Processing (GPO Command)");
        ApduCommand gpo = EmvApduFactory.createGpo(null);
        log.add("-> APDU TX: " + gpo.toHex() + " (" + gpo.getDescription() + ")");
        result.aipHex = "7C00";
        String aflHex = "08010100";
        log.add("<- APDU RX: 770E8202" + result.aipHex + "9404" + aflHex + "9000 (SW1 SW2: 9000 OK)");
        log.add("   [AIP - Tag 82]: " + result.aipHex + " (SDA & DDA Supported)");
        log.add("   [AFL - Tag 94]: " + aflHex + " (Read SFI 1, Record 1)");

        // Step 3: Read Application Data
        log.add("\n[STEP 3] Read Application Data (SFI Record Processing)");
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
        result.expiryDate = "281231";

        String track2Hex = result.pan + "D" + result.expiryDate + "201000000000";
        if (track2Hex.length() % 2 != 0) track2Hex += "F";
        int track2Len = track2Hex.length() / 2;

        log.add("<- APDU RX: 702B57" + String.format(Locale.US, "%02X", track2Len) + track2Hex + "5F2403" + result.expiryDate + "5A08" + result.pan.substring(0, Math.min(16, result.pan.length())) + "9000");
        log.add("   [PAN - Tag 5A]: " + result.pan);
        log.add("   [Expiry Date - Tag 5F24]: " + result.expiryDate);

        // Step 4: Offline Data Authentication
        log.add("\n[STEP 4] Offline Data Authentication (fDDA / CDA Verification)");
        log.add("-> Dynamic RSA Signature verified using Card Chip Public Key (DDA SUCCESS).");

        // Step 5: Processing Restrictions & CVM Selection
        log.add("\n[STEP 5] Processing Restrictions & Cardholder Verification (CVM)");
        log.add("-> Application Effective & Expiration Date Validated: PASS (" + result.expiryDate + ")");
        log.add("-> Evaluated CVM List: Online PIN / CDCVM / No CVM");
        log.add("   [CVM Results - Tag 9F34]: 020002 (Online PIN Processed & Successful)");

        // Step 6: Terminal Risk Management & TVR Evaluation
        log.add("\n[STEP 6] Terminal Risk Management & TVR Evaluation");
        result.tvrHex = "0000008000";
        log.add("   [TVR - Tag 95]: " + result.tvrHex + " (Floor Limit Exceeded -> Requesting Online Authorization)");

        // Step 7: First Generate AC (ARQC Generation)
        log.add("\n[STEP 7] First Generate AC (Cryptogram Generation -> ARQC Request)");
        byte[] cdol1 = new byte[29];
        ApduCommand genAc1 = EmvApduFactory.createFirstGenerateAc((byte) 0x80, cdol1);
        log.add("-> APDU TX: " + genAc1.toHex() + " (" + genAc1.getDescription() + ")");

        result.atcHex = "00A4";
        result.arqcHex = "A1B2C3D4E5F67890";
        String iadHex = "06010A03A00000";
        String unHex = "12345678";

        log.add("<- APDU RX: 801280" + result.atcHex + result.arqcHex + iadHex + "9000 (SW1 SW2: 9000 OK)");
        log.add("   [CID - Tag 9F27]: 80 (ARQC - Online Host Request Required)");
        log.add("   [ATC - Tag 9F36]: " + result.atcHex + " (Transaction Counter: " + Integer.parseInt(result.atcHex, 16) + ")");
        log.add("   [ARQC - Tag 9F26]: " + result.arqcHex + " (Card Cryptographic Signature)");

        // Step 8: Construct Field 55 EMV Data Payload
        log.add("\n[STEP 8] Constructing ISO 8583 Field 55 ICC Data Payload");
        StringBuilder f55 = new StringBuilder();
        f55.append("9F2608").append(result.arqcHex);
        f55.append("9F270180");
        f55.append("9F1007").append(iadHex);
        f55.append("9F3704").append(unHex);
        f55.append("9F3602").append(result.atcHex);
        f55.append("9505").append(result.tvrHex);
        f55.append("9A03260925");
        f55.append("9C0100");
        f55.append("9F0206").append(String.format(Locale.US, "%012d", amountInCents));
        f55.append("5F2A020840");
        f55.append("8202").append(result.aipHex);
        f55.append("9F1A020840");

        result.field55Hex = f55.toString();
        log.add("   [Field 55 Hex (" + (result.field55Hex.length() / 2) + " Bytes)]:\n   " + result.field55Hex);

        result.isApproved = true;
        log.add("\n=== STAGE 1 EMV CARD TERMINAL KERNEL COMPLETED ===");

        return result;
    }
}
