package com.myapp.emvposexample.emv;

import com.myapp.emvposexample.utils.HexUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Educational Data Transfer Object: ISO 7816-4 Smart Card APDU Response Frame.
 *
 * <p>Represents an APDU response frame returned from a smart card chip or contactless NFC card.
 * Encapsulates response data payload bytes and 2-byte Status Words (SW1 SW2), evaluating status
 * codes against ISO 7816-4 status dictionaries (e.g. 0x9000 = Success, 0x6A82 = File Not Found).</p>

 * @author Smart Card Engineering Group
 */
public class ApduResponse {

    private static final Map<String, String> SW_DICTIONARY = new HashMap<>();

    static {
        SW_DICTIONARY.put("9000", "Process completed successfully (9000 OK)");
        SW_DICTIONARY.put("61", "Command executed successfully, additional response bytes available");
        SW_DICTIONARY.put("6281", "Warning: Part of returned data may be corrupted");
        SW_DICTIONARY.put("63C2", "PIN verification failed (2 tries remaining)");
        SW_DICTIONARY.put("63C1", "PIN verification failed (1 try remaining)");
        SW_DICTIONARY.put("63C0", "PIN try limit exceeded / Card blocked");
        SW_DICTIONARY.put("6700", "Wrong length parameter Lc / Le");
        SW_DICTIONARY.put("6881", "Logical channel / Secure messaging not supported");
        SW_DICTIONARY.put("6982", "Security status not satisfied");
        SW_DICTIONARY.put("6983", "Authentication method blocked");
        SW_DICTIONARY.put("6984", "Referenced data invalidated");
        SW_DICTIONARY.put("6985", "Conditions of use not satisfied");
        SW_DICTIONARY.put("6A81", "Function not supported");
        SW_DICTIONARY.put("6A82", "File or Application not found (6A82)");
        SW_DICTIONARY.put("6A83", "Record not found (6A83)");
        SW_DICTIONARY.put("6A86", "Incorrect parameters P1 P2");
        SW_DICTIONARY.put("6A88", "Referenced data not found");
        SW_DICTIONARY.put("6D00", "Instruction code INS not supported");
        SW_DICTIONARY.put("6E00", "Class CLA not supported");
    }

    private final byte[] dataBytes;
    private final byte sw1;
    private final byte sw2;

    /**
     * Constructs an ISO 7816 APDU Response frame.
     *
     * @param dataBytes Response payload byte array.
     * @param sw1       Status Word 1 byte.
     * @param sw2       Status Word 2 byte.
     */
    public ApduResponse(byte[] dataBytes, byte sw1, byte sw2) {
        this.dataBytes = dataBytes != null ? dataBytes : new byte[0];
        this.sw1 = sw1;
        this.sw2 = sw2;
    }

    /**
     * Deserializes a full hexadecimal response string (including trailing 2-byte SW1 SW2) into an {@link ApduResponse} object.
     *
     * @param hexWithSw Raw hex response string (e.g. "6F1A...9000").
     * @return Parsed {@link ApduResponse} instance.
     */
    public static ApduResponse fromHex(String hexWithSw) {
        byte[] bytes = HexUtils.hexToBytes(hexWithSw);
        if (bytes.length < 2) {
            return new ApduResponse(new byte[0], (byte) 0x6F, (byte) 0x00);
        }
        int dataLen = bytes.length - 2;
        byte[] data = new byte[dataLen];
        System.arraycopy(bytes, 0, data, 0, dataLen);
        return new ApduResponse(data, bytes[bytes.length - 2], bytes[bytes.length - 1]);
    }

    /**
     * Returns response data payload byte array.
     */
    public byte[] getDataBytes() {
        return dataBytes;
    }

    /**
     * Returns formatted upper-case hexadecimal response data payload.
     */
    public String getDataHex() {
        return HexUtils.bytesToHex(dataBytes);
    }

    /**
     * Returns Status Word 1 byte.
     */
    public byte getSw1() {
        return sw1;
    }

    /**
     * Returns Status Word 2 byte.
     */
    public byte getSw2() {
        return sw2;
    }

    /**
     * Returns 4-character upper-case hex string representation of SW1 SW2 status words (e.g. "9000", "6A82").
     */
    public String getSwHex() {
        return String.format(Locale.US, "%02X%02X", sw1 & 0xFF, sw2 & 0xFF);
    }

    /**
     * Returns whether the response status indicates success (SW1 SW2 = 0x9000).
     *
     * @return <code>true</code> if success; <code>false</code> otherwise.
     */
    public boolean isSuccess() {
        return (sw1 & 0xFF) == 0x90 && (sw2 & 0xFF) == 0x00;
    }

    /**
     * Returns friendly English status description from SW dictionary.
     */
    public String getStatusDescription() {
        String swHex = getSwHex();
        if (SW_DICTIONARY.containsKey(swHex)) {
            return SW_DICTIONARY.get(swHex);
        }
        String sw1Prefix = String.format(Locale.US, "%02X", sw1 & 0xFF);
        if (SW_DICTIONARY.containsKey(sw1Prefix)) {
            return SW_DICTIONARY.get(sw1Prefix);
        }
        return "Status Word Code: 0x" + swHex;
    }
}
