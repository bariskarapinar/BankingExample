package com.myapp.emvposexample.iso8583;

/**
 * Educational Data Transfer Object: Populated ISO 8583 Data Element Container.
 *
 * <p>Binds a populated field value string to its corresponding {@link IsoFieldSpec} metadata rules.</p>
 *
 * @author ISO 8583 Protocol Engineering Group
 */
public class IsoFieldValue {

    private final IsoFieldSpec spec;
    private final String value;

    /**
     * Constructs a populated ISO field value container.
     *
     * @param spec  Associated field specification metadata.
     * @param value Raw string value payload.
     */
    public IsoFieldValue(IsoFieldSpec spec, String value) {
        this.spec = spec;
        this.value = value != null ? value : "";
    }

    /**
     * Returns associated field specification metadata.
     */
    public IsoFieldSpec getSpec() {
        return spec;
    }

    /**
     * Returns raw field string value payload.
     */
    public String getValue() {
        return value;
    }
}
