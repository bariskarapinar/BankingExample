package com.myapp.emvposexample.ui;

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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.myapp.emvposexample.R;
import com.myapp.emvposexample.emv.TlvElement;
import com.myapp.emvposexample.emv.TlvParser;
import com.myapp.emvposexample.iso8583.IsoFieldDictionary;
import com.myapp.emvposexample.iso8583.IsoFieldSpec;
import com.myapp.emvposexample.iso8583.IsoFieldValue;
import com.myapp.emvposexample.iso8583.IsoMessage;
import com.myapp.emvposexample.iso8583.IsoMessageUnpacker;
import com.myapp.emvposexample.iso8583.IsoMtiDecoder;
import com.myapp.emvposexample.viewmodel.Iso8583ViewModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class Iso8583Fragment extends Fragment {

    private EditText etIsoHex;
    private TextView tvIsoHeaderSummary;
    private IsoFieldAdapter adapter;
    private Iso8583ViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_iso8583, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(Iso8583ViewModel.class);

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

        viewModel.getPackedHex().observe(getViewLifecycleOwner(), hex -> {
            if (hex != null && !hex.equals(etIsoHex.getText().toString())) {
                etIsoHex.setText(hex);
            }
        });

        viewModel.getHeaderSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
                tvIsoHeaderSummary.setText(summary);
            }
        });

        viewModel.getUnpackedMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                List<IsoFieldValue> list = new ArrayList<>(msg.getFields().values());
                adapter.setFields(list);
            }
        });

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
                viewModel.applyPreset(position);
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

        viewModel.applyPreset(0);
    }

    private void unpackAndDisplay() {
        String hex = etIsoHex.getText().toString().trim();
        viewModel.unpackHex(hex);
    }

    private void analyzeField55() {
        if (getContext() == null) return;
        String hex = etIsoHex.getText().toString().trim();
        IsoMessage msg = IsoMessageUnpacker.unpackHex(hex);
        String field55Hex = msg.getFieldValue(55);

        if (field55Hex == null || field55Hex.isEmpty()) {
            Toast.makeText(getContext(), "Field 55 (EMV TLV) not present in this ISO message.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<TlvElement> tlvList = TlvParser.parseHex(field55Hex);
        StringBuilder sb = new StringBuilder();
        sb.append("=== FIELD 55 EMV BER-TLV PAYLOAD DUMP ===\n\n");
        for (TlvElement tlv : tlvList) {
            sb.append("Tag ").append(tlv.getTagHex()).append(" [").append(tlv.getEmvTag().getName()).append("]\n");
            sb.append("  Length: ").append(tlv.getLength()).append(" Bytes\n");
            sb.append("  Value Hex: ").append(tlv.getValueHex()).append("\n");
            if (tlv.getExplanation() != null && !tlv.getExplanation().isEmpty()) {
                sb.append("  Explanation: ").append(tlv.getExplanation().replace("\n", "\n  ")).append("\n");
            }
            sb.append("\n");
        }

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Field 55 (ICC Data) Chip Analysis")
                .setMessage(sb.toString())
                .setPositiveButton("Close", null)
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

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Searchable ISO 8583 Field Catalog")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void updateFieldLookupResults(String query, Collection<IsoFieldSpec> allSpecs, TextView tvResults) {
        String cleanQuery = query.toLowerCase(Locale.US).trim();
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (IsoFieldSpec spec : allSpecs) {
            boolean matchNum = String.valueOf(spec.getFieldNumber()).contains(cleanQuery);
            boolean matchName = spec.getName().toLowerCase(Locale.US).contains(cleanQuery);
            boolean matchDesc = spec.getDescriptionEn().toLowerCase(Locale.US).contains(cleanQuery);

            if (cleanQuery.isEmpty() || matchNum || matchName || matchDesc) {
                count++;
                sb.append("Field ").append(spec.getFieldNumber()).append(" - ").append(spec.getName()).append("\n");
                sb.append("  Data Type: ").append(spec.getDataType()).append(" | Length Type: ").append(spec.getLengthType()).append(" (Max ").append(spec.getMaxLen()).append(")\n");
                sb.append("  Description: ").append(spec.getDescriptionEn()).append("\n\n");
            }
        }

        if (count == 0) {
            tvResults.setText("No matching ISO 8583 field found.");
        } else {
            tvResults.setText("Fields Found: " + count + "\n\n" + sb.toString());
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
        sb.append("=== MTI CLASSIFICATION ===\n");
        sb.append(mtiInfo.getFullDescriptionEn()).append("\n\n");

        sb.append("=== FIELD PRESENCE BITMAP MATRIX ===\n");
        int maxField = msg.getBitmap().hasSecondaryBitmap() ? 128 : 64;
        for (int i = 1; i <= maxField; i++) {
            boolean active = msg.getBitmap().isFieldSet(i);
            IsoFieldSpec spec = IsoFieldDictionary.getFieldSpec(i);
            if (active) {
                sb.append(String.format(Locale.US, "  [✓] Field %-3d [%-30s]: PRESENT\n", i, spec.getName()));
            } else {
                sb.append(String.format(Locale.US, "  [ ] Field %-3d [%-30s]: -\n", i, spec.getName()));
            }
        }

        tvBitmapMatrixDetails.setText(sb.toString());

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("ISO 8583 MTI & Bitmap Matrix")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }
}
