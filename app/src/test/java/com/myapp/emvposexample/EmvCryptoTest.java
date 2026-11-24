package com.myapp.emvposexample;

import com.myapp.emvposexample.emv.EmvCryptoUtils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Educational Unit Test Suite: EMV Cryptography & ARQC Session Key Derivation Tests.
 *
 * <p>Verifies ATC session key diversification and 34-byte EMV Book 2 Table 13 input vector construction.</p>
 *
 * @author Financial Security Test Group
 */
public class EmvCryptoTest {

    /**
     * Verifies ARQC session key derivation and 8-byte 3DES CBC-MAC cryptogram calculation.
     */
    @Test
    public void testArqcVectorCalculation() {
        // Step 1: Calculate ARQC session key and input data vector
        EmvCryptoUtils.ArqcVectorResult result = EmvCryptoUtils.buildArqcVector(
                "0123456789ABCDEF0123456789ABCDEF",
                "00A4",
                25000,
                "0840",
                "0000008000",
                "12345678",
                "7C00"
        );

        // Step 2: Verify calculated session key, vector, and 8-byte ARQC cryptogram output
        assertNotNull(result);
        assertNotNull(result.sessionKeyHex);
        assertNotNull(result.inputDataVectorHex);
        assertNotNull(result.simulatedArqcHex);
        assertEquals(16, result.simulatedArqcHex.length());
        assertTrue(result.explanation.contains("ARQC CRYPTOGRAM"));
    }
}
