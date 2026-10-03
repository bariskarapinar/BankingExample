package com.myapp.bankingexample.emv;

import com.myapp.bankingexample.utils.HexUtils;

import java.util.ArrayList;
import java.util.List;

public class TlvElement {

    private final String tagHex;
    private final EmvTag emvTag;
    private final int length;
    private final byte[] valueBytes;
    private final List<TlvElement> children = new ArrayList<>();
    private String explanation = "";

    public TlvElement(String tagHex, byte[] valueBytes) {
        this.tagHex = tagHex.toUpperCase();
        this.emvTag = EmvTagDictionary.getTag(this.tagHex);
        this.valueBytes = valueBytes != null ? valueBytes : new byte[0];
        this.length = this.valueBytes.length;
    }

    public String getTagHex() {
        return tagHex;
    }

    public EmvTag getEmvTag() {
        return emvTag;
    }

    public int getLength() {
        return length;
    }

    public byte[] getValueBytes() {
        return valueBytes;
    }

    public String getValueHex() {
        return HexUtils.bytesToHex(valueBytes);
    }

    public String getValueAscii() {
        return HexUtils.hexToAscii(getValueHex());
    }

    public List<TlvElement> getChildren() {
        return children;
    }

    public void addChild(TlvElement child) {
        if (child != null) {
            children.add(child);
        }
    }

    public boolean isConstructed() {
        return emvTag.isConstructed() || !children.isEmpty();
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public TlvElement findChildByTag(String tagHex) {
        if (tagHex == null) return null;
        for (TlvElement child : children) {
            if (child.getTagHex().equalsIgnoreCase(tagHex)) {
                return child;
            }
            TlvElement found = child.findChildByTag(tagHex);
            if (found != null) return found;
        }
        return null;
    }
}
