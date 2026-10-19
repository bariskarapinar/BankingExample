package com.myapp.bankingexample.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.bankingexample.R;
import com.myapp.bankingexample.emv.EmvCryptoUtils;
import com.myapp.bankingexample.emv.TlvElement;
import com.myapp.bankingexample.emv.TlvParser;
import com.myapp.bankingexample.utils.PinBlockCalculator;

import java.util.List;
import java.util.Locale;

public class EmvTlvFragment extends Fragment {

    private EditText etTlvInput;
    private TextView tvParsedCount;
    private TlvAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emv_tlv, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etTlvInput = view.findViewById(R.id.etTlvInput);
        tvParsedCount = view.findViewById(R.id.tvParsedCount);
        RecyclerView rvTlvTree = view.findViewById(R.id.rvTlvTree);

        adapter = new TlvAdapter();
        rvTlvTree.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTlvTree.setAdapter(adapter);

        Button btnParseTlv = view.findViewById(R.id.btnParseTlv);
        Button btnSampleFci = view.findViewById(R.id.btnSampleFci);
        Button btnSampleF55 = view.findViewById(R.id.btnSampleF55);
        Button btnPinBlockTool = view.findViewById(R.id.btnPinBlockTool);
        Button btnArqcCalcTool = view.findViewById(R.id.btnArqcCalcTool);

        // Sample FCI Hex
        String sampleFci = "6F1A840E31325041592E5359532E4444463031A5088801025F2D02656E";
        // Sample Field 55 Hex
        String sampleF55 = "9F2608A1B2C3D4E5F678909F2701809F100706010A03A000009F3704123456789F360200A4950500000080009A032609259C01009F02060000001500005F2A02079282027C009F1A020792";

        etTlvInput.setText(sampleF55);
        parseAndDisplay();

        btnParseTlv.setOnClickListener(v -> parseAndDisplay());
        btnSampleFci.setOnClickListener(v -> {
            etTlvInput.setText(sampleFci);
            parseAndDisplay();
        });
        btnSampleF55.setOnClickListener(v -> {
            etTlvInput.setText(sampleF55);
            parseAndDisplay();
        });

        btnPinBlockTool.setOnClickListener(v -> showPinBlockDialog());
        btnArqcCalcTool.setOnClickListener(v -> showArqcCalcDialog());
    }

    private void parseAndDisplay() {
        String inputHex = etTlvInput.getText().toString().trim();
        List<TlvElement> elements = TlvParser.parseHex(inputHex);
        adapter.setElements(elements);
        tvParsedCount.setText(String.format(Locale.US, "Ayrıştırılan Tag Sayısı: %d", elements.size()));
    }

    private void showPinBlockDialog() {
        if (getContext() == null) return;
        PinBlockCalculator.PinBlockResult res = PinBlockCalculator.calculateIso0("1234", "4543123456789012");
        new AlertDialog.Builder(getContext())
                .setTitle("ISO 9564-1 PIN Block Hesaplayıcı")
                .setMessage(res.explanation)
                .setPositiveButton("Kapat", null)
                .show();
    }

    private void showArqcCalcDialog() {
        if (getContext() == null) return;
        EmvCryptoUtils.ArqcVectorResult res = EmvCryptoUtils.buildArqcVector(
                "0123456789ABCDEF0123456789ABCDEF",
                "00A4",
                25000,
                "0792",
                "0000008000",
                "12345678",
                "7C00"
        );
        new AlertDialog.Builder(getContext())
                .setTitle("EMV ARQC Kriptogram Demosu")
                .setMessage(res.explanation)
                .setPositiveButton("Kapat", null)
                .show();
    }
}
