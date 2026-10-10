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

import com.myapp.bankingexample.R;
import com.myapp.bankingexample.emv.ApduCommand;
import com.myapp.bankingexample.emv.ApduResponse;
import com.myapp.bankingexample.emv.EmvApduFactory;
import com.myapp.bankingexample.utils.HexUtils;

public class EmvApduFragment extends Fragment {

    private EditText etCla, etIns, etP1, etP2, etApduData;
    private TextView tvApduResult;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_emv_apdu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spApduPresets = view.findViewById(R.id.spApduPresets);
        etCla = view.findViewById(R.id.etCla);
        etIns = view.findViewById(R.id.etIns);
        etP1 = view.findViewById(R.id.etP1);
        etP2 = view.findViewById(R.id.etP2);
        etApduData = view.findViewById(R.id.etApduData);
        Button btnSendApdu = view.findViewById(R.id.btnSendApdu);
        tvApduResult = view.findViewById(R.id.tvApduResult);

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
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, presets);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spApduPresets.setAdapter(adapter);
        }

        spApduPresets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyPreset(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnSendApdu.setOnClickListener(v -> executeSimulatedApdu());
    }

    private void applyPreset(int position) {
        switch (position) {
            case 0: // SELECT PSE
                ApduCommand pse = EmvApduFactory.createSelectPse();
                fillFields(pse);
                break;
            case 1: // SELECT PPSE
                ApduCommand ppse = EmvApduFactory.createSelectPpse();
                fillFields(ppse);
                break;
            case 2: // SELECT Visa
                ApduCommand visa = EmvApduFactory.createSelectAid("A0000000031010", "Visa");
                fillFields(visa);
                break;
            case 3: // SELECT Mastercard
                ApduCommand mc = EmvApduFactory.createSelectAid("A0000000041010", "Mastercard");
                fillFields(mc);
                break;
            case 4: // GPO
                ApduCommand gpo = EmvApduFactory.createGpo(null);
                fillFields(gpo);
                break;
            case 5: // READ RECORD
                ApduCommand readRec = EmvApduFactory.createReadRecord(1, 1);
                fillFields(readRec);
                break;
            case 6: // VERIFY PIN
                ApduCommand verify = EmvApduFactory.createVerifyPin("1234");
                fillFields(verify);
                break;
            case 7: // GENERATE AC
                ApduCommand genAc = EmvApduFactory.createFirstGenerateAc((byte) 0x80, new byte[29]);
                fillFields(genAc);
                break;
        }
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

            ApduCommand cmd = new ApduCommand(cla, ins, p1, p2, data, 0x00, "Özel APDU Komutu");

            StringBuilder sb = new StringBuilder();
            sb.append("=== APDU İLETİŞİM SİMÜLASYONU ===\n\n");
            sb.append("-> GÖNDERİLEN APDU (TX):\n");
            sb.append("   Hex: ").append(cmd.toHex()).append("\n");
            sb.append("   CLA: 0x").append(String.format("%02X", cla & 0xFF));
            sb.append(" | INS: 0x").append(String.format("%02X", ins & 0xFF));
            sb.append(" | P1: 0x").append(String.format("%02X", p1 & 0xFF));
            sb.append(" | P2: 0x").append(String.format("%02X", p2 & 0xFF)).append("\n");
            sb.append("   Data: ").append(HexUtils.bytesToHex(data)).append("\n\n");

            // Simulate Card Response
            ApduResponse resp;
            if ((ins & 0xFF) == 0xA4) { // SELECT
                resp = ApduResponse.fromHex("6F1A840E31325041592E5359532E4444463031A5088801025F2D02656E9000");
            } else if ((ins & 0xFF) == 0xA8) { // GPO
                resp = ApduResponse.fromHex("770E82027C009404080101009000");
            } else if ((ins & 0xFF) == 0xB2) { // READ RECORD
                resp = ApduResponse.fromHex("702257134543123456789012D2812201000000000F5F24032812319000");
            } else if ((ins & 0xFF) == 0x20) { // VERIFY PIN
                resp = ApduResponse.fromHex("9000"); // PIN OK
            } else if ((ins & 0xFF) == 0xAE) { // GENERATE AC
                resp = ApduResponse.fromHex("80128000A4A1B2C3D4E5F6789006010A03A000009000"); // ARQC
            } else {
                resp = ApduResponse.fromHex("9000");
            }

            sb.append("<- ALINAN KART YANITI (RX):\n");
            sb.append("   Data Hex: ").append(resp.getDataHex()).append("\n");
            sb.append("   Status Words (SW1 SW2): ").append(resp.getSwHex()).append("\n");
            sb.append("   Durum Açıklaması: ").append(resp.getStatusDescription()).append("\n");

            tvApduResult.setText(sb.toString());

        } catch (Exception e) {
            tvApduResult.setText("Hata: Geçersiz APDU parametresi formatı.");
        }
    }
}
