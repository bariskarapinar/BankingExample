package com.myapp.emvposexample.ui;

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
import androidx.lifecycle.ViewModelProvider;

import com.myapp.emvposexample.R;
import com.myapp.emvposexample.emv.ApduCommand;
import com.myapp.emvposexample.utils.HexUtils;
import com.myapp.emvposexample.viewmodel.EmvApduViewModel;

public class EmvApduFragment extends Fragment {

    private EditText etCla, etIns, etP1, etP2, etApduData;
    private TextView tvApduResult;
    private EmvApduViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emv_apdu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(EmvApduViewModel.class);

        Spinner spApduPresets = view.findViewById(R.id.spApduPresets);
        etCla = view.findViewById(R.id.etCla);
        etIns = view.findViewById(R.id.etIns);
        etP1 = view.findViewById(R.id.etP1);
        etP2 = view.findViewById(R.id.etP2);
        etApduData = view.findViewById(R.id.etApduData);
        Button btnSendApdu = view.findViewById(R.id.btnSendApdu);
        tvApduResult = view.findViewById(R.id.tvApduResult);

        viewModel.getSelectedCommand().observe(getViewLifecycleOwner(), cmd -> {
            if (cmd != null) {
                fillFields(cmd);
            }
        });

        viewModel.getExecutionResultLog().observe(getViewLifecycleOwner(), log -> {
            if (log != null) {
                tvApduResult.setText(log);
            }
        });

        String[] presets = new String[]{
                "SELECT PSE (12PAY.SYS.DDF01)",
                "SELECT PPSE (2PAY.SYS.DDF01)",
                "SELECT Visa AID (A0000000031010)",
                "SELECT Mastercard AID (A0000000041010)",
                "GET PROCESSING OPTIONS (GPO)",
                "READ RECORD (SFI 1, Record 1)",
                "VERIFY Offline PIN (Plaintext)",
                "1st GENERATE AC (ARQC Request)"
        };

        if (getContext() != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_spinner_selected, presets);
            adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
            spApduPresets.setAdapter(adapter);
        }

        spApduPresets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                viewModel.applyPreset(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnSendApdu.setOnClickListener(v -> executeSimulatedApdu());
    }

    private void fillFields(ApduCommand cmd) {
        etCla.setText(String.format("%02X", cmd.getCla() & 0xFF));
        etIns.setText(String.format("%02X", cmd.getIns() & 0xFF));
        etP1.setText(String.format("%02X", cmd.getP1() & 0xFF));
        etP2.setText(String.format("%02X", cmd.getP2() & 0xFF));
        etApduData.setText(HexUtils.bytesToHex(cmd.getData()));
    }

    private void executeSimulatedApdu() {
        try {
            byte cla = HexUtils.hexToBytes(etCla.getText().toString().trim())[0];
            byte ins = HexUtils.hexToBytes(etIns.getText().toString().trim())[0];
            byte p1 = HexUtils.hexToBytes(etP1.getText().toString().trim())[0];
            byte p2 = HexUtils.hexToBytes(etP2.getText().toString().trim())[0];
            byte[] data = HexUtils.hexToBytes(etApduData.getText().toString().trim());

            viewModel.executeApdu(cla, ins, p1, p2, data);

        } catch (Exception e) {
            tvApduResult.setText("Error: Invalid APDU parameter hex format.");
        }
    }
}
