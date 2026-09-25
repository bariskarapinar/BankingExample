package com.myapp.bankingexample;

import com.myapp.bankingexample.emv.TlvElement;
import com.myapp.bankingexample.emv.TlvParser;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TlvParserTest {

    @Test
    public void testParseSimpleTlv() {
        // Tag 9F02 (6 bytes amount = 1000 TL), Tag 5F2A (2 bytes currency = TRY 0792)
        String hex = "9F02060000001000005F2A020792";
        List<TlvElement> elements = TlvParser.parseHex(hex);

        assertEquals(2, elements.size());

        TlvElement tag9F02 = elements.get(0);
        assertEquals("9F02", tag9F02.getTagHex());
        assertEquals(6, tag9F02.getLength());
        assertEquals("000000100000", tag9F02.getValueHex());

        TlvElement tag5F2A = elements.get(1);
        assertEquals("5F2A", tag5F2A.getTagHex());
        assertEquals(2, tag5F2A.getLength());
        assertEquals("0792", tag5F2A.getValueHex());
    }

    @Test
    public void testParseConstructedFciTlv() {
        // FCI Template (Tag 6F): Tag 84 DF Name (7 bytes 31325041592E53)
        String hex = "6F0B840731325041592E53";
        List<TlvElement> elements = TlvParser.parseHex(hex);

        assertEquals(1, elements.size());

        TlvElement fci = elements.get(0);
        assertEquals("6F", fci.getTagHex());
        assertTrue(fci.isConstructed());
        assertEquals(1, fci.getChildren().size());

        TlvElement dfName = fci.getChildren().get(0);
        assertEquals("84", dfName.getTagHex());
        assertEquals("31325041592E53", dfName.getValueHex());
    }

    @Test
    public void testTvrExplanation() {
        // TVR 0000008000 -> Byte 4 Bit 8: Transaction exceeds floor limit
        byte[] tvr = new byte[]{0x00, 0x00, 0x00, (byte) 0x80, 0x00};
        String explanation = TlvParser.explainTVR(tvr);
        assertNotNull(explanation);
        assertTrue(explanation.contains("floor limit"));
    }
}
