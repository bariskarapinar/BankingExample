package com.myapp.emvposexample.emv;

/**
 * Educational Data Model: BER-TLV Tag Metadata Specification.
 *
 * <p>Encapsulates metadata attributes for standard BER-TLV tags defined in EMV Books 1-4
 * specifications (Tag Hex Code, Name, Format, Tag Class, and Primitive vs Constructed Type).</p>
 *
 * @author EMV L2 Kernel Engineering Group
 */
public class EmvTag {

    /**
     * ISO/IEC 8825-1 Tag Class classifications encoded in bits 8-7 of Tag Byte 1.
     */
    public enum TagClass {
        UNIVERSAL,        // Bit 8=0, Bit 7=0
        APPLICATION,      // Bit 8=0, Bit 7=1
        CONTEXT_SPECIFIC, // Bit 8=1, Bit 7=0
        PRIVATE           // Bit 8=1, Bit 7=1
    }

    /**
     * ISO/IEC 8825-1 Tag Type encoded in bit 6 of Tag Byte 1.
     */
    public enum TagType {
        PRIMITIVE,   // Bit 6 = 0 (Contains value data bytes)
        CONSTRUCTED  // Bit 6 = 1 (Contains nested child TLV tags)
    }

    private final String hex;
    private final String name;
    private final String descriptionEn;
    private final String format;
    private final TagClass tagClass;
    private final TagType tagType;

    /**
     * Constructs an EMV Tag specification entry.
     */
    public EmvTag(String hex, String name, String descriptionTr, String descriptionEn, String format, TagClass tagClass, TagType tagType) {
        this.hex = hex.toUpperCase();
        this.name = name;
        this.descriptionEn = descriptionEn;
        this.format = format;
        this.tagClass = tagClass;
        this.tagType = tagType;
    }

    /**
     * Returns upper-case hexadecimal tag code (e.g. "9F26").
     */
    public String getHex() {
        return hex;
    }

    /**
     * Returns friendly English tag name (e.g. "Application Cryptogram").
     */
    public String getName() {
        return name;
    }

    public String getDescriptionTr() {
        return descriptionEn;
    }

    /**
     * Returns long technical description in English.
     */
    public String getDescriptionEn() {
        return descriptionEn;
    }

    /**
     * Returns data format specification (e.g. "b 64", "n 6", "ans").
     */
    public String getFormat() {
        return format;
    }

    /**
     * Returns ISO/IEC 8825-1 Tag Class.
     */
    public TagClass getTagClass() {
        return tagClass;
    }

    /**
     * Returns ISO/IEC 8825-1 Tag Type (Primitive vs Constructed).
     */
    public TagType getTagType() {
        return tagType;
    }

    /**
     * Returns whether this tag is constructed (contains nested child tags).
     */
    public boolean isConstructed() {
        return tagType == TagType.CONSTRUCTED;
    }
}
