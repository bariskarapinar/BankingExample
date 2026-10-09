package com.myapp.bankingexample.iso8583;

public class IsoFieldValue {

    private final IsoFieldSpec spec;
    private final String value;

    public IsoFieldValue(IsoFieldSpec spec, String value) {
        this.spec = spec;
        this.value = value != null ? value : "";
    }

    public IsoFieldSpec getSpec() {
        return spec;
    }

    public String getValue() {
        return value;
    }
}
