package com.myapp.emvposexample.iso8583;

public class IsoFieldSpec {

    public enum DataType {
        NUMERIC,
        ALPHA,
        ALPHANUMERIC,
        SPECIAL,
        BINARY,
        BCD,
        TRACK2
    }

    public enum LengthType {
        FIXED,
        LLVAR,
        LLLVAR
    }

    private final int fieldNumber;
    private final String name;
    private final DataType dataType;
    private final LengthType lengthType;
    private final int maxLen;
    private final String descriptionTr;

    public IsoFieldSpec(int fieldNumber, String name, DataType dataType, LengthType lengthType, int maxLen, String descriptionTr) {
        this.fieldNumber = fieldNumber;
        this.name = name;
        this.dataType = dataType;
        this.lengthType = lengthType;
        this.maxLen = maxLen;
        this.descriptionTr = descriptionTr;
    }

    public int getFieldNumber() {
        return fieldNumber;
    }

    public String getName() {
        return name;
    }

    public DataType getDataType() {
        return dataType;
    }

    public LengthType getLengthType() {
        return lengthType;
    }

    public int getMaxLen() {
        return maxLen;
    }

    public String getDescriptionTr() {
        return descriptionTr;
    }

    public String getDescriptionEn() {
        return descriptionTr; // Descriptions in IsoFieldDictionary are in English
    }
}
