package com.myapp.emvposexample;

import com.myapp.emvposexample.iso8583.IsoBitmap;
import com.myapp.emvposexample.iso8583.IsoMessage;
import com.myapp.emvposexample.iso8583.IsoMessagePacker;
import com.myapp.emvposexample.iso8583.IsoMessageUnpacker;
import com.myapp.emvposexample.iso8583.IsoMtiDecoder;
import com.myapp.emvposexample.iso8583.IsoSampleMessages;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Educational Unit Test Suite: ISO 8583 Message Engine Functional Tests.
 *
 * <p>Verifies primary 64-bit bitmap generation, automatic 128-bit secondary bitmap trigger logic,
 * round-trip 0200 message packing/unpacking, and 4-digit MTI classification decoding.</p>
 *
 * @author ISO 8583 Protocol Test Group
 */
public class Iso8583Test {

    /**
     * Verifies setting and clearing bit flags in a Primary 64-bit Bitmap container.
     */
    @Test
    public void testBitmapGeneration() {
        IsoBitmap bitmap = new IsoBitmap();
        bitmap.setField(3, true);
        bitmap.setField(4, true);
        bitmap.setField(11, true);

        assertTrue(bitmap.isFieldSet(3));
        assertTrue(bitmap.isFieldSet(4));
        assertTrue(bitmap.isFieldSet(11));
        assertFalse(bitmap.isFieldSet(2));
        assertFalse(bitmap.hasSecondaryBitmap());
    }

    /**
     * Verifies that activating any Secondary Field (Fields 65 to 128) automatically sets Bit 1.
     */
    @Test
    public void testSecondaryBitmapTrigger() {
        IsoBitmap bitmap = new IsoBitmap();
        bitmap.setField(3, true);
        bitmap.setField(70, true); // Secondary field active

        assertTrue(bitmap.hasSecondaryBitmap());
        assertTrue(bitmap.isFieldSet(1)); // Bit 1 active
        assertTrue(bitmap.isFieldSet(70));
    }

    /**
     * Verifies lossless round-trip serialization and deserialization of an ISO 8583 0200 message with Field 55 EMV payload.
     */
    @Test
    public void testPackAndUnpack0200Message() {
        // Step 1: Create 0200 Financial Request sample message
        IsoMessage origMsg = IsoSampleMessages.create0200FinancialRequest("9F2608A1B2C3D4E5F67890");

        // Step 2: Pack message model into binary bytes
        byte[] packedBytes = IsoMessagePacker.pack(origMsg);

        assertNotNull(packedBytes);
        assertTrue(packedBytes.length > 20);

        // Step 3: Unpack binary bytes back into object-oriented IsoMessage model
        IsoMessage unpackedMsg = IsoMessageUnpacker.unpack(packedBytes);

        // Step 4: Verify field payloads match original values
        assertEquals("0200", unpackedMsg.getMti());
        assertEquals("5412751234567890", unpackedMsg.getFieldValue(2));
        assertEquals("000000", unpackedMsg.getFieldValue(3));
        assertEquals("000000150000", unpackedMsg.getFieldValue(4));
        assertEquals("000456", unpackedMsg.getFieldValue(11));
        assertEquals("051", unpackedMsg.getFieldValue(22));
        assertEquals("9F2608A1B2C3D4E5F67890", unpackedMsg.getFieldValue(55));
    }

    /**
     * Verifies 4-digit MTI classification decoder rules for "0210" Financial Response.
     */
    @Test
    public void testMtiDecoder() {
        IsoMtiDecoder.MtiInfo info = IsoMtiDecoder.decode("0210");
        assertEquals("0210", info.mti);
        assertEquals("ISO 8583-1:1987", info.isoVersion);
        assertTrue(info.messageClass.contains("Financial"));
        assertTrue(info.messageFunction.contains("Response"));
    }
}
