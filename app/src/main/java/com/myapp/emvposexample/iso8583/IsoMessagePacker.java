package com.myapp.emvposexample.iso8583;

import com.myapp.emvposexample.utils.HexUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

/**
 * Educational Protocol Engine: ISO 8583 Message Serializer / Packer.
 *
 * <p>Serializes an object-oriented {@link IsoMessage} model into a compliant binary/ASCII payload stream.
 * Executes exact header formatting, 4-digit MTI encoding, bitmap bitmask calculation, LLVAR/LLLVAR
 * variable-length header prepending, numeric zero-padding, and binary payload writing.</p>
 *
 * <h3>ISO 8583 Binary Packing Sequence:</h3>
 * <ol>
 *   <li><b>TPDU / Transport Header (Optional):</b> Raw hex transport network header (e.g. 5-byte TPDU).</li>
 *   <li><b>MTI (Message Type Identifier):</b> 4 ASCII bytes (e.g. '0', '2', '0', '0').</li>
 *   <li><b>Primary / Secondary Bitmap:</b> 8 bytes (64 bits) or 16 bytes (128 bits) binary bitmask.</li>
 *   <li><b>Data Elements (Fields 2 to 128):</b> Serialized sequentially in ascending field order based on active bitmap bits.</li>
 * </ol>
 *
 * @author ISO 8583 Protocol Engineering Group
 */
public class IsoMessagePacker {

    /**
     * Serializes an {@link IsoMessage} instance into a raw byte array payload.
     *
     * @param message Structured ISO 8583 message model.
     * @return Serialized byte array ready for socket transmission.
     */
    public static byte[] pack(IsoMessage message) {
        if (message == null) return new byte[0];

        try {
            // Step 1: Initialize dynamic byte array output buffer
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // Step 2: Write Transport Header (TPDU) if present
            if (message.getHeader() != null && !message.getHeader().isEmpty()) {
                baos.write(HexUtils.hexToBytes(message.getHeader()));
            }

            // Step 3: Write 4-digit MTI (Message Type Identifier) as ASCII bytes
            String mti = message.getMti();
            if (mti == null || mti.length() < 4) mti = "0200";
            baos.write(mti.getBytes(StandardCharsets.US_ASCII));

            // Step 4: Write Primary (and Secondary if needed) Binary Bitmap
            IsoBitmap bitmap = message.getBitmap();
            byte[] bitmapBytes = bitmap.toBytes();
            baos.write(bitmapBytes);

            // Step 5: Serialize each active Data Element field in ascending order
            Map<Integer, IsoFieldValue> fields = message.getFields();
            for (Map.Entry<Integer, IsoFieldValue> entry : fields.entrySet()) {
                IsoFieldValue fieldVal = entry.getValue();
                IsoFieldSpec spec = fieldVal.getSpec();
                String rawVal = fieldVal.getValue();

                // Serialize individual field payload
                packField(baos, spec, rawVal);
            }

            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    /**
     * Helper method to convert packed binary message bytes directly into a upper-case hex string.
     *
     * @param message Structured ISO 8583 message model.
     * @return Formatted upper-case hex string payload.
     */
    public static String packToHex(IsoMessage message) {
        return HexUtils.bytesToHex(pack(message));
    }

    /**
     * Serializes a single ISO 8583 field element into the stream according to its specification.
     * Handles LLVAR (2-digit length) and LLLVAR (3-digit length) variable headers and data type rules.
     */
    private static void packField(ByteArrayOutputStream baos, IsoFieldSpec spec, String value) throws IOException {
        if (value == null) value = "";

        // Calculate payload length (Binary bytes vs ASCII char count)
        int len = value.length();
        if (spec.getDataType() == IsoFieldSpec.DataType.BINARY) {
            len = (value.length() + 1) / 2; // Convert hex string length to byte count
        }

        // Step 1: Prepend Variable Length Header if required (LLVAR or LLLVAR)
        if (spec.getLengthType() == IsoFieldSpec.LengthType.LLVAR) {
            // LLVAR: 2-digit ASCII length header (e.g. "16")
            String lenStr = String.format(Locale.US, "%02d", len);
            baos.write(lenStr.getBytes(StandardCharsets.US_ASCII));
        } else if (spec.getLengthType() == IsoFieldSpec.LengthType.LLLVAR) {
            // LLLVAR: 3-digit ASCII length header (e.g. "075")
            String lenStr = String.format(Locale.US, "%03d", len);
            baos.write(lenStr.getBytes(StandardCharsets.US_ASCII));
        }

        // Step 2: Write Payload Data Bytes based on Data Type specification
        if (spec.getDataType() == IsoFieldSpec.DataType.BINARY) {
            // Binary data (e.g. Field 52 PIN block, Field 55 EMV TLV data)
            baos.write(HexUtils.hexToBytes(value));
        } else if (spec.getDataType() == IsoFieldSpec.DataType.NUMERIC && spec.getLengthType() == IsoFieldSpec.LengthType.FIXED) {
            // Fixed-length numeric data (zero-padded on the left to max length)
            String padded = String.format(Locale.US, "%" + spec.getMaxLen() + "s", value).replace(' ', '0');
            baos.write(padded.getBytes(StandardCharsets.US_ASCII));
        } else {
            // Standard ASCII string data
            baos.write(value.getBytes(StandardCharsets.US_ASCII));
        }
    }
}
