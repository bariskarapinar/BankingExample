package com.myapp.emvposexample.iso8583;

import com.myapp.emvposexample.utils.HexUtils;

import java.util.BitSet;

/**
 * Educational Protocol Engine: ISO 8583 Primary & Secondary Bitmap Manager.
 *
 * <p>In ISO 8583 payment messaging, the <b>Bitmap</b> is an 8-byte (64-bit) or 16-byte (128-bit)
 * bitmask placed immediately after the 4-digit MTI header. Each active bit indicates the presence
 * of the corresponding Data Element in the message payload.</p>
 *
 * <h3>ISO 8583 Bitmap Rules:</h3>
 * <ul>
 *   <li><b>Bit 1 (Secondary Bitmap Flag):</b> If Bit 1 = <code>1</code>, a 128-bit Secondary Bitmap is present (Fields 65 to 128). If Bit 1 = <code>0</code>, only 64 fields exist.</li>
 *   <li><b>Bits 2 to 64:</b> Maps directly to Data Elements Field 2 through Field 64.</li>
 *   <li><b>Bits 65 to 128:</b> Maps directly to Data Elements Field 65 through Field 128.</li>
 * </ul>
 *
 * @author ISO 8583 Protocol Engineering Group
 */
public class IsoBitmap {

    private final BitSet bitSet;

    /**
     * Initializes an empty 128-bit ISO 8583 Bitmap container.
     */
    public IsoBitmap() {
        this.bitSet = new BitSet(128);
    }

    /**
     * Parses a raw 8-byte (64-bit) or 16-byte (128-bit) binary bitmap buffer into bitmask flags.
     *
     * @param bitmapBytes Raw byte buffer.
     */
    public IsoBitmap(byte[] bitmapBytes) {
        this.bitSet = new BitSet(128);
        if (bitmapBytes != null) {
            for (int i = 0; i < bitmapBytes.length * 8 && i < 128; i++) {
                int byteIdx = i / 8;
                int bitIdx = 7 - (i % 8); // Most significant bit first
                if ((bitmapBytes[byteIdx] & (1 << bitIdx)) != 0) {
                    bitSet.set(i + 1); // 1-based ISO Field index
                }
            }
        }
    }

    /**
     * Sets or clears a specific ISO 8583 Data Element bit flag (Fields 1 to 128).
     * Automatically sets Bit 1 if any Secondary field (65-128) is active.
     *
     * @param fieldNumber Data Element field number (1 to 128).
     * @param active      <code>true</code> to enable field; <code>false</code> to disable.
     */
    public void setField(int fieldNumber, boolean active) {
        if (fieldNumber >= 1 && fieldNumber <= 128) {
            if (active) {
                bitSet.set(fieldNumber);
            } else {
                bitSet.clear(fieldNumber);
            }
            // Evaluate whether Secondary Bitmap (Bit 1) is required
            boolean hasSecondary = false;
            for (int f = 65; f <= 128; f++) {
                if (bitSet.get(f)) {
                    hasSecondary = true;
                    break;
                }
            }
            if (hasSecondary) {
                bitSet.set(1); // Enable Secondary Bitmap flag
            } else if (fieldNumber != 1) {
                bitSet.clear(1);
            }
        }
    }

    /**
     * Checks if a specific ISO 8583 field number is present in this bitmap.
     *
     * @param fieldNumber Data Element field number (1 to 128).
     * @return <code>true</code> if active; <code>false</code> otherwise.
     */
    public boolean isFieldSet(int fieldNumber) {
        if (fieldNumber < 1 || fieldNumber > 128) return false;
        return bitSet.get(fieldNumber);
    }

    /**
     * Returns whether the Secondary Bitmap flag (Bit 1) is active.
     *
     * @return <code>true</code> if Secondary Bitmap exists (16-byte total length).
     */
    public boolean hasSecondaryBitmap() {
        return bitSet.get(1);
    }

    /**
     * Packs the bitmap flags into a raw 8-byte (64-bit) or 16-byte (128-bit) byte array.
     *
     * @return Packed binary bitmap byte array.
     */
    public byte[] toBytes() {
        int numBytes = hasSecondaryBitmap() ? 16 : 8;
        byte[] bytes = new byte[numBytes];
        for (int i = 0; i < numBytes * 8; i++) {
            if (bitSet.get(i + 1)) {
                int byteIdx = i / 8;
                int bitIdx = 7 - (i % 8);
                bytes[byteIdx] |= (byte) (1 << bitIdx);
            }
        }
        return bytes;
    }

    /**
     * Returns an upper-case hexadecimal representation of the bitmap (e.g. "723C468020E00200").
     *
     * @return Formatted upper-case hex string.
     */
    public String toHex() {
        return HexUtils.bytesToHex(toBytes());
    }

    /**
     * Formats the bitmap as space-separated 8-bit binary strings for educational bitmask inspection.
     *
     * @return Space-delimited binary bitmask string (e.g. "01110010 00111100...").
     */
    public String toBinaryString() {
        StringBuilder sb = new StringBuilder();
        int maxBit = hasSecondaryBitmap() ? 128 : 64;
        for (int i = 1; i <= maxBit; i++) {
            sb.append(bitSet.get(i) ? "1" : "0");
            if (i % 8 == 0 && i < maxBit) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
