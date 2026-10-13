package com.myapp.bankingexample.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.bankingexample.R;

import java.util.ArrayList;
import java.util.List;

public class SimLogAdapter extends RecyclerView.Adapter<SimLogAdapter.ViewHolder> {

    private final List<String> logs = new ArrayList<>();

    public void setLogs(List<String> newLogs) {
        logs.clear();
        if (newLogs != null) {
            logs.addAll(newLogs);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sim_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String line = logs.get(position);
        holder.tvLogLine.setText(line);

        if (line.startsWith("===") || line.startsWith("---")) {
            holder.tvLogLine.setTextColor(Color.parseColor("#0284C7")); // Accent
            holder.tvLogLine.setTextSize(12);
        } else if (line.contains("APDU TX:")) {
            holder.tvLogLine.setTextColor(Color.parseColor("#2563EB")); // Blue
        } else if (line.contains("APDU RX:")) {
            holder.tvLogLine.setTextColor(Color.parseColor("#059669")); // Green
        } else if (line.contains("ONAYLANDI") || line.contains("BAŞARILI")) {
            holder.tvLogLine.setTextColor(Color.parseColor("#16A34A")); // Dark Green
        } else if (line.contains("Field ")) {
            holder.tvLogLine.setTextColor(Color.parseColor("#475569"));
        } else {
            holder.tvLogLine.setTextColor(Color.parseColor("#0F172A"));
        }
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLogLine;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLogLine = itemView.findViewById(R.id.tvLogLine);
        }
    }
}
