package com.myapp.bankingexample.iso8583;

import com.myapp.bankingexample.utils.HexUtils;

import java.nio.charset.StandardCharsets;

public class IsoMessageUnpacker {

    public static IsoMessage unpackHex(String hexString) {
        return unpack(HexUtils.hexToBytes(hexString));
    }

    public static IsoMessage unpack(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return new IsoMessage("0000");
        }

        int index = 0;

        // Optional TPDU or header check
        // Standard ASCII ISO 8583 starts with 4-digit MTI (e.g., '0' '1' '0' '0' or '0' '2' '0' '0')
        if (bytes.length >= 16 && (bytes[0] == (byte) 0x60 || bytes[0] == (byte) 0x00)) {
            // Header present (5 bytes hex/binary TPDU)
            index = 5;
        }

        String mti = new String(bytes, index, 4, StandardCharsets.US_ASCII);
        index += 4;

        IsoMessage message = new IsoMessage(mti);

        // Check primary bitmap (8 bytes)
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
            int fieldLen = 0;

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

            String valStr = "";
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
