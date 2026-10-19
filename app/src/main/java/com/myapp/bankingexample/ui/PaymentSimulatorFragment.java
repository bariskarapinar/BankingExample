package com.myapp.bankingexample.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.bankingexample.R;
import com.myapp.bankingexample.simulator.EndToEndPaymentSimulator;

public class PaymentSimulatorFragment extends Fragment {

    private Spinner spCardScheme, spEntryMode;
    private EditText etAmount;
    private TextView tvSimStatus;
    private SimLogAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_simulator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spCardScheme = view.findViewById(R.id.spCardScheme);
        spEntryMode = view.findViewById(R.id.spEntryMode);
        etAmount = view.findViewById(R.id.etAmount);
        tvSimStatus = view.findViewById(R.id.tvSimStatus);
        Button btnStartSimulation = view.findViewById(R.id.btnStartSimulation);
        RecyclerView rvSimLogs = view.findViewById(R.id.rvSimLogs);

        adapter = new SimLogAdapter();
        rvSimLogs.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSimLogs.setAdapter(adapter);

        String[] cardSchemes = new String[]{"Visa", "Mastercard", "Troy", "American Express"};
        String[] entryModes = new String[]{"051 - EMV Temaslı Çip", "071 - EMV Temassız NFC"};

        if (getContext() != null) {
            ArrayAdapter<String> schemeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, cardSchemes);
            schemeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spCardScheme.setAdapter(schemeAdapter);

            ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, entryModes);
            modeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spEntryMode.setAdapter(modeAdapter);
        }

        btnStartSimulation.setOnClickListener(v -> runSimulation());
    }

    private void runSimulation() {
        String cardScheme = spCardScheme.getSelectedItem().toString();
        String entryMode = spEntryMode.getSelectedItemPosition() == 0 ? "051" : "071";

        long amountCents = 25000; // default
        try {
            double amt = Double.parseDouble(etAmount.getText().toString().trim());
            amountCents = (long) (amt * 100);
        } catch (Exception e) {
            etAmount.setText("250.00");
        }

        EndToEndPaymentSimulator.SimulationLogResult result = EndToEndPaymentSimulator.runFullPaymentFlow(cardScheme, amountCents, entryMode);
        tvSimStatus.setText(result.statusSummary);
        adapter.setLogs(result.logs);
    }
}
