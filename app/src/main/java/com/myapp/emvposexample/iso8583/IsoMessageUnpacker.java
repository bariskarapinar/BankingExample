package com.myapp.emvposexample.iso8583;

import com.myapp.emvposexample.utils.HexUtils;

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

        if (bytes.length >= 16 && (bytes[0] == (byte) 0x60 || bytes[0] == (byte) 0x00)) {
            index = 5;
        }

        String mti = new String(bytes, index, 4, StandardCharsets.US_ASCII);
        index += 4;

        IsoMessage message = new IsoMessage(mti);

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
            } else {
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
