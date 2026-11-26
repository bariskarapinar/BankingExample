package com.myapp.emvposexample.iso8583;

/**
 * Educational Data Model: ISO 8583 Data Element Specification Metadata.
 *
 * <p>Encapsulates metadata rules for an individual ISO 8583 Data Element (Field Number, Name, Data Type,
 * Length Type LLVAR/LLLVAR/FIXED, Maximum Length, and Description).</p>
 *
 * @author ISO 8583 Protocol Engineering Group
 */
public class IsoFieldSpec {

    /**
     * ISO 8583 Data Element format categories.
     */
    public enum DataType {
        NUMERIC,      // 'n' Digits
        ALPHA,        // 'a' Alphabetic characters
        ALPHANUMERIC, // 'an' Alphanumeric characters
        SPECIAL,      // 'ans' / Special characters
        BINARY,       // 'b' Binary bytes / Hex
        BCD,          // Binary Coded Decimal
        TRACK2        // Track 2 magnetic stripe characters
    }

    /**
     * ISO 8583 Length Header Types.
     */
    public enum LengthType {
        FIXED,  // Fixed character length
        LLVAR,  // 2-digit variable length header (00 to 99)
        LLLVAR  // 3-digit variable length header (000 to 999)
    }

    private final int fieldNumber;
    private final String name;
    private final DataType dataType;
    private final LengthType lengthType;
    private final int maxLen;
    private final String descriptionEn;

    /**
     * Constructs an ISO 8583 Data Element specification entry.
     */
    public IsoFieldSpec(int fieldNumber, String name, DataType dataType, LengthType lengthType, int maxLen, String descriptionEn) {
        this.fieldNumber = fieldNumber;
        this.name = name;
        this.dataType = dataType;
        this.lengthType = lengthType;
        this.maxLen = maxLen;
        this.descriptionEn = descriptionEn;
    }

    /**
     * Returns ISO 8583 field number (1 to 128).
     */
    public int getFieldNumber() {
        return fieldNumber;
    }

    /**
     * Returns friendly English field name (e.g. "Primary Account Number (PAN)").
     */
    public String getName() {
        return name;
    }

    /**
     * Returns ISO field data format type.
     */
    public DataType getDataType() {
        return dataType;
    }

    /**
     * Returns ISO field length header type (FIXED, LLVAR, LLLVAR).
     */
    public LengthType getLengthType() {
        return lengthType;
    }

    /**
     * Returns maximum allowable field character/byte length.
     */
    public int getMaxLen() {
        return maxLen;
    }

    public String getDescriptionTr() {
        return descriptionEn;
    }

    /**
     * Returns technical description string in English.
     */
    public String getDescriptionEn() {
        return descriptionEn;
    }
}
