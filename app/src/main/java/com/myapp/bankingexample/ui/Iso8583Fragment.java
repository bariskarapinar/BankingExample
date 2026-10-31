package com.myapp.bankingexample.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.bankingexample.R;
import com.myapp.bankingexample.emv.TlvElement;
import com.myapp.bankingexample.emv.TlvParser;
import com.myapp.bankingexample.iso8583.IsoFieldDictionary;
import com.myapp.bankingexample.iso8583.IsoFieldSpec;
import com.myapp.bankingexample.iso8583.IsoFieldValue;
import com.myapp.bankingexample.iso8583.IsoMessage;
import com.myapp.bankingexample.iso8583.IsoMessagePacker;
import com.myapp.bankingexample.iso8583.IsoMessageUnpacker;
import com.myapp.bankingexample.iso8583.IsoMtiDecoder;
import com.myapp.bankingexample.iso8583.IsoSampleMessages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

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
        Button btnAnalyzeField55 = view.findViewById(R.id.btnAnalyzeField55);
        Button btnFieldLookupTool = view.findViewById(R.id.btnFieldLookupTool);
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
        btnMtiInfo.setOnClickListener(v -> displayMtiAndBitmapMatrix());
        if (btnAnalyzeField55 != null) {
            btnAnalyzeField55.setOnClickListener(v -> analyzeField55());
        }
        if (btnFieldLookupTool != null) {
            btnFieldLookupTool.setOnClickListener(v -> showFieldLookupDialog());
        }

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

    private void analyzeField55() {
        if (getContext() == null) return;
        String hex = etIsoHex.getText().toString().trim();
        IsoMessage msg = IsoMessageUnpacker.unpackHex(hex);
        String field55Hex = msg.getFieldValue(55);

        if (field55Hex == null || field55Hex.isEmpty()) {
            Toast.makeText(getContext(), "Bu ISO mesajında Saha 55 (EMV TLV) bulunamadı.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<TlvElement> tlvList = TlvParser.parseHex(field55Hex);
        StringBuilder sb = new StringBuilder();
        sb.append("=== SAHA 55 EMV BER-TLV DÖKÜMÜ ===\n\n");
        for (TlvElement tlv : tlvList) {
            sb.append("Tag ").append(tlv.getTagHex()).append(" [").append(tlv.getEmvTag().getName()).append("]\n");
            sb.append("  Uzunluk: ").append(tlv.getLength()).append(" Bayt\n");
            sb.append("  Değer Hex: ").append(tlv.getValueHex()).append("\n");
            if (tlv.getExplanation() != null && !tlv.getExplanation().isEmpty()) {
                sb.append("  Açıklama: ").append(tlv.getExplanation().replace("\n", "\n  ")).append("\n");
            }
            sb.append("\n");
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Saha 55 (Field 55 - ICC Data) Çip Analizi")
                .setMessage(sb.toString())
                .setPositiveButton("Kapat", null)
                .show();
    }

    private void showFieldLookupDialog() {
        if (getContext() == null) return;
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_field_lookup, null);
        EditText etFieldSearch = dialogView.findViewById(R.id.etFieldSearch);
        TextView tvFieldLookupResults = dialogView.findViewById(R.id.tvFieldLookupResults);

        Collection<IsoFieldSpec> allSpecs = IsoFieldDictionary.getAllSpecs();
        updateFieldLookupResults("", allSpecs, tvFieldLookupResults);

        etFieldSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateFieldLookupResults(s.toString(), allSpecs, tvFieldLookupResults);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        new AlertDialog.Builder(getContext())
                .setTitle("Arama Motorlu ISO 8583 Saha Kataloğu")
                .setView(dialogView)
                .setPositiveButton("Kapat", null)
                .show();
    }

    private void updateFieldLookupResults(String query, Collection<IsoFieldSpec> allSpecs, TextView tvResults) {
        String cleanQuery = query.toLowerCase(Locale.US).trim();
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (IsoFieldSpec spec : allSpecs) {
            boolean matchNum = String.valueOf(spec.getFieldNumber()).contains(cleanQuery);
            boolean matchName = spec.getName().toLowerCase(Locale.US).contains(cleanQuery);
            boolean matchDesc = spec.getDescriptionTr().toLowerCase(Locale.US).contains(cleanQuery);

            if (cleanQuery.isEmpty() || matchNum || matchName || matchDesc) {
                count++;
                sb.append("Saha (Field) ").append(spec.getFieldNumber()).append(" - ").append(spec.getName()).append("\n");
                sb.append("  Veri Tipi: ").append(spec.getDataType()).append(" | Uzunluk Tipi: ").append(spec.getLengthType()).append(" (Max ").append(spec.getMaxLen()).append(")\n");
                sb.append("  Açıklama: ").append(spec.getDescriptionTr()).append("\n\n");
            }
        }

        if (count == 0) {
            tvResults.setText("Aramaya uygun ISO 8583 sahası bulunamadı.");
        } else {
            tvResults.setText("Bulunan Saha Sayısı: " + count + "\n\n" + sb.toString());
        }
    }

    private void displayMtiAndBitmapMatrix() {
        if (getContext() == null) return;
        String hex = etIsoHex.getText().toString().trim();
        IsoMessage msg = IsoMessageUnpacker.unpackHex(hex);
        IsoMtiDecoder.MtiInfo mtiInfo = IsoMtiDecoder.decode(msg.getMti());

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_iso_bitmap_grid, null);
        TextView tvBitmapMatrixHex = dialogView.findViewById(R.id.tvBitmapMatrixHex);
        TextView tvBitmapMatrixDetails = dialogView.findViewById(R.id.tvBitmapMatrixDetails);

        tvBitmapMatrixHex.setText(String.format(Locale.US, "MTI: %s | Bitmap Hex: %s", mtiInfo.mti, msg.getBitmap().toHex()));

        StringBuilder sb = new StringBuilder();
        sb.append("=== MTI BİLGİSİ ===\n");
        sb.append(mtiInfo.getFullDescriptionTr()).append("\n\n");

        sb.append("=== AKTİF SAHALAR MATRİSİ (FIELD MATRIX) ===\n");
        int maxField = msg.getBitmap().hasSecondaryBitmap() ? 128 : 64;
        for (int i = 1; i <= maxField; i++) {
            boolean active = msg.getBitmap().isFieldSet(i);
            IsoFieldSpec spec = IsoFieldDictionary.getFieldSpec(i);
            if (active) {
                sb.append(String.format(Locale.US, "  [✓] Saha %-3d [%-30s]: AKTİF\n", i, spec.getName()));
            } else {
                sb.append(String.format(Locale.US, "  [ ] Saha %-3d [%-30s]: -\n", i, spec.getName()));
            }
        }

        tvBitmapMatrixDetails.setText(sb.toString());

        new AlertDialog.Builder(getContext())
                .setTitle("ISO 8583 MTI & Bitmap Matrisi")
                .setView(dialogView)
                .setPositiveButton("Kapat", null)
                .show();
    }
}
