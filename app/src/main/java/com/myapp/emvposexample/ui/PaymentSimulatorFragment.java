package com.myapp.emvposexample.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.emvposexample.R;
import com.myapp.emvposexample.simulator.EndToEndPaymentSimulator;
import com.myapp.emvposexample.utils.ShareUtils;
import com.myapp.emvposexample.viewmodel.PaymentSimulatorViewModel;

import java.util.Locale;

public class PaymentSimulatorFragment extends Fragment {

    private Spinner spCardScheme, spEntryMode;
    private EditText etAmount;
    private TextView tvSimStatus;
    private View receiptVisualizer;
    private View cardContent;
    private ImageView ivCardLogo;
    private TextView tvCardScheme, tvCardNumber, tvReceiptAmount, tvReceiptPan;
    private SimLogAdapter adapter;
    private PaymentSimulatorViewModel viewModel;
    private EndToEndPaymentSimulator.SimulationLogResult currentResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment_simulator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PaymentSimulatorViewModel.class);

        spCardScheme = view.findViewById(R.id.spCardScheme);
        spEntryMode = view.findViewById(R.id.spEntryMode);
        etAmount = view.findViewById(R.id.etAmount);
        tvSimStatus = view.findViewById(R.id.tvSimStatus);
        receiptVisualizer = view.findViewById(R.id.receiptVisualizer);

        cardContent = view.findViewById(R.id.cardContent);
        ivCardLogo = view.findViewById(R.id.ivCardLogo);
        tvCardScheme = view.findViewById(R.id.tvCardScheme);
        tvCardNumber = view.findViewById(R.id.tvCardNumber);
        tvReceiptAmount = view.findViewById(R.id.tvReceiptAmount);
        tvReceiptPan = view.findViewById(R.id.tvReceiptPan);

        Button btnStartSimulation = view.findViewById(R.id.btnStartSimulation);
        Button btnShareLogs = view.findViewById(R.id.btnShareLogs);
        RecyclerView rvSimLogs = view.findViewById(R.id.rvSimLogs);

        adapter = new SimLogAdapter();
        rvSimLogs.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSimLogs.setAdapter(adapter);

        viewModel.getSimulationResult().observe(getViewLifecycleOwner(), res -> {
            if (res != null) {
                currentResult = res;
                tvSimStatus.setText(res.statusSummary);
                adapter.setLogs(res.logs);

                if (receiptVisualizer != null) {
                    receiptVisualizer.setVisibility(View.VISIBLE);
                    if (tvReceiptAmount != null) {
                        try {
                            double amt = Double.parseDouble(etAmount.getText().toString().trim());
                            tvReceiptAmount.setText(String.format(Locale.US, "$%.2f", amt));
                        } catch (Exception e) {
                            tvReceiptAmount.setText("$250.00");
                        }
                    }
                }
            }
        });

        String[] cardSchemes = new String[]{"Visa", "Mastercard", "Troy", "American Express"};
        String[] entryModes = new String[]{"051 - EMV Contact Chip", "071 - EMV Contactless NFC"};

        if (getContext() != null) {
            ArrayAdapter<String> schemeAdapter = new ArrayAdapter<>(getContext(), R.layout.item_spinner_selected, cardSchemes);
            schemeAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
            spCardScheme.setAdapter(schemeAdapter);

            ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(getContext(), R.layout.item_spinner_selected, entryModes);
            modeAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
            spEntryMode.setAdapter(modeAdapter);
        }

        spCardScheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCardVisualizer(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnStartSimulation.setOnClickListener(v -> runSimulation());
        if (btnShareLogs != null) {
            btnShareLogs.setOnClickListener(v -> {
                if (getContext() != null && currentResult != null) {
                    StringBuilder fullLog = new StringBuilder();
                    for (String line : currentResult.logs) {
                        fullLog.append(line).append("\n");
                    }
                    ShareUtils.shareText(getContext(), "EMV & ISO 8583 Payment Simulation Trace", fullLog.toString());
                }
            });
        }

        updateCardVisualizer(0);
    }

    private void updateCardVisualizer(int position) {
        if (cardContent == null || ivCardLogo == null || tvCardScheme == null || tvCardNumber == null) return;

        switch (position) {
            case 1: // Mastercard
                cardContent.setBackgroundResource(R.drawable.bg_card_mastercard);
                ivCardLogo.setImageResource(R.drawable.ic_mastercard_logo);
                tvCardScheme.setText("MASTERCARD");
                tvCardNumber.setText("5412  7512  3456  7890");
                if (tvReceiptPan != null) tvReceiptPan.setText("CARD NO: 5412 **** **** 7890 (C)");
                break;
            case 2: // Troy
                cardContent.setBackgroundResource(R.drawable.bg_card_troy);
                ivCardLogo.setImageResource(R.drawable.ic_troy_logo);
                tvCardScheme.setText("TROY DEBIT");
                tvCardNumber.setText("9792  0012  3456  7890");
                if (tvReceiptPan != null) tvReceiptPan.setText("CARD NO: 9792 **** **** 7890 (C)");
                break;
            case 3: // American Express
                cardContent.setBackgroundResource(R.drawable.bg_card_amex);
                ivCardLogo.setImageResource(R.drawable.ic_amex_logo);
                tvCardScheme.setText("AMEX CENTURION");
                tvCardNumber.setText("3782  821234  56789");
                if (tvReceiptPan != null) tvReceiptPan.setText("CARD NO: 3782 **** **** 789 (C)");
                break;
            case 0: // Visa
            default:
                cardContent.setBackgroundResource(R.drawable.bg_card_visa);
                ivCardLogo.setImageResource(R.drawable.ic_visa_logo);
                tvCardScheme.setText("VISA CREDIT");
                tvCardNumber.setText("4543  1234  5678  9012");
                if (tvReceiptPan != null) tvReceiptPan.setText("CARD NO: 4543 **** **** 9012 (C)");
                break;
        }
    }

    private void runSimulation() {
        String cardScheme = spCardScheme.getSelectedItem().toString();
        String entryMode = spEntryMode.getSelectedItemPosition() == 0 ? "051" : "071";

        double amt = 250.00;
        try {
            amt = Double.parseDouble(etAmount.getText().toString().trim());
        } catch (Exception e) {
            etAmount.setText("250.00");
        }

        viewModel.runSimulation(cardScheme, amt, entryMode);
    }
}
