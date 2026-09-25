package com.myapp.bankingexample;

import com.myapp.bankingexample.simulator.EndToEndPaymentSimulator;
import org.junit.Test;

import static org.junit.Assert.*;

public class PaymentSimulatorTest {

    @Test
    public void testFullEndToEndPaymentSimulation() {
        EndToEndPaymentSimulator.SimulationLogResult result = EndToEndPaymentSimulator.runFullPaymentFlow("Visa", 25000, "051");

        assertNotNull(result);
        assertNotNull(result.statusSummary);
        assertTrue(result.statusSummary.contains("ONAYLANDI"));
        assertNotNull(result.requestIsoHex);
        assertNotNull(result.responseIsoHex);
        assertFalse(result.logs.isEmpty());
    }
}
