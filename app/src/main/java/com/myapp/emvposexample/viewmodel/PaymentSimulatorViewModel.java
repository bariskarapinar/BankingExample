package com.myapp.emvposexample.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.myapp.emvposexample.simulator.EndToEndPaymentSimulator;

/**
 * Educational Architecture ViewModel: End-to-End Payment Gateway Simulator State Manager.
 *
 * <p>Manages reactive LiveData state for the end-to-end 4-party financial transaction simulator
 * (POS Terminal -> EMV Chip Card -> ISO 8583 Acquirer -> Issuer Host).</p>
 *
 * @author Android Architecture & ViewModel Group
 */
public class PaymentSimulatorViewModel extends ViewModel {

    // Reactive MutableLiveData holding end-to-end simulation trace result model
    private final MutableLiveData<EndToEndPaymentSimulator.SimulationLogResult> simulationResult = new MutableLiveData<>();

    /**
     * Returns observable LiveData stream of end-to-end simulation results.
     *
     * @return Observable LiveData simulation result object.
     */
    public LiveData<EndToEndPaymentSimulator.SimulationLogResult> getSimulationResult() {
        return simulationResult;
    }

    /**
     * Triggers an end-to-end payment transaction simulation run off the main UI thread.
     *
     * @param cardScheme Card brand ("Visa", "Mastercard", "Troy", "American Express").
     * @param amount     Transaction amount in decimal currency units.
     * @param entryMode  POS entry mode ("051" Chip, "071" Contactless NFC).
     */
    public void runSimulation(String cardScheme, double amount, String entryMode) {
        // Step 1: Convert decimal transaction amount to integer cents
        long amountCents = (long) (amount * 100);
        // Step 2: Execute full 4-party simulation lifecycle
        EndToEndPaymentSimulator.SimulationLogResult res = EndToEndPaymentSimulator.runFullPaymentFlow(cardScheme, amountCents, entryMode);
        // Step 3: Post simulation trace result to UI observers
        simulationResult.setValue(res);
    }
}
