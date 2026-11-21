package com.myapp.emvposexample.iso8583;

import com.myapp.emvposexample.utils.HexUtils;

import java.nio.charset.StandardCharsets;

/**
 * Educational Protocol Engine: ISO 8583 Message Deserializer / Unpacker.
 *
 * <p>Parses an incoming raw binary or hexadecimal byte stream into a structured, object-oriented
 * {@link IsoMessage} model. Executes step-by-step header detection, MTI decoding, primary and
 * secondary bitmap parsing, LLVAR/LLLVAR variable-length header decoding, and payload field extraction.</p>

 * <h3>ISO 8583 Deserialization Steps:</h3>
 * <ol>
 *   <li><b>TPDU Header Detection:</b> Skips 5-byte network transport header if present.</li>
 *   <li><b>MTI Extraction:</b> Decodes 4 ASCII header characters into 4-digit MTI string.</li>
 *   <li><b>Bitmap Parsing:</b> Unpacks 8-byte Primary Bitmap. If Bit 1 is active (0x80), unpacks an additional 8-byte Secondary Bitmap.</li>
 *   <li><b>Field Extraction:</b> Loops through active bitmap fields (2 to 128), reading LLVAR/LLLVAR length headers and extracting binary or ASCII data payloads.</li>
 * </ol>
 *
 * @author ISO 8583 Protocol Engineering Group
 */
public class IsoMessageUnpacker {

    /**
     * Unpacks a hexadecimal payload string into a structured {@link IsoMessage} object.
     *
     * @param hexString Raw upper/lower-case hex payload.
     * @return Parsed {@link IsoMessage} model.
     */
    public static IsoMessage unpackHex(String hexString) {
        return unpack(HexUtils.hexToBytes(hexString));
    }

    /**
     * Deserializes a raw byte array payload into a structured {@link IsoMessage} model.
     *
     * @param bytes Incoming network payload byte stream.
     * @return Parsed {@link IsoMessage} object.
     */
    public static IsoMessage unpack(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return new IsoMessage("0000");
        }

        int index = 0;

        // Step 1: Detect and skip 5-byte TPDU transport header if present
        if (bytes.length >= 16 && (bytes[0] == (byte) 0x60 || bytes[0] == (byte) 0x00)) {
            index = 5;
        }

        // Step 2: Read 4 ASCII bytes representing MTI (e.g. "0200", "0210")
        String mti = new String(bytes, index, 4, StandardCharsets.US_ASCII);
        index += 4;

        IsoMessage message = new IsoMessage(mti);

        // Step 3: Read 8-byte Primary Bitmap
        byte[] primaryBitmapBytes = new byte[8];
        System.arraycopy(bytes, index, primaryBitmapBytes, 0, 8);
        index += 8;

        // Step 4: Check if Secondary Bitmap is active (Bit 1 = 0x80 in first bitmap byte)
        boolean hasSecondary = (primaryBitmapBytes[0] & 0x80) != 0;
        byte[] bitmapBytes;
        if (hasSecondary) {
            // Read additional 8-byte Secondary Bitmap (16 bytes total)
            bitmapBytes = new byte[16];
            System.arraycopy(primaryBitmapBytes, 0, bitmapBytes, 0, 8);
            System.arraycopy(bytes, index, bitmapBytes, 8, 8);
            index += 8;
        } else {
            bitmapBytes = primaryBitmapBytes;
        }

        IsoBitmap bitmap = new IsoBitmap(bitmapBytes);

        // Step 5: Deserialize each active Data Element field in ascending field order
        int maxField = hasSecondary ? 128 : 64;
        for (int fieldNum = 2; fieldNum <= maxField; fieldNum++) {
            // Check if field bit is enabled in bitmap
            if (!bitmap.isFieldSet(fieldNum)) {
                continue;
            }

            if (index >= bytes.length) break;

            IsoFieldSpec spec = IsoFieldDictionary.getFieldSpec(fieldNum);
            int fieldLen;

            // Step 5a: Read variable-length header (LLVAR or LLLVAR) if required
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
                // Fixed-length data element
                fieldLen = spec.getMaxLen();
            }

            // Step 5b: Extract Field Payload Data Bytes
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

            // Step 5c: Store parsed field value into ISO message model
            message.setField(fieldNum, valStr);
        }

        return message;
    }
}
