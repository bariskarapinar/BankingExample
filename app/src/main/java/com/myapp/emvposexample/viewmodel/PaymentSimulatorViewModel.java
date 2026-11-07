package com.myapp.emvposexample.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.myapp.emvposexample.simulator.EndToEndPaymentSimulator;

public class PaymentSimulatorViewModel extends ViewModel {

    private final MutableLiveData<EndToEndPaymentSimulator.SimulationLogResult> simulationResult = new MutableLiveData<>();

    public LiveData<EndToEndPaymentSimulator.SimulationLogResult> getSimulationResult() {
        return simulationResult;
    }

    public void runSimulation(String cardScheme, double amount, String entryMode) {
        long amountCents = (long) (amount * 100);
        EndToEndPaymentSimulator.SimulationLogResult res = EndToEndPaymentSimulator.runFullPaymentFlow(cardScheme, amountCents, entryMode);
        simulationResult.setValue(res);
    }
}
