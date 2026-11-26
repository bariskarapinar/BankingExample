package com.myapp.emvposexample.iso8583;

import java.util.Map;
import java.util.TreeMap;

/**
 * Educational Data Model: ISO 8583 Financial Message Container.
 *
 * <p>Represents a complete ISO 8583 message model containing transport header (TPDU),
 * 4-digit MTI, {@link IsoBitmap} bitmask container, and a sorted map of populated Data Elements (Fields 2 to 128).</p>

 * @author ISO 8583 Protocol Engineering Group
 */
public class IsoMessage {

    private String header = "";
    private String mti;
    private final IsoBitmap bitmap;
    private final Map<Integer, IsoFieldValue> fields = new TreeMap<>(); // Sorted by field number

    /**
     * Constructs a default ISO 8583 message with MTI "0200".
     */
    public IsoMessage() {
        this.mti = "0200";
        this.bitmap = new IsoBitmap();
    }

    /**
     * Constructs an ISO 8583 message with a custom MTI header.
     *
     * @param mti 4-digit Message Type Identifier (e.g. "0100", "0200", "0210").
     */
    public IsoMessage(String mti) {
        this.mti = mti;
        this.bitmap = new IsoBitmap();
    }

    /**
     * Returns transport network header string (e.g. TPDU).
     */
    public String getHeader() {
        return header;
    }

    /**
     * Sets transport network header string.
     */
    public void setHeader(String header) {
        this.header = header != null ? header : "";
    }

    /**
     * Returns 4-digit MTI header string.
     */
    public String getMti() {
        return mti;
    }

    /**
     * Sets 4-digit MTI header string.
     */
    public void setMti(String mti) {
        this.mti = mti;
    }

    /**
     * Returns associated {@link IsoBitmap} bitmask container.
     */
    public IsoBitmap getBitmap() {
        return bitmap;
    }

    /**
     * Sets a Data Element field value and automatically enables the corresponding bit in the bitmap.
     *
     * @param fieldNumber ISO field number (2 to 128).
     * @param value       Raw string value payload.
     */
    public void setField(int fieldNumber, String value) {
        if (fieldNumber >= 2 && fieldNumber <= 128) {
            IsoFieldSpec spec = IsoFieldDictionary.getFieldSpec(fieldNumber);
            fields.put(fieldNumber, new IsoFieldValue(spec, value));
            bitmap.setField(fieldNumber, true);
        }
    }

    /**
     * Returns the raw string value payload of a specific field number, or null if inactive.
     *
     * @param fieldNumber ISO field number (2 to 128).
     * @return Field string value payload, or null.
     */
    public String getFieldValue(int fieldNumber) {
        if (fields.containsKey(fieldNumber)) {
            return fields.get(fieldNumber).getValue();
        }
        return null;
    }

    /**
     * Returns sorted map of all active Data Elements in this message.
     */
    public Map<Integer, IsoFieldValue> getFields() {
        return fields;
    }

    /**
     * Returns whether a specific field number is active in this message bitmap.
     */
    public boolean isFieldSet(int fieldNumber) {
        return bitmap.isFieldSet(fieldNumber);
    }
}
