package com.myapp.emvposexample.emv;

import com.myapp.emvposexample.utils.HexUtils;

import java.nio.charset.StandardCharsets;

/**
 * Educational Factory Engine: ISO 7816 / EMV Smart Card APDU Command Builder.
 *
 * <p>Constructs ISO 7816-4 smart card Application Protocol Data Unit (APDU) commands
 * used by POS card terminals to communicate with EMV contact chip and contactless NFC cards.</p>
 *
 * <h3>ISO 7816 APDU Command Header Architecture:</h3>
 * <ul>
 *   <li><b>CLA (Class Byte):</b> <code>0x00</code> = ISO 7816-4 Standard, <code>0x80</code> = Proprietary / EMV Specific.</li>
 *   <li><b>INS (Instruction Byte):</b>
 *     <code>0xA4</code> = SELECT, <code>0xA8</code> = GPO, <code>0xB2</code> = READ RECORD,
 *     <code>0x20</code> = VERIFY PIN, <code>0xAE</code> = GENERATE AC, <code>0xCA</code> = GET DATA.
 *   </li>
 *   <li><b>P1 &amp; P2 (Parameters 1 &amp; 2):</b> Instruction-specific qualifiers and options.</li>
 *   <li><b>Lc (Length of Command Data):</b> Byte count of data payload transmitted to the card chip.</li>
 *   <li><b>Data Payload:</b> Parameter byte stream (e.g. AID, PDOL data, PIN block).</li>
 *   <li><b>Le (Expected Length of Response Data):</b> <code>0x00</code> requests all available response bytes.</li>
 * </ul>
 *
 * @author Smart Card Engineering Group
 */
public class EmvApduFactory {

    /**
     * Builds SELECT PSE (Payment System Environment - "12PAY.SYS.DDF01") APDU for Contact Chip directory lookup.
     */
    public static ApduCommand createSelectPse() {
        byte[] pseBytes = "12PAY.SYS.DDF01".getBytes(StandardCharsets.US_ASCII);
        return new ApduCommand((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, pseBytes, 0x00, "SELECT PSE (Contact Directory 12PAY.SYS.DDF01)");
    }

    /**
     * Builds SELECT PPSE (Proximity Payment System Environment - "2PAY.SYS.DDF01") APDU for Contactless NFC directory lookup.
     */
    public static ApduCommand createSelectPpse() {
        byte[] ppseBytes = "2PAY.SYS.DDF01".getBytes(StandardCharsets.US_ASCII);
        return new ApduCommand((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, ppseBytes, 0x00, "SELECT PPSE (Contactless Directory 2PAY.SYS.DDF01)");
    }

    /**
     * Builds SELECT AID (Application Identifier) APDU command to choose a card scheme (Visa, Mastercard, Troy, Amex).
     *
     * @param aidHex  Hex string of AID (e.g. "A0000000031010" for Visa).
     * @param aidName Friendly card scheme name.
     */
    public static ApduCommand createSelectAid(String aidHex, String aidName) {
        byte[] aidBytes = HexUtils.hexToBytes(aidHex);
        return new ApduCommand((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, aidBytes, 0x00, "SELECT AID (" + aidName + " - " + aidHex + ")");
    }

    /**
     * Builds GET PROCESSING OPTIONS (GPO) APDU command to retrieve AIP (Tag 82) and AFL (Tag 94) from card chip.
     *
     * @param pdolDataBytes Terminal transaction input bytes requested by card PDOL.
     */
    public static ApduCommand createGpo(byte[] pdolDataBytes) {
        byte[] data;
        if (pdolDataBytes == null || pdolDataBytes.length == 0) {
            // Default PDOL container: Tag 83 Length 02 Value 0000
            data = new byte[]{(byte) 0x83, 0x02, 0x00, 0x00};
        } else {
            data = new byte[2 + pdolDataBytes.length];
            data[0] = (byte) 0x83; // Tag 83 payload header
            data[1] = (byte) pdolDataBytes.length;
            System.arraycopy(pdolDataBytes, 0, data, 2, pdolDataBytes.length);
        }
        return new ApduCommand((byte) 0x80, (byte) 0xA8, (byte) 0x00, (byte) 0x00, data, 0x00, "GET PROCESSING OPTIONS (GPO)");
    }

    /**
     * Builds READ RECORD APDU command to read card records guided by AFL (Application File Locator).
     *
     * @param sfi          Short File Identifier (1 to 30).
     * @param recordNumber Record index (1 to 16).
     */
    public static ApduCommand createReadRecord(int sfi, int recordNumber) {
        // P2 parameter calculation: SFI shifted 3 bits left OR'd with 0x04 (read record indicator)
        byte p2 = (byte) (((sfi & 0x1F) << 3) | 0x04);
        return new ApduCommand((byte) 0x00, (byte) 0xB2, (byte) recordNumber, p2, null, 0x00, "READ RECORD (SFI " + sfi + ", Record " + recordNumber + ")");
    }

    /**
     * Builds VERIFY PIN APDU command for offline plaintext PIN verification.
     *
     * @param pinDigits Clear PIN digits entered by cardholder on PIN Pad.
     */
    public static ApduCommand createVerifyPin(String pinDigits) {
        byte[] pinBlock = new byte[8];
        pinBlock[0] = (byte) (0x20 | (pinDigits.length() & 0x0F)); // Header: 0x20 + PIN length
        for (int i = 0; i < 7; i++) {
            pinBlock[1 + i] = (byte) 0xFF; // Fill remaining bytes with 0xFF
        }
        for (int i = 0; i < pinDigits.length(); i++) {
            int nibble = Character.digit(pinDigits.charAt(i), 10);
            int byteIndex = 1 + (i / 2);
            if (i % 2 == 0) {
                pinBlock[byteIndex] = (byte) ((nibble << 4) | 0x0F);
            } else {
                pinBlock[byteIndex] = (byte) ((pinBlock[byteIndex] & 0xF0) | (nibble & 0x0F));
            }
        }
        return new ApduCommand((byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x80, pinBlock, null, "VERIFY (Offline Plaintext PIN)");
    }

    /**
     * Builds FIRST GENERATE AC APDU command to request initial cryptogram (ARQC, TC, AAC) from card.
     *
     * @param acType    Requested Cryptogram Type (0x80 = ARQC, 0x40 = TC, 0x00 = AAC).
     * @param cdol1Data Terminal transaction parameters requested by CDOL1.
     */
    public static ApduCommand createFirstGenerateAc(byte acType, byte[] cdol1Data) {
        String acName = acType == (byte) 0x80 ? "ARQC (Online Request)" : (acType == (byte) 0x40 ? "TC (Offline Approved)" : "AAC (Offline Declined)");
        return new ApduCommand((byte) 0x80, (byte) 0xAE, acType, (byte) 0x00, cdol1Data, 0x00, "1st GENERATE AC (" + acName + ")");
    }

    /**
     * Builds SECOND GENERATE AC APDU command after online host response (contains Host Tag 91 ARPC and Tag 8A ARC).
     *
     * @param acType    Requested Final Cryptogram Type (0x40 = TC, 0x00 = AAC).
     * @param cdol2Data Online Host authentication results requested by CDOL2.
     */
    public static ApduCommand createSecondGenerateAc(byte acType, byte[] cdol2Data) {
        String acName = acType == (byte) 0x40 ? "TC (Transaction Certificate)" : "AAC (Application Authentication Cryptogram)";
        return new ApduCommand((byte) 0x80, (byte) 0xAE, acType, (byte) 0x00, cdol2Data, 0x00, "2nd GENERATE AC (" + acName + ")");
    }

    /**
     * Builds GET DATA APDU command to retrieve specific card data tags (e.g. ATC 0x9F36, PIN Try Counter 0x9F17).
     *
     * @param tagHex Hex string of tag to query.
     */
    public static ApduCommand createGetData(String tagHex) {
        byte[] tagBytes = HexUtils.hexToBytes(tagHex);
        byte p1 = tagBytes.length > 0 ? tagBytes[0] : 0x00;
        byte p2 = tagBytes.length > 1 ? tagBytes[1] : 0x00;
        return new ApduCommand((byte) 0x80, (byte) 0xCA, p1, p2, null, 0x00, "GET DATA (Tag 0x" + tagHex + ")");
    }
}
