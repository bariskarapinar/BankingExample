package com.myapp.emvposexample.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.myapp.emvposexample.emv.ApduCommand;
import com.myapp.emvposexample.emv.ApduResponse;
import com.myapp.emvposexample.emv.EmvApduFactory;
import com.myapp.emvposexample.utils.HexUtils;

import java.util.Locale;

/**
 * Educational Architecture ViewModel: ISO 7816 / EMV APDU Command Simulator State Manager.
 *
 * <p>Manages reactive LiveData state for APDU command construction, preset selection,
 * and smart card response SW1/SW2 status word evaluation.</p>
 *
 * @author Android Architecture & ViewModel Group
 */
public class EmvApduViewModel extends ViewModel {

    // Reactive MutableLiveData holding currently selected or populated APDU Command
    private final MutableLiveData<ApduCommand> selectedCommand = new MutableLiveData<>();
    // Reactive MutableLiveData holding APDU execution result log string
    private final MutableLiveData<String> executionResultLog = new MutableLiveData<>();

    /**
     * Returns observable LiveData stream of currently selected {@link ApduCommand}.
     *
     * @return Observable LiveData APDU command.
     */
    public LiveData<ApduCommand> getSelectedCommand() {
        return selectedCommand;
    }

    /**
     * Returns observable LiveData stream of APDU execution result log string.
     *
     * @return Observable LiveData execution log string.
     */
    public LiveData<String> getExecutionResultLog() {
        return executionResultLog;
    }

    /**
     * Applies a pre-configured APDU command preset based on Spinner dropdown selection position.
     *
     * @param position Selected preset index (0 to 7).
     */
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

    /**
     * Executes APDU command simulation and generates a detailed step-by-step communication log.
     *
     * @param cla  Class byte.
     * @param ins  Instruction byte.
     * @param p1   Parameter 1.
     * @param p2   Parameter 2.
     * @param data Command payload byte array.
     */
    public void executeApdu(byte cla, byte ins, byte p1, byte p2, byte[] data) {
        // Step 1: Construct APDU Command model
        ApduCommand cmd = new ApduCommand(cla, ins, p1, p2, data, 0x00, "Custom APDU Command");

        // Step 2: Build Outbound (TX) APDU Log Header
        StringBuilder sb = new StringBuilder();
        sb.append("=== APDU COMMUNICATION SIMULATION ===\n\n");
        sb.append("-> OUTBOUND APDU COMMAND (TX):\n");
        sb.append("   Hex: ").append(cmd.toHex()).append("\n");
        sb.append("   CLA: 0x").append(String.format(Locale.US, "%02X", cla & 0xFF));
        sb.append(" | INS: 0x").append(String.format(Locale.US, "%02X", ins & 0xFF));
        sb.append(" | P1: 0x").append(String.format(Locale.US, "%02X", p1 & 0xFF));
        sb.append(" | P2: 0x").append(String.format(Locale.US, "%02X", p2 & 0xFF)).append("\n");
        sb.append("   Data: ").append(HexUtils.bytesToHex(data)).append("\n\n");

        // Step 3: Simulate Smart Card Response (RX)
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

        // Step 4: Append Inbound (RX) Card Response to Log
        sb.append("<- INBOUND CARD RESPONSE (RX):\n");
        sb.append("   Data Hex: ").append(resp.getDataHex()).append("\n");
        sb.append("   Status Words (SW1 SW2): ").append(resp.getSwHex()).append("\n");
        sb.append("   Status Word Summary: ").append(resp.getStatusDescription()).append("\n");

        // Step 5: Post updated log string to observers
        executionResultLog.setValue(sb.toString());
    }
}
