package com.myapp.bankingexample.emv;

import com.myapp.bankingexample.utils.HexUtils;

import java.nio.charset.StandardCharsets;

public class EmvApduFactory {

    public static ApduCommand createSelectPse() {
        byte[] pseBytes = "12PAY.SYS.DDF01".getBytes(StandardCharsets.US_ASCII);
        return new ApduCommand((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, pseBytes, 0x00, "SELECT PSE (Contact Directory 12PAY.SYS.DDF01)");
    }

    public static ApduCommand createSelectPpse() {
        byte[] ppseBytes = "2PAY.SYS.DDF01".getBytes(StandardCharsets.US_ASCII);
        return new ApduCommand((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, ppseBytes, 0x00, "SELECT PPSE (Contactless Directory 2PAY.SYS.DDF01)");
    }

    public static ApduCommand createSelectAid(String aidHex, String aidName) {
        byte[] aidBytes = HexUtils.hexToBytes(aidHex);
        return new ApduCommand((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, aidBytes, 0x00, "SELECT AID (" + aidName + " - " + aidHex + ")");
    }

    public static ApduCommand createGpo(byte[] pdolDataBytes) {
        byte[] data;
        if (pdolDataBytes == null || pdolDataBytes.length == 0) {
            // Default empty GPO command data format 83 02 00 00
            data = new byte[]{(byte) 0x83, 0x02, 0x00, 0x00};
        } else {
            data = new byte[2 + pdolDataBytes.length];
            data[0] = (byte) 0x83;
            data[1] = (byte) pdolDataBytes.length;
            System.arraycopy(pdolDataBytes, 0, data, 2, pdolDataBytes.length);
        }
        return new ApduCommand((byte) 0x80, (byte) 0xA8, (byte) 0x00, (byte) 0x00, data, 0x00, "GET PROCESSING OPTIONS (GPO)");
    }

    public static ApduCommand createReadRecord(int sfi, int recordNumber) {
        byte p2 = (byte) (((sfi & 0x1F) << 3) | 0x04);
        return new ApduCommand((byte) 0x00, (byte) 0xB2, (byte) recordNumber, p2, null, 0x00, "READ RECORD (SFI " + sfi + ", Record " + recordNumber + ")");
    }

    public static ApduCommand createVerifyPin(String pinDigits) {
        // Plaintext PIN block format for ISO 9564-1 Format 2 or Offline PIN
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
        return new ApduCommand((byte) 0x00, (byte) 0x20, (byte) 0x00, (byte) 0x80, pinBlock, null, "VERIFY (Offline Plaintext PIN)");
    }

    public static ApduCommand createFirstGenerateAc(byte acType, byte[] cdol1Data) {
        // acType: 0x80 = ARQC, 0x40 = TC, 0x00 = AAC
        String acName = acType == (byte) 0x80 ? "ARQC (Online Request)" : (acType == (byte) 0x40 ? "TC (Offline Approved)" : "AAC (Offline Declined)");
        return new ApduCommand((byte) 0x80, (byte) 0xAE, acType, (byte) 0x00, cdol1Data, 0x00, "1st GENERATE AC (" + acName + ")");
    }

    public static ApduCommand createSecondGenerateAc(byte acType, byte[] cdol2Data) {
        String acName = acType == (byte) 0x40 ? "TC (Transaction Certificate)" : "AAC (Application Authentication Cryptogram)";
        return new ApduCommand((byte) 0x80, (byte) 0xAE, acType, (byte) 0x00, cdol2Data, 0x00, "2nd GENERATE AC (" + acName + ")");
    }

    public static ApduCommand createGetData(String tagHex) {
        byte[] tagBytes = HexUtils.hexToBytes(tagHex);
        byte p1 = tagBytes.length > 0 ? tagBytes[0] : 0x00;
        byte p2 = tagBytes.length > 1 ? tagBytes[1] : 0x00;
        return new ApduCommand((byte) 0x80, (byte) 0xCA, p1, p2, null, 0x00, "GET DATA (Tag 0x" + tagHex + ")");
    }
}
