package com.myapp.bankingexample.iso8583;

import java.util.Map;
import java.util.TreeMap;

public class IsoMessage {

    private String header = ""; // e.g. TPDU "6000000000"
    private String mti;
    private final IsoBitmap bitmap;
    private final Map<Integer, IsoFieldValue> fields = new TreeMap<>();

    public IsoMessage() {
        this.mti = "0200";
        this.bitmap = new IsoBitmap();
    }

    public IsoMessage(String mti) {
        this.mti = mti;
        this.bitmap = new IsoBitmap();
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header != null ? header : "";
    }

    public String getMti() {
        return mti;
    }

    public void setMti(String mti) {
        this.mti = mti;
    }

    public IsoBitmap getBitmap() {
        return bitmap;
    }

    public void setField(int fieldNumber, String value) {
        if (fieldNumber >= 2 && fieldNumber <= 128) {
            IsoFieldSpec spec = IsoFieldDictionary.getFieldSpec(fieldNumber);
            fields.put(fieldNumber, new IsoFieldValue(spec, value));
            bitmap.setField(fieldNumber, true);
        }
    }

    public String getFieldValue(int fieldNumber) {
        if (fields.containsKey(fieldNumber)) {
            return fields.get(fieldNumber).getValue();
        }
        return null;
    }

    public Map<Integer, IsoFieldValue> getFields() {
        return fields;
    }

    public boolean isFieldSet(int fieldNumber) {
        return bitmap.isFieldSet(fieldNumber);
    }
}
