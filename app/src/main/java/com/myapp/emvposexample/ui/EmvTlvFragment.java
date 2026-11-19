package com.myapp.emvposexample.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.myapp.emvposexample.emv.EmvCryptoUtils;
import com.myapp.emvposexample.emv.EmvTag;
import com.myapp.emvposexample.emv.EmvTagDictionary;
import com.myapp.emvposexample.emv.TlvParser;
import com.myapp.emvposexample.utils.HexUtils;
import com.myapp.emvposexample.viewmodel.EmvTlvViewModel;

import java.util.Collection;
import java.util.Locale;

public class EmvTlvFragment extends Fragment {

    private EditText etTlvInput;
    private TextView tvParsedCount;
    private TlvAdapter adapter;
    private EmvTlvViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emv_tlv, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EmvTlvViewModel.class);

        etTlvInput = view.findViewById(R.id.etTlvInput);
        tvParsedCount = view.findViewById(R.id.tvParsedCount);
        RecyclerView rvTlvTree = view.findViewById(R.id.rvTlvTree);

        adapter = new TlvAdapter();
        rvTlvTree.setLayoutManager(new LinearLayoutManager(getContext()));
        rvTlvTree.setAdapter(adapter);

        viewModel.getTlvElements().observe(getViewLifecycleOwner(), elements -> adapter.setElements(elements));
        viewModel.getParsedCountText().observe(getViewLifecycleOwner(), text -> tvParsedCount.setText(text));

        Button btnParseTlv = view.findViewById(R.id.btnParseTlv);
        Button btnTagLookupTool = view.findViewById(R.id.btnTagLookupTool);
        Button btnSampleFci = view.findViewById(R.id.btnSampleFci);
        Button btnSampleF55 = view.findViewById(R.id.btnSampleF55);
        Button btnPinBlockTool = view.findViewById(R.id.btnPinBlockTool);
        Button btnTvrEditorTool = view.findViewById(R.id.btnTvrEditorTool);
        Button btnArqcCalcTool = view.findViewById(R.id.btnArqcCalcTool);

        String sampleFci = "6F1A840E31325041592E5359532E4444463031A5088801025F2D02656E";
        String sampleF55 = "9F2608A1B2C3D4E5F678909F2701809F100706010A03A000009F3704123456789F360200A4950500000080009A032609259C01009F02060000001500005F2A02079282027C009F1A020792";

        etTlvInput.setText(sampleF55);
        parseAndDisplay();

        btnParseTlv.setOnClickListener(v -> parseAndDisplay());
        if (btnTagLookupTool != null) {
            btnTagLookupTool.setOnClickListener(v -> showTagLookupDialog());
        }
        btnSampleFci.setOnClickListener(v -> {
            etTlvInput.setText(sampleFci);
            parseAndDisplay();
        });
        btnSampleF55.setOnClickListener(v -> {
            etTlvInput.setText(sampleF55);
            parseAndDisplay();
        });

        btnPinBlockTool.setOnClickListener(v -> showPinBlockDialog());
        if (btnTvrEditorTool != null) {
            btnTvrEditorTool.setOnClickListener(v -> showTvrDialog());
        }
        btnArqcCalcTool.setOnClickListener(v -> showArqcCalcDialog());
    }

    private void parseAndDisplay() {
        String inputHex = etTlvInput.getText().toString().trim();
        viewModel.parseTlv(inputHex);
    }

    private void showTagLookupDialog() {
        if (getContext() == null) return;
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_tag_lookup, null);
        EditText etTagSearch = dialogView.findViewById(R.id.etTagSearch);
        TextView tvTagLookupResults = dialogView.findViewById(R.id.tvTagLookupResults);

        Collection<EmvTag> allTags = EmvTagDictionary.getAllTags();
        updateTagLookupResults("", allTags, tvTagLookupResults);

        etTagSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTagLookupResults(s.toString(), allTags, tvTagLookupResults);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Searchable EMV Tag Dictionary")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void updateTagLookupResults(String query, Collection<EmvTag> allTags, TextView tvResults) {
        String cleanQuery = query.toLowerCase(Locale.US).trim();
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (EmvTag tag : allTags) {
            boolean matchHex = tag.getHex().toLowerCase(Locale.US).contains(cleanQuery);
            boolean matchName = tag.getName().toLowerCase(Locale.US).contains(cleanQuery);
            boolean matchDesc = tag.getDescriptionEn().toLowerCase(Locale.US).contains(cleanQuery);

            if (cleanQuery.isEmpty() || matchHex || matchName || matchDesc) {
                count++;
                sb.append("Tag ").append(tag.getHex()).append(" - ").append(tag.getName()).append("\n");
                sb.append("  Format: ").append(tag.getFormat()).append(" | Type: ").append(tag.getTagType()).append("\n");
                sb.append("  Description: ").append(tag.getDescriptionEn()).append("\n\n");
            }
        }

        if (count == 0) {
            tvResults.setText("No matching EMV tag found.");
        } else {
            tvResults.setText("Tags Found: " + count + "\n\n" + sb.toString());
        }
    }

    private void showPinBlockDialog() {
        if (getContext() == null) return;
        PinPadDialog.show(getContext(), "4543123456789012", (pinDigits, pinBlockHex) -> {
            copyToClipboard(pinBlockHex);
            Toast.makeText(getContext(), "PIN: " + pinDigits + " | ISO-0 Block: " + pinBlockHex, Toast.LENGTH_LONG).show();
        });
    }

    private void showTvrDialog() {
        if (getContext() == null) return;
        byte[] sampleTvr = HexUtils.hexToBytes("0000008000");
        String explanation = TlvParser.explainTVR(sampleTvr);
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("TVR (Tag 95 - 5 Bytes / 40 Bits) Analysis")
                .setMessage(explanation)
                .setPositiveButton("Copy", (dialog, which) -> copyToClipboard("0000008000"))
                .setNegativeButton("Close", null)
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
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("EMV ARQC Cryptogram Demo")
                .setMessage(res.explanation)
                .setPositiveButton("Copy", (dialog, which) -> copyToClipboard(res.simulatedArqcHex))
                .setNegativeButton("Close", null)
                .show();
    }

    private void copyToClipboard(String text) {
        if (getContext() == null) return;
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("EMV Data", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Copied to Clipboard: " + text, Toast.LENGTH_SHORT).show();
        }
    }
}
