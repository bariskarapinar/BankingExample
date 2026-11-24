package com.myapp.emvposexample.emv;

import com.myapp.emvposexample.utils.HexUtils;

/**
 * Educational Data Transfer Object: ISO 7816-4 APDU Command Frame.
 *
 * <p>Represents an Application Protocol Data Unit (APDU) command frame transmitted from a card terminal
 * to a smart card chip or contactless NFC card. Encapsulates 4-byte header (CLA, INS, P1, P2), Lc payload,
 * data bytes, Le expected response length, and friendly description.</p>
 *
 * @author Smart Card Engineering Group
 */
public class ApduCommand {

    private final byte cla;
    private final byte ins;
    private final byte p1;
    private final byte p2;
    private final byte[] data;
    private final Integer le;
    private final String description;

    /**
     * Constructs an ISO 7816 APDU Command frame.
     *
     * @param cla         Class byte (0x00 Standard, 0x80 Proprietary/EMV).
     * @param ins         Instruction byte (0xA4 Select, 0xA8 GPO, 0xB2 Read Record, 0xAE Generate AC).
     * @param p1          Parameter 1.
     * @param p2          Parameter 2.
     * @param data        Data payload byte array (Lc is calculated automatically).
     * @param le          Expected response length Le byte (0x00 requests all available bytes).
     * @param description Friendly command explanation string.
     */
    public ApduCommand(byte cla, byte ins, byte p1, byte p2, byte[] data, Integer le, String description) {
        this.cla = cla;
        this.ins = ins;
        this.p1 = p1;
        this.p2 = p2;
        this.data = data != null ? data : new byte[0];
        this.le = le;
        this.description = description;
    }

    /**
     * Returns APDU Class byte (CLA).
     */
    public byte getCla() {
        return cla;
    }

    /**
     * Returns APDU Instruction byte (INS).
     */
    public byte getIns() {
        return ins;
    }

    /**
     * Returns Parameter 1 (P1).
     */
    public byte getP1() {
        return p1;
    }

    /**
     * Returns Parameter 2 (P2).
     */
    public byte getP2() {
        return p2;
    }

    /**
     * Returns command data payload byte array.
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Returns expected response byte length Le.
     */
    public Integer getLe() {
        return le;
    }

    /**
     * Returns friendly description string.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Serializes the APDU command frame into a raw byte array (CLA INS P1 P2 [Lc Data] [Le]).
     *
     * @return Serialized APDU byte array payload.
     */
    public byte[] toBytes() {
        int len = 4 + (data.length > 0 ? 1 + data.length : 0) + (le != null ? 1 : 0);
        byte[] bytes = new byte[len];
        int pos = 0;
        bytes[pos++] = cla;
        bytes[pos++] = ins;
        bytes[pos++] = p1;
        bytes[pos++] = p2;

        if (data.length > 0) {
            bytes[pos++] = (byte) data.length; // Lc length header byte
            System.arraycopy(data, 0, bytes, pos, data.length);
            pos += data.length;
        }

        if (le != null) {
            bytes[pos] = (byte) (le & 0xFF); // Le expected response byte
        }

        return bytes;
    }

    /**
     * Returns upper-case hexadecimal string representation of the serialized APDU command frame.
     *
     * @return Formatted upper-case hex string.
     */
    public String toHex() {
        return HexUtils.bytesToHex(toBytes());
    }
}
