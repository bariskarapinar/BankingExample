package com.myapp.bankingexample.iso8583;

import com.myapp.bankingexample.utils.HexUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

public class IsoMessagePacker {

    public static byte[] pack(IsoMessage message) {
        if (message == null) return new byte[0];

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // 1. Header (if any)
            if (message.getHeader() != null && !message.getHeader().isEmpty()) {
                baos.write(HexUtils.hexToBytes(message.getHeader()));
            }

            // 2. MTI (4 ASCII bytes)
            String mti = message.getMti();
            if (mti == null || mti.length() < 4) mti = "0200";
            baos.write(mti.getBytes(StandardCharsets.US_ASCII));

            // 3. Bitmaps
            IsoBitmap bitmap = message.getBitmap();
            byte[] bitmapBytes = bitmap.toBytes();
            baos.write(bitmapBytes);

            // 4. Fields
            Map<Integer, IsoFieldValue> fields = message.getFields();
            for (Map.Entry<Integer, IsoFieldValue> entry : fields.entrySet()) {
                int fieldNum = entry.getKey();
                IsoFieldValue fieldVal = entry.getValue();
                IsoFieldSpec spec = fieldVal.getSpec();
                String rawVal = fieldVal.getValue();

                packField(baos, spec, rawVal);
            }

            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static String packToHex(IsoMessage message) {
        return HexUtils.bytesToHex(pack(message));
    }

    private static void packField(ByteArrayOutputStream baos, IsoFieldSpec spec, String value) throws IOException {
        if (value == null) value = "";

        // Fixed / Variable length check
        int len = value.length();
        if (spec.getDataType() == IsoFieldSpec.DataType.BINARY) {
            // For binary, input string is hex string, so byte count is hex length / 2
            len = (value.length() + 1) / 2;
        }

        // Length header
        if (spec.getLengthType() == IsoFieldSpec.LengthType.LLVAR) {
            String lenStr = String.format(Locale.US, "%02d", len);
            baos.write(lenStr.getBytes(StandardCharsets.US_ASCII));
        } else if (spec.getLengthType() == IsoFieldSpec.LengthType.LLLVAR) {
            String lenStr = String.format(Locale.US, "%03d", len);
            baos.write(lenStr.getBytes(StandardCharsets.US_ASCII));
        }

        // Value encoding
        if (spec.getDataType() == IsoFieldSpec.DataType.BINARY) {
            baos.write(HexUtils.hexToBytes(value));
        } else if (spec.getDataType() == IsoFieldSpec.DataType.NUMERIC && spec.getLengthType() == IsoFieldSpec.LengthType.FIXED) {
            // Pad numeric fixed field with leading zeroes
            String padded = String.format(Locale.US, "%" + spec.getMaxLen() + "s", value).replace(' ', '0');
            baos.write(padded.getBytes(StandardCharsets.US_ASCII));
        } else {
            baos.write(value.getBytes(StandardCharsets.US_ASCII));
        }
    }
}
