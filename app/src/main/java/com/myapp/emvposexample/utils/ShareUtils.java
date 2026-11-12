package com.myapp.emvposexample.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.List;

public class ShareUtils {

    public static void copyToClipboard(@NonNull Context context, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && text != null && !text.isEmpty()) {
            ClipData clip = ClipData.newPlainText(label != null ? label : "Payment Data", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Pano'ya Kopyalandı", Toast.LENGTH_SHORT).show();
        }
    }

    public static void shareText(@NonNull Context context, String subject, String text) {
        if (text == null || text.isEmpty()) return;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject != null ? subject : "EMV & ISO 8583 Log");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent, "Paylaş / Gönder"));
    }

    public static void shareCsvLogs(@NonNull Context context, String subject, List<String> logLines) {
        if (logLines == null || logLines.isEmpty()) return;
        StringBuilder csv = new StringBuilder();
        csv.append("INDEX,LOG_LINE\n");
        for (int i = 0; i < logLines.size(); i++) {
            String sanitized = logLines.get(i).replace("\"", "\"\"");
            csv.append(i + 1).append(",\"").append(sanitized).append("\"\n");
        }
        shareText(context, subject != null ? subject : "EMV Transaction Log CSV", csv.toString());
    }
}
