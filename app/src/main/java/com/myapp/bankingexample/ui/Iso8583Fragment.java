package com.myapp.bankingexample.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.myapp.bankingexample.iso8583.IsoFieldValue;
import com.myapp.bankingexample.iso8583.IsoMessage;
import com.myapp.bankingexample.iso8583.IsoMessagePacker;
import com.myapp.bankingexample.iso8583.IsoMessageUnpacker;
import com.myapp.bankingexample.iso8583.IsoMtiDecoder;
import com.myapp.bankingexample.iso8583.IsoSampleMessages;
import com.myapp.bankingexample.utils.HexUtils;

import java.util.ArrayList;
import java.util.List;

public class Iso8583Fragment extends Fragment {

    private EditText etIsoHex;
    private TextView tvIsoHeaderSummary;
    private IsoFieldAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_iso8583, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spIsoPresets = view.findViewById(R.id.spIsoPresets);
        etIsoHex = view.findViewById(R.id.etIsoHex);
        tvIsoHeaderSummary = view.findViewById(R.id.tvIsoHeaderSummary);
        Button btnUnpackIso = view.findViewById(R.id.btnUnpackIso);
        Button btnMtiInfo = view.findViewById(R.id.btnMtiInfo);
        RecyclerView rvIsoFields = view.findViewById(R.id.rvIsoFields);

        adapter = new IsoFieldAdapter();
        rvIsoFields.setLayoutManager(new LinearLayoutManager(getContext()));
        rvIsoFields.setAdapter(adapter);

        String[] presets = new String[]{
                "0200 Financial Request (With Field 55 EMV Data)",
                "0210 Financial Response (Auth Code 123456 & ARPC)",
                "0100 Authorization Request (Magstripe POS)",
                "0420 Reversal Advice (Timeout Reversal)",
                "0800 Network Management (Echo Test)"
        };

        if (getContext() != null) {
            ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, presets);
            spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spIsoPresets.setAdapter(spinAdapter);
        }

        spIsoPresets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyPreset(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnUnpackIso.setOnClickListener(v -> unpackAndDisplay());
        btnMtiInfo.setOnClickListener(v -> displayMtiInfo());

        // Default 0200
        applyPreset(0);
    }

    private void applyPreset(int position) {
        IsoMessage msg;
        switch (position) {
            case 0:
                msg = IsoSampleMessages.create0200FinancialRequest(null);
                break;
            case 1:
                msg = IsoSampleMessages.create0210FinancialResponse("123456", "00", null);
                break;
            case 2:
                msg = IsoSampleMessages.create0100AuthRequest();
                break;
            case 3:
                msg = IsoSampleMessages.create0420ReversalAdvice();
                break;
            default:
                msg = IsoSampleMessages.create0800NetworkManagement();
                break;
        }

        String hex = IsoMessagePacker.packToHex(msg);
        etIsoHex.setText(hex);
        unpackAndDisplay();
    }

    private void unpackAndDisplay() {
        String hex = etIsoHex.getText().toString().trim();
        IsoMessage msg = IsoMessageUnpacker.unpackHex(hex);

        IsoMtiDecoder.MtiInfo mtiInfo = IsoMtiDecoder.decode(msg.getMti());
        String headerSummary = "MTI: " + mtiInfo.getFullDescriptionTr() + "\nPrimary Bitmap: " + msg.getBitmap().toHex() + " (" + (msg.getBitmap().hasSecondaryBitmap() ? "128 Bit" : "64 Bit") + ")";
        tvIsoHeaderSummary.setText(headerSummary);

        List<IsoFieldValue> list = new ArrayList<>(msg.getFields().values());
        adapter.setFields(list);
    }

    private void displayMtiInfo() {
        String hex = etIsoHex.getText().toString().trim();
        IsoMessage msg = IsoMessageUnpacker.unpackHex(hex);
        IsoMtiDecoder.MtiInfo mtiInfo = IsoMtiDecoder.decode(msg.getMti());

        StringBuilder sb = new StringBuilder();
        sb.append("=== MTI DETAYLI ANALİZİ ===\n");
        sb.append("MTI Kodu: ").append(mtiInfo.mti).append("\n");
        sb.append("ISO Sürümü: ").append(mtiInfo.isoVersion).append("\n");
        sb.append("Mesaj Sınıfı: ").append(mtiInfo.messageClass).append("\n");
        sb.append("Mesaj İşlevi: ").append(mtiInfo.messageFunction).append("\n");
        sb.append("Mesaj Kaynağı: ").append(mtiInfo.messageOrigin);

        tvIsoHeaderSummary.setText(sb.toString());
    }
}
