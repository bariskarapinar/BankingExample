package com.myapp.bankingexample.emv;

import com.myapp.bankingexample.utils.HexUtils;

import java.nio.charset.StandardCharsets;

/**
 * Factory class generating standard ISO 7816-4 / EMVCo C-APDU (Command APDU) packets.
 *
 * <p>Provides pre-configured APDUs for the entire EMV 10-step transaction lifecycle:
 * <ul>
 *   <li>{@code SELECT PSE / PPSE}: Directory selection for Contact and Contactless entry points</li>
 *   <li>{@code SELECT AID}: Application Selection by AID (Visa, Mastercard, Troy, Amex)</li>
 *   <li>{@code GPO}: Initiate Application Processing with PDOL payload</li>
 *   <li>{@code READ RECORD}: Application data retrieval from SFI files</li>
 *   <li>{@code VERIFY PIN}: Offline Plaintext PIN verification</li>
 *   <li>{@code GENERATE AC}: 1st &amp; 2nd Generate Application Cryptogram requests</li>
 * </ul>
 * </p>
 */
public class EmvApduFactory {

    /**
     * Creates a {@code SELECT PSE} APDU command (12PAY.SYS.DDF01) for contact card application discovery.
     *
     * @return Formatted {@link ApduCommand}
     */
    public static ApduCommand createSelectPse() {
        byte[] pseBytes = "12PAY.SYS.DDF01".getBytes(StandardCharsets.US_ASCII);
        // CLA=00, INS=A4 (SELECT), P1=04 (Select by DF Name), P2=00 (First occurrence)
        return new ApduCommand((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, pseBytes, 0x00, "SELECT PSE (Contact Directory 12PAY.SYS.DDF01)");
    }

    /**
     * Creates a {@code SELECT PPSE} APDU command (2PAY.SYS.DDF01) for contactless card application discovery.
     *
     * @return Formatted {@link ApduCommand}
     */
    public static ApduCommand createSelectPpse() {
        byte[] ppseBytes = "2PAY.SYS.DDF01".getBytes(StandardCharsets.US_ASCII);
        // CLA=00, INS=A4 (SELECT), P1=04 (Select by DF Name), P2=00 (First occurrence)
        return new ApduCommand((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, ppseBytes, 0x00, "SELECT PPSE (Contactless Directory 2PAY.SYS.DDF01)");
    }

    /**
     * Creates a {@code SELECT AID} APDU command for a specific card application (e.g. Visa, Mastercard, Troy).
     *
     * @param aidHex  Application Identifier hex string (e.g. "A0000000031010")
     * @param aidName Human-readable AID name
     * @return Formatted {@link ApduCommand}
     */
    public static ApduCommand createSelectAid(String aidHex, String aidName) {
        byte[] aidBytes = HexUtils.hexToBytes(aidHex);
        return new ApduCommand((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, aidBytes, 0x00, "SELECT AID (" + aidName + " - " + aidHex + ")");
    }

    /**
     * Creates a {@code GET PROCESSING OPTIONS (GPO)} APDU command (INS 0xA8).
     *
     * @param pdolDataBytes Terminal data requested in card PDOL (or null/empty)
     * @return Formatted {@link ApduCommand}
     */
    public static ApduCommand createGpo(byte[] pdolDataBytes) {
        byte[] data;
        if (pdolDataBytes == null || pdolDataBytes.length == 0) {
            // Default empty GPO command payload format: Tag 0x83 + Length 0x02 + 0x00 0x00
            data = new byte[]{(byte) 0x83, 0x02, 0x00, 0x00};
        } else {
            data = new byte[2 + pdolDataBytes.length];
            data[0] = (byte) 0x83;
            data[1] = (byte) pdolDataBytes.length;
            System.arraycopy(pdolDataBytes, 0, data, 2, pdolDataBytes.length);
        }
        // CLA=80 (EMV Proprietary), INS=A8 (GPO), P1=00, P2=00
        return new ApduCommand((byte) 0x80, (byte) 0xA8, (byte) 0x00, (byte) 0x00, data, 0x00, "GET PROCESSING OPTIONS (GPO)");
    }

    /**
     * Creates a {@code READ RECORD} APDU command (INS 0xB2) for a given SFI and Record number.
     *
     * @param sfi          Short File Identifier (1 to 30)
     * @param recordNumber Record index (1 to 255)
     * @return Formatted {@link ApduCommand}
     */
    public static ApduCommand createReadRecord(int sfi, int recordNumber) {
        // P2 = (SFI << 3) | 0x04 (0x04 indicates Record reading parameter)
        byte p2 = (byte) (((sfi & 0x1F) << 3) | 0x04);
        return new ApduCommand((byte) 0x00, (byte) 0xB2, (byte) recordNumber, p2, null, 0x00, "READ RECORD (SFI " + sfi + ", Record " + recordNumber + ")");
    }

    /**
     * Creates a {@code VERIFY} APDU command (INS 0x20) for Offline Plaintext PIN verification.
     *
     * @param pinDigits Plaintext PIN digits (e.g. "1234")
     * @return Formatted {@link ApduCommand}
     */
    public static ApduCommand createVerifyPin(String pinDigits) {
        // ISO 9564-1 Plaintext PIN block format for Offline PIN:
        // Byte 0: 0x20 | PIN Length (0x24 for 4-digit PIN)
        // Bytes 1..7: Padded PIN digits nibbles and 'F' nibble fill
        byte[] pinBlock = new byte[8];
        pinBlock[0] = (byte) (0x20 | (pinDigits.length() & 0x0F));
        for (int i = 0; i < 7; i++) {
            pinBlock[1 + i] = (byte) 0xFF;
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
        // CLA=00, INS=20 (VERIFY), P1=00, P2=80 (Plaintext PIN)
        return new ApduCommand((byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x80, pinBlock, null, "VERIFY (Offline Plaintext PIN)");
    }

    /**
     * Creates a {@code 1st GENERATE AC} APDU command (INS 0xAE) with CDOL1 payload.
     *
     * @param acType    Requested Cryptogram Type (0x80 = ARQC, 0x40 = TC, 0x00 = AAC)
     * @param cdol1Data Payload bytes matching Card CDOL1 requirements
     * @return Formatted {@link ApduCommand}
     */
    public static ApduCommand createFirstGenerateAc(byte acType, byte[] cdol1Data) {
        String acName = acType == (byte) 0x80 ? "ARQC (Online Request)" : (acType == (byte) 0x40 ? "TC (Offline Approved)" : "AAC (Offline Declined)");
        return new ApduCommand((byte) 0x80, (byte) 0xAE, acType, (byte) 0x00, cdol1Data, 0x00, "1st GENERATE AC (" + acName + ")");
    }

    /**
     * Creates a {@code 2nd GENERATE AC} APDU command (INS 0xAE) with CDOL2 / Issuer Auth Data payload.
     *
     * @param acType    Requested Cryptogram Type (0x40 = TC Approved, 0x00 = AAC Declined)
     * @param cdol2Data Payload bytes containing Host ARPC (Tag 91) and ARC (Tag 8A)
     * @return Formatted {@link ApduCommand}
     */
    public static ApduCommand createSecondGenerateAc(byte acType, byte[] cdol2Data) {
        String acName = acType == (byte) 0x40 ? "TC (Transaction Certificate)" : "AAC (Application Authentication Cryptogram)";
        return new ApduCommand((byte) 0x80, (byte) 0xAE, acType, (byte) 0x00, cdol2Data, 0x00, "2nd GENERATE AC (" + acName + ")");
    }

    /**
     * Creates a {@code GET DATA} APDU command (INS 0xCA) to read card internal counters (ATC, PIN Try Counter).
     *
     * @param tagHex Tag hex string to read (e.g. "9F36" for ATC, "9F17" for PIN Try Counter)
     * @return Formatted {@link ApduCommand}
     */
    public static ApduCommand createGetData(String tagHex) {
        byte[] tagBytes = HexUtils.hexToBytes(tagHex);
        byte p1 = tagBytes.length > 0 ? tagBytes[0] : 0x00;
        byte p2 = tagBytes.length > 1 ? tagBytes[1] : 0x00;
        return new ApduCommand((byte) 0x80, (byte) 0xCA, p1, p2, null, 0x00, "GET DATA (Tag 0x" + tagHex + ")");
    }
}
