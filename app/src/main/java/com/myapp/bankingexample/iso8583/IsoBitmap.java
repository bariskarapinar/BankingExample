package com.myapp.bankingexample.iso8583;

import com.myapp.bankingexample.utils.HexUtils;

import java.util.BitSet;

public class IsoBitmap {

    private final BitSet bitSet;

    public IsoBitmap() {
        this.bitSet = new BitSet(128);
    }

    public IsoBitmap(byte[] bitmapBytes) {
        this.bitSet = new BitSet(128);
        if (bitmapBytes != null) {
            for (int i = 0; i < bitmapBytes.length * 8 && i < 128; i++) {
                int byteIdx = i / 8;
                int bitIdx = 7 - (i % 8);
                if ((bitmapBytes[byteIdx] & (1 << bitIdx)) != 0) {
                    bitSet.set(i + 1); // 1-based ISO field index
                }
            }
        }
    }

    public void setField(int fieldNumber, boolean active) {
        if (fieldNumber >= 1 && fieldNumber <= 128) {
            if (active) {
                bitSet.set(fieldNumber);
            } else {
                bitSet.clear(fieldNumber);
            }
            // Field 1 indicates secondary bitmap presence
            boolean hasSecondary = false;
            for (int f = 65; f <= 128; f++) {
                if (bitSet.get(f)) {
                    hasSecondary = true;
                    break;
                }
            }
            if (hasSecondary) {
                bitSet.set(1);
            } else if (fieldNumber != 1) {
                bitSet.clear(1);
            }
        }
    }

    public boolean isFieldSet(int fieldNumber) {
        if (fieldNumber < 1 || fieldNumber > 128) return false;
        return bitSet.get(fieldNumber);
    }

    public boolean hasSecondaryBitmap() {
        return bitSet.get(1);
    }

    public byte[] toBytes() {
        int numBytes = hasSecondaryBitmap() ? 16 : 8;
        byte[] bytes = new byte[numBytes];
        for (int i = 0; i < numBytes * 8; i++) {
            if (bitSet.get(i + 1)) {
                int byteIdx = i / 8;
                int bitIdx = 7 - (i % 8);
                bytes[byteIdx] |= (1 << bitIdx);
            }
        }
        return bytes;
    }

    public String toHex() {
        return HexUtils.bytesToHex(toBytes());
    }

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
