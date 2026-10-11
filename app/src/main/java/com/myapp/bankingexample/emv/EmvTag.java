package com.myapp.bankingexample.emv;

public class EmvTag {

    public enum TagClass {
        UNIVERSAL,
        APPLICATION,
        CONTEXT_SPECIFIC,
        PRIVATE
    }

    public enum TagType {
        PRIMITIVE,
        CONSTRUCTED
    }

    private final String hex;
    private final String name;
    private final String descriptionTr;
    private final String descriptionEn;
    private final String format;
    private final TagClass tagClass;
    private final TagType tagType;

    public EmvTag(String hex, String name, String descriptionTr, String descriptionEn, String format, TagClass tagClass, TagType tagType) {
        this.hex = hex.toUpperCase();
        this.name = name;
        this.descriptionTr = descriptionTr;
        this.descriptionEn = descriptionEn;
        this.format = format;
        this.tagClass = tagClass;
        this.tagType = tagType;
    }

    public String getHex() {
        return hex;
    }

    public String getName() {
        return name;
    }

    public String getDescriptionTr() {
        return descriptionTr;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public String getFormat() {
        return format;
    }

    public TagClass getTagClass() {
        return tagClass;
    }

    public TagType getTagType() {
        return tagType;
    }

    public boolean isConstructed() {
        return tagType == TagType.CONSTRUCTED;
    }
}
