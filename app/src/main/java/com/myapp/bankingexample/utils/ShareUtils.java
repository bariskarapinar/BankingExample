package com.myapp.bankingexample.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

/**
 * Utility class providing Copy-to-Clipboard and Android System Share Intent actions.
 */
public class ShareUtils {

    /**
     * Copies text to the system clipboard and displays a Toast notification.
     *
     * @param context Android context
     * @param label   Clip label description
     * @param text    Text payload to copy
     */
    public static void copyToClipboard(@NonNull Context context, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && text != null && !text.isEmpty()) {
            ClipData clip = ClipData.newPlainText(label != null ? label : "Payment Data", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Pano'ya Kopyalandı", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Opens the Android System Share Sheet to share logs, receipts, or hex dumps.
     *
     * @param context Android context
     * @param subject Share subject
     * @param text    Full text body to share
     */
    public static void shareText(@NonNull Context context, String subject, String text) {
        if (text == null || text.isEmpty()) return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject != null ? subject : "EMV & ISO 8583 Log");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent, "Paylaş / Gönder"));
    }
}
