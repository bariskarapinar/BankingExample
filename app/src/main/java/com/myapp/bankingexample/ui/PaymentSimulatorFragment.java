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

import java.util.Locale;

public class PaymentSimulatorFragment extends Fragment {

    private Spinner spCardScheme, spEntryMode;
    private EditText etAmount;
    private TextView tvSimStatus;
    private View receiptVisualizer;
    private TextView tvCardScheme, tvCardNumber, tvReceiptAmount, tvReceiptPan;
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
        receiptVisualizer = view.findViewById(R.id.receiptVisualizer);

        tvCardScheme = view.findViewById(R.id.tvCardScheme);
        tvCardNumber = view.findViewById(R.id.tvCardNumber);
        tvReceiptAmount = view.findViewById(R.id.tvReceiptAmount);
        tvReceiptPan = view.findViewById(R.id.tvReceiptPan);

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

        long amountCents = 25000;
        try {
            double amt = Double.parseDouble(etAmount.getText().toString().trim());
            amountCents = (long) (amt * 100);
        } catch (Exception e) {
            etAmount.setText("250.00");
        }

        EndToEndPaymentSimulator.SimulationLogResult result = EndToEndPaymentSimulator.runFullPaymentFlow(cardScheme, amountCents, entryMode);
        tvSimStatus.setText(result.statusSummary);
        adapter.setLogs(result.logs);

        // Update Credit Card Visualizer
        tvCardScheme.setText(cardScheme.toUpperCase(Locale.US));
        if ("Visa".equalsIgnoreCase(cardScheme)) {
            tvCardNumber.setText("4543  1234  5678  9012");
            if (tvReceiptPan != null) tvReceiptPan.setText("KART NO: 4543 **** **** 9012 (C)");
        } else if ("Mastercard".equalsIgnoreCase(cardScheme)) {
            tvCardNumber.setText("5412  7512  3456  7890");
            if (tvReceiptPan != null) tvReceiptPan.setText("KART NO: 5412 **** **** 7890 (C)");
        } else if ("Troy".equalsIgnoreCase(cardScheme)) {
            tvCardNumber.setText("9792  0012  3456  7890");
            if (tvReceiptPan != null) tvReceiptPan.setText("KART NO: 9792 **** **** 7890 (C)");
        } else {
            tvCardNumber.setText("3782  8212  3456  789");
            if (tvReceiptPan != null) tvReceiptPan.setText("KART NO: 3782 **** **** 789 (C)");
        }

        // Show Thermal Receipt
        if (receiptVisualizer != null) {
            receiptVisualizer.setVisibility(View.VISIBLE);
            if (tvReceiptAmount != null) {
                tvReceiptAmount.setText(String.format(Locale.US, "%.2f TL", amountCents / 100.0));
            }
        }
    }
}
