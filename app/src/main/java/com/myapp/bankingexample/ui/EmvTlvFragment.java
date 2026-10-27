package com.myapp.bankingexample.ui;

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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.bankingexample.R;
import com.myapp.bankingexample.emv.EmvCryptoUtils;
import com.myapp.bankingexample.emv.EmvTag;
import com.myapp.bankingexample.emv.EmvTagDictionary;
import com.myapp.bankingexample.emv.TlvElement;
import com.myapp.bankingexample.emv.TlvParser;
import com.myapp.bankingexample.utils.HexUtils;

import java.util.Collection;
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
        Button btnTagLookupTool = view.findViewById(R.id.btnTagLookupTool);
        Button btnSampleFci = view.findViewById(R.id.btnSampleFci);
        Button btnSampleF55 = view.findViewById(R.id.btnSampleF55);
        Button btnPinBlockTool = view.findViewById(R.id.btnPinBlockTool);
        Button btnTvrEditorTool = view.findViewById(R.id.btnTvrEditorTool);
        Button btnArqcCalcTool = view.findViewById(R.id.btnArqcCalcTool);

        // Sample FCI Hex
        String sampleFci = "6F1A840E31325041592E5359532E4444463031A5088801025F2D02656E";
        // Sample Field 55 Hex
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
        List<TlvElement> elements = TlvParser.parseHex(inputHex);
        adapter.setElements(elements);
        tvParsedCount.setText(String.format(Locale.US, "Ayrıştırılan Tag Sayısı: %d", elements.size()));
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

        new AlertDialog.Builder(getContext())
                .setTitle("Arama Motorlu EMV Tag Sözlüğü")
                .setView(dialogView)
                .setPositiveButton("Kapat", null)
                .show();
    }

    private void updateTagLookupResults(String query, Collection<EmvTag> allTags, TextView tvResults) {
        String cleanQuery = query.toLowerCase(Locale.US).trim();
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (EmvTag tag : allTags) {
            boolean matchHex = tag.getHex().toLowerCase(Locale.US).contains(cleanQuery);
            boolean matchName = tag.getName().toLowerCase(Locale.US).contains(cleanQuery);
            boolean matchDesc = tag.getDescriptionTr().toLowerCase(Locale.US).contains(cleanQuery);

            if (cleanQuery.isEmpty() || matchHex || matchName || matchDesc) {
                count++;
                sb.append("Tag ").append(tag.getHex()).append(" - ").append(tag.getName()).append("\n");
                sb.append("  Format: ").append(tag.getFormat()).append(" | Tip: ").append(tag.getTagType()).append("\n");
                sb.append("  Açıklama: ").append(tag.getDescriptionTr()).append("\n\n");
            }
        }

        if (count == 0) {
            tvResults.setText("Aramaya uygun EMV Tag bulunamadı.");
        } else {
            tvResults.setText("Bulunan Tag Sayısı: " + count + "\n\n" + sb.toString());
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
        new AlertDialog.Builder(getContext())
                .setTitle("TVR (Tag 95 - 5 Bayt / 40 Bit) Analizi")
                .setMessage(explanation)
                .setPositiveButton("Kopyala", (dialog, which) -> copyToClipboard("0000008000"))
                .setNegativeButton("Kapat", null)
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
                .setPositiveButton("Kopyala", (dialog, which) -> copyToClipboard(res.simulatedArqcHex))
                .setNegativeButton("Kapat", null)
                .show();
    }

    private void copyToClipboard(String text) {
        if (getContext() == null) return;
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("EMV Data", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(getContext(), "Pano'ya Kopyalandı: " + text, Toast.LENGTH_SHORT).show();
        }
    }
}
