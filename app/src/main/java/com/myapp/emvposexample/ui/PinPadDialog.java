package com.myapp.emvposexample.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.myapp.emvposexample.R;
import com.myapp.emvposexample.utils.PinBlockCalculator;

/**
 * Educational UI Component: Simulated POS PIN Pad Overlay Dialog.
 *
 * <p>Provides a virtual PIN Pad overlay to simulate cardholder PIN entry during EMV transactions.
 * Calculates ISO 9564-1 Format 0 (ISO-0 / ANSI X9.8) PIN blocks in real time using the Primary Account Number (PAN).</p>
 *
 * @author Android UI & Utilities Group
 */
public class PinPadDialog {

    /**
     * Callback interface triggered when cardholder confirms PIN entry on the virtual PIN Pad.
     */
    public interface OnPinEnteredListener {
        void onPinEntered(String pinDigits, String pinBlockHex);
    }

    /**
     * Inflates and displays the virtual POS PIN Pad modal dialog.
     *
     * @param context  Android Context.
     * @param pan      Primary Account Number used for ISO-0 PIN block XOR calculation.
     * @param listener Callback listener receiving confirmed PIN digits and PIN block hex.
     */
    public static void show(@NonNull Context context, String pan, @NonNull OnPinEnteredListener listener) {
        // Step 1: Inflate custom PIN Pad layout
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_emv_pinpad, null);

        TextView tvPinDisplay = view.findViewById(R.id.tvPinDisplay);
        TextView tvCalculatedPinBlock = view.findViewById(R.id.tvCalculatedPinBlock);

        StringBuilder pinSb = new StringBuilder();

        // Step 2: Build Material Alert Dialog
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        // Step 3: Attach Click Listener for Numeric Keypad Buttons (0 to 9)
        View.OnClickListener numberClickListener = v -> {
            if (pinSb.length() < 6) {
                Button btn = (Button) v;
                pinSb.append(btn.getText().toString());
                updatePinDisplay(pinSb.toString(), pan, tvPinDisplay, tvCalculatedPinBlock);
            }
        };

        int[] numberBtnIds = new int[]{
                R.id.btnKey0, R.id.btnKey1, R.id.btnKey2, R.id.btnKey3, R.id.btnKey4,
                R.id.btnKey5, R.id.btnKey6, R.id.btnKey7, R.id.btnKey8, R.id.btnKey9
        };

        for (int id : numberBtnIds) {
            Button btn = view.findViewById(id);
            if (btn != null) {
                btn.setOnClickListener(numberClickListener);
            }
        }

        // Step 4: Attach Click Listener for CLEAR (Delete) Button
        Button btnKeyClear = view.findViewById(R.id.btnKeyClear);
        if (btnKeyClear != null) {
            btnKeyClear.setOnClickListener(v -> {
                if (pinSb.length() > 0) {
                    pinSb.deleteCharAt(pinSb.length() - 1);
                    updatePinDisplay(pinSb.toString(), pan, tvPinDisplay, tvCalculatedPinBlock);
                }
            });
        }

        // Step 5: Attach Click Listener for ENTER (Confirm) Button
        Button btnKeyEnter = view.findViewById(R.id.btnKeyEnter);
        if (btnKeyEnter != null) {
            btnKeyEnter.setOnClickListener(v -> {
                String pin = pinSb.length() > 0 ? pinSb.toString() : "1234";
                PinBlockCalculator.PinBlockResult res = PinBlockCalculator.calculateIso0(pin, pan);
                listener.onPinEntered(pin, res.pinBlockHex);
                dialog.dismiss();
            });
        }

        // Initialize display with default preset "1234"
        updatePinDisplay("1234", pan, tvPinDisplay, tvCalculatedPinBlock);
        pinSb.append("1234");

        dialog.show();
    }

    /**
     * Updates masked PIN display dots ("● ● ● ●") and calculates real-time ISO-0 PIN block hex.
     */
    private static void updatePinDisplay(String pin, String pan, TextView tvDisplay, TextView tvBlock) {
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < pin.length(); i++) {
            if (i > 0) dots.append(" ");
            dots.append("●");
        }
        if (dots.length() == 0) {
            tvDisplay.setText("ENTER PIN");
        } else {
            tvDisplay.setText(dots.toString());
        }

        if (pin.length() >= 4) {
            PinBlockCalculator.PinBlockResult res = PinBlockCalculator.calculateIso0(pin, pan);
            tvBlock.setText("ISO-0 PIN Block: " + res.pinBlockHex);
        } else {
            tvBlock.setText("ISO-0 PIN Block: (Enter at least 4 digits)");
        }
    }
}
