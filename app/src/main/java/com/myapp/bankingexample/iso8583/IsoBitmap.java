package com.myapp.bankingexample.iso8583;

import com.myapp.bankingexample.utils.HexUtils;

import java.util.BitSet;

/**
 * ISO 8583 Primary (64-bit) &amp; Secondary (128-bit) Dynamic Bitmap Manager.
 *
 * <p>Manages field presence bitmasks for ISO 8583 Data Elements (Fields 1 to 128).
 * Field 1 acts as the Secondary Bitmap presence flag: if any field between 65 and 128 is set,
 * Field 1 is automatically activated, expanding the bitmap from 8 bytes (64 bits) to 16 bytes (128 bits).</p>
 */
public class IsoBitmap {

    private final BitSet bitSet;

    /**
     * Constructs an empty 128-bit ISO bitmap.
     */
    public IsoBitmap() {
        this.bitSet = new BitSet(128);
    }

    /**
     * Constructs an ISO bitmap initialized from a raw 8-byte or 16-byte binary payload.
     *
     * @param bitmapBytes 8-byte or 16-byte raw bitmap bytes
     */
    public IsoBitmap(byte[] bitmapBytes) {
        this.bitSet = new BitSet(128);
        if (bitmapBytes != null) {
            for (int i = 0; i < bitmapBytes.length * 8 && i < 128; i++) {
                int byteIdx = i / 8;
                int bitIdx = 7 - (i % 8);
                if ((bitmapBytes[byteIdx] & (1 << bitIdx)) != 0) {
                    bitSet.set(i + 1); // ISO fields are 1-indexed
                }
            }
        }
    }

    /**
     * Sets or clears a specific ISO 8583 field number (1 to 128) and dynamically updates Field 1 (Secondary Bitmap flag).
     *
     * @param fieldNumber ISO field number (1 to 128)
     * @param active      True to activate field, False to deactivate
     */
    public void setField(int fieldNumber, boolean active) {
        if (fieldNumber >= 1 && fieldNumber <= 128) {
            if (active) {
                bitSet.set(fieldNumber);
            } else {
                bitSet.clear(fieldNumber);
            }
            // Check if any secondary field (65..128) is present
            boolean hasSecondary = false;
            for (int f = 65; f <= 128; f++) {
                if (bitSet.get(f)) {
                    hasSecondary = true;
                    break;
                }
            }
            if (hasSecondary) {
                bitSet.set(1); // Set Field 1 (Secondary Bitmap Flag)
            } else if (fieldNumber != 1) {
                bitSet.clear(1); // Clear Field 1 if no secondary fields remain
            }
        }
    }

    /**
     * Checks whether a specific ISO 8583 field number is active in the bitmap.
     *
     * @param fieldNumber ISO field number (1 to 128)
     * @return True if active, False otherwise
     */
    public boolean isFieldSet(int fieldNumber) {
        if (fieldNumber < 1 || fieldNumber > 128) return false;
        return bitSet.get(fieldNumber);
    }

    /**
     * Returns whether the Secondary Bitmap (Fields 65-128) is present.
     *
     * @return True if Field 1 is set
     */
    public boolean hasSecondaryBitmap() {
        return bitSet.get(1);
    }

    /**
     * Exports the bitmap to an 8-byte (Primary only) or 16-byte (Primary + Secondary) raw binary array.
     *
     * @return Raw binary bitmap array
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
     * Exports the bitmap to a Hexadecimal string.
     *
     * @return Hexadecimal bitmap string (16 or 32 hex characters)
     */
    public String toHex() {
        return HexUtils.bytesToHex(toBytes());
    }

    /**
     * Exports the bitmap to a formatted space-separated 8-bit binary string representation.
     *
     * @return Space-separated binary string (e.g. "11000000 00111000 ...")
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
