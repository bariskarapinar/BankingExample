package com.myapp.bankingexample.iso8583;

import com.myapp.bankingexample.utils.HexUtils;

import java.nio.charset.StandardCharsets;

/**
 * ISO 8583-1:1987 Message Decoder &amp; Unpacker Engine.
 *
 * <p>Parses raw hex strings or byte arrays into structured {@link IsoMessage} objects.
 * Decodes MTI, Primary/Secondary Bitmaps, and parses active fields according to field spec rules.</p>
 */
public class IsoMessageUnpacker {

    /**
     * Unpacks a hex string representation of an ISO 8583 message.
     *
     * @param hexString Clean hex string
     * @return Unpacked {@link IsoMessage} model
     */
    public static IsoMessage unpackHex(String hexString) {
        return unpack(HexUtils.hexToBytes(hexString));
    }

    /**
     * Unpacks a raw byte array payload of an ISO 8583 message.
     *
     * @param bytes Raw payload bytes
     * @return Unpacked {@link IsoMessage} model
     */
    public static IsoMessage unpack(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return new IsoMessage("0000");
        }

        int index = 0;

        // Check for optional TPDU header (e.g. 5-byte TPDU starting with 0x60)
        if (bytes.length >= 16 && (bytes[0] == (byte) 0x60 || bytes[0] == (byte) 0x00)) {
            index = 5;
        }

        // Parse MTI (4 ASCII bytes)
        String mti = new String(bytes, index, 4, StandardCharsets.US_ASCII);
        index += 4;

        IsoMessage message = new IsoMessage(mti);

        // Parse Primary Bitmap (8 bytes)
        byte[] primaryBitmapBytes = new byte[8];
        System.arraycopy(bytes, index, primaryBitmapBytes, 0, 8);
        index += 8;

        boolean hasSecondary = (primaryBitmapBytes[0] & 0x80) != 0;
        byte[] bitmapBytes;
        if (hasSecondary) {
            bitmapBytes = new byte[16];
            System.arraycopy(primaryBitmapBytes, 0, bitmapBytes, 0, 8);
            System.arraycopy(bytes, index, bitmapBytes, 8, 8);
            index += 8;
        } else {
            bitmapBytes = primaryBitmapBytes;
        }

        IsoBitmap bitmap = new IsoBitmap(bitmapBytes);

        // Unpack Fields 2 to 128
        int maxField = hasSecondary ? 128 : 64;
        for (int fieldNum = 2; fieldNum <= maxField; fieldNum++) {
            if (!bitmap.isFieldSet(fieldNum)) {
                continue;
            }

            if (index >= bytes.length) break;

            IsoFieldSpec spec = IsoFieldDictionary.getFieldSpec(fieldNum);
            int fieldLen;

            if (spec.getLengthType() == IsoFieldSpec.LengthType.LLVAR) {
                if (index + 2 > bytes.length) break;
                String lenStr = new String(bytes, index, 2, StandardCharsets.US_ASCII);
                index += 2;
                try {
                    fieldLen = Integer.parseInt(lenStr);
                } catch (NumberFormatException e) {
                    fieldLen = spec.getMaxLen();
                }
            } else if (spec.getLengthType() == IsoFieldSpec.LengthType.LLLVAR) {
                if (index + 3 > bytes.length) break;
                String lenStr = new String(bytes, index, 3, StandardCharsets.US_ASCII);
                index += 3;
                try {
                    fieldLen = Integer.parseInt(lenStr);
                } catch (NumberFormatException e) {
                    fieldLen = spec.getMaxLen();
                }
            } else { // FIXED
                fieldLen = spec.getMaxLen();
            }

            String valStr;
            if (spec.getDataType() == IsoFieldSpec.DataType.BINARY) {
                if (index + fieldLen > bytes.length) {
                    fieldLen = bytes.length - index;
                }
                valStr = HexUtils.bytesToHex(bytes, index, fieldLen);
                index += fieldLen;
            } else {
                if (index + fieldLen > bytes.length) {
                    fieldLen = bytes.length - index;
                }
                valStr = new String(bytes, index, fieldLen, StandardCharsets.US_ASCII);
                index += fieldLen;
            }

            message.setField(fieldNum, valStr);
        }

        return message;
    }
}
