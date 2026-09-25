package com.myapp.bankingexample;

import com.myapp.bankingexample.utils.PinBlockCalculator;
import org.junit.Test;

import static org.junit.Assert.*;

public class PinBlockCalculatorTest {

    @Test
    public void testIso0PinBlockCalculation() {
        String pin = "1234";
        String pan = "4543123456789012";

        PinBlockCalculator.PinBlockResult result = PinBlockCalculator.calculateIso0(pin, pan);

        assertNotNull(result);
        assertEquals("041234FFFFFFFFFF", result.clearPinFieldHex);
        assertEquals("0000312345678901", result.panFieldHex);
        // 041234FFFFFFFFFF XOR 0000312345678901 = 041205DCBA9876FE
        assertEquals("041205DCBA9876FE", result.pinBlockHex);
        assertNotNull(result.explanation);
    }

    @Test
    public void testIso1PinBlockCalculation() {
        String pin = "1234";
        PinBlockCalculator.PinBlockResult result = PinBlockCalculator.calculateIso1(pin);

        assertNotNull(result);
        assertTrue(result.pinBlockHex.startsWith("141234"));
        assertEquals(16, result.pinBlockHex.length());
    }
}
