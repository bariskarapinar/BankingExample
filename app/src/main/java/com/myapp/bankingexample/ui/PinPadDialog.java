package com.myapp.bankingexample.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.myapp.bankingexample.R;
import com.myapp.bankingexample.utils.PinBlockCalculator;

public class PinPadDialog {

    public interface OnPinEnteredListener {
        void onPinEntered(String pinDigits, String pinBlockHex);
    }

    public static void show(@NonNull Context context, String pan, @NonNull OnPinEnteredListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_emv_pinpad, null);

        TextView tvPinDisplay = view.findViewById(R.id.tvPinDisplay);
        TextView tvCalculatedPinBlock = view.findViewById(R.id.tvCalculatedPinBlock);

        StringBuilder pinSb = new StringBuilder();

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

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

        Button btnKeyClear = view.findViewById(R.id.btnKeyClear);
        if (btnKeyClear != null) {
            btnKeyClear.setOnClickListener(v -> {
                if (pinSb.length() > 0) {
                    pinSb.deleteCharAt(pinSb.length() - 1);
                    updatePinDisplay(pinSb.toString(), pan, tvPinDisplay, tvCalculatedPinBlock);
                }
            });
        }

        Button btnKeyEnter = view.findViewById(R.id.btnKeyEnter);
        if (btnKeyEnter != null) {
            btnKeyEnter.setOnClickListener(v -> {
                String pin = pinSb.length() > 0 ? pinSb.toString() : "1234";
                PinBlockCalculator.PinBlockResult res = PinBlockCalculator.calculateIso0(pin, pan);
                listener.onPinEntered(pin, res.pinBlockHex);
                dialog.dismiss();
            });
        }

        updatePinDisplay("1234", pan, tvPinDisplay, tvCalculatedPinBlock);
        pinSb.append("1234");

        dialog.show();
    }

    private static void updatePinDisplay(String pin, String pan, TextView tvDisplay, TextView tvBlock) {
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < pin.length(); i++) {
            if (i > 0) dots.append(" ");
            dots.append("●");
        }
        if (dots.length() == 0) {
            tvDisplay.setText("PIN GIRINIZ");
        } else {
            tvDisplay.setText(dots.toString());
        }

        if (pin.length() >= 4) {
            PinBlockCalculator.PinBlockResult res = PinBlockCalculator.calculateIso0(pin, pan);
            tvBlock.setText("ISO-0 PIN Block: " + res.pinBlockHex);
        } else {
            tvBlock.setText("ISO-0 PIN Block: (En az 4 hane girin)");
        }
    }
}
