package com.myapp.bankingexample.iso8583;

public class IsoFieldSpec {

    public enum DataType {
        NUMERIC,        // n
        ALPHA,          // a
        ALPHANUMERIC,   // an
        SPECIAL,        // ans
        BINARY,         // b
        BCD,            // bcd
        TRACK2          // z / track2
    }

    public enum LengthType {
        FIXED,
        LLVAR,   // 2-digit length header (00..99)
        LLLVAR  // 3-digit length header (000..999)
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
}
