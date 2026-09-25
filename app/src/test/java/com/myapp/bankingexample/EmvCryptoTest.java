package com.myapp.bankingexample;

import com.myapp.bankingexample.emv.EmvCryptoUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class EmvCryptoTest {

    @Test
    public void testArqcVectorCalculation() {
        EmvCryptoUtils.ArqcVectorResult result = EmvCryptoUtils.buildArqcVector(
                "0123456789ABCDEF0123456789ABCDEF",
                "00A4",
                25000,
                "0792",
                "0000008000",
                "12345678",
                "7C00"
        );

        assertNotNull(result);
        assertNotNull(result.sessionKeyHex);
        assertNotNull(result.inputDataVectorHex);
        assertNotNull(result.simulatedArqcHex);
        assertEquals(16, result.simulatedArqcHex.length()); // 8 bytes = 16 hex chars
        assertTrue(result.explanation.contains("ARQC KRİPTOGRAM"));
    }
}
