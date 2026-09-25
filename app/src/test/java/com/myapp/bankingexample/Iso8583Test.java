package com.myapp.bankingexample;

import com.myapp.bankingexample.iso8583.IsoBitmap;
import com.myapp.bankingexample.iso8583.IsoMessage;
import com.myapp.bankingexample.iso8583.IsoMessagePacker;
import com.myapp.bankingexample.iso8583.IsoMessageUnpacker;
import com.myapp.bankingexample.iso8583.IsoMtiDecoder;
import com.myapp.bankingexample.iso8583.IsoSampleMessages;
import org.junit.Test;

import static org.junit.Assert.*;

public class Iso8583Test {

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

    @Test
    public void testSecondaryBitmapTrigger() {
        IsoBitmap bitmap = new IsoBitmap();
        bitmap.setField(3, true);
        bitmap.setField(70, true); // Field > 64 triggers secondary bitmap (Field 1 set)

        assertTrue(bitmap.hasSecondaryBitmap());
        assertTrue(bitmap.isFieldSet(1));
        assertTrue(bitmap.isFieldSet(70));
    }

    @Test
    public void testPackAndUnpack0200Message() {
        IsoMessage origMsg = IsoSampleMessages.create0200FinancialRequest("9F2608A1B2C3D4E5F67890");
        byte[] packedBytes = IsoMessagePacker.pack(origMsg);

        assertNotNull(packedBytes);
        assertTrue(packedBytes.length > 20);

        IsoMessage unpackedMsg = IsoMessageUnpacker.unpack(packedBytes);

        assertEquals("0200", unpackedMsg.getMti());
        assertEquals("5412751234567890", unpackedMsg.getFieldValue(2));
        assertEquals("000000", unpackedMsg.getFieldValue(3));
        assertEquals("000000150000", unpackedMsg.getFieldValue(4));
        assertEquals("000456", unpackedMsg.getFieldValue(11));
        assertEquals("051", unpackedMsg.getFieldValue(22));
        assertEquals("9F2608A1B2C3D4E5F67890", unpackedMsg.getFieldValue(55));
    }

    @Test
    public void testMtiDecoder() {
        IsoMtiDecoder.MtiInfo info = IsoMtiDecoder.decode("0210");
        assertEquals("0210", info.mti);
        assertEquals("ISO 8583-1:1987", info.isoVersion);
        assertTrue(info.messageClass.contains("Finansal"));
        assertTrue(info.messageFunction.contains("Yanıt"));
    }
}
