package com.myapp.emvposexample.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.myapp.emvposexample.emv.ApduCommand;
import com.myapp.emvposexample.emv.ApduResponse;
import com.myapp.emvposexample.emv.EmvApduFactory;
import com.myapp.emvposexample.utils.HexUtils;

public class EmvApduViewModel extends ViewModel {

    private final MutableLiveData<ApduCommand> selectedCommand = new MutableLiveData<>();
    private final MutableLiveData<String> executionResultLog = new MutableLiveData<>();

    public LiveData<ApduCommand> getSelectedCommand() {
        return selectedCommand;
    }

    public LiveData<String> getExecutionResultLog() {
        return executionResultLog;
    }

    public void applyPreset(int position) {
        switch (position) {
            case 0:
                selectedCommand.setValue(EmvApduFactory.createSelectPse());
                break;
            case 1:
                selectedCommand.setValue(EmvApduFactory.createSelectPpse());
                break;
            case 2:
                selectedCommand.setValue(EmvApduFactory.createSelectAid("A0000000031010", "Visa"));
                break;
            case 3:
                selectedCommand.setValue(EmvApduFactory.createSelectAid("A0000000041010", "Mastercard"));
                break;
            case 4:
                selectedCommand.setValue(EmvApduFactory.createGpo(null));
                break;
            case 5:
                selectedCommand.setValue(EmvApduFactory.createReadRecord(1, 1));
                break;
            case 6:
                selectedCommand.setValue(EmvApduFactory.createVerifyPin("1234"));
                break;
            case 7:
                selectedCommand.setValue(EmvApduFactory.createFirstGenerateAc((byte) 0x80, new byte[29]));
                break;
        }
    }

    public void executeApdu(byte cla, byte ins, byte p1, byte p2, byte[] data) {
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

        ApduResponse resp;
        if ((ins & 0xFF) == 0xA4) {
            resp = ApduResponse.fromHex("6F1A840E31325041592E5359532E4444463031A5088801025F2D02656E9000");
        } else if ((ins & 0xFF) == 0xA8) {
            resp = ApduResponse.fromHex("770E82027C009404080101009000");
        } else if ((ins & 0xFF) == 0xB2) {
            resp = ApduResponse.fromHex("702257134543123456789012D2812201000000000F5F24032812319000");
        } else if ((ins & 0xFF) == 0x20) {
            resp = ApduResponse.fromHex("9000");
        } else if ((ins & 0xFF) == 0xAE) {
            resp = ApduResponse.fromHex("80128000A4A1B2C3D4E5F6789006010A03A000009000");
        } else {
            resp = ApduResponse.fromHex("9000");
        }

        sb.append("<- ALINAN KART YANITI (RX):\n");
        sb.append("   Data Hex: ").append(resp.getDataHex()).append("\n");
        sb.append("   Status Words (SW1 SW2): ").append(resp.getSwHex()).append("\n");
        sb.append("   Durum Açıklaması: ").append(resp.getStatusDescription()).append("\n");

        executionResultLog.setValue(sb.toString());
    }
}
