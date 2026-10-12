package com.myapp.bankingexample.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.bankingexample.R;
import com.myapp.bankingexample.iso8583.IsoFieldValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IsoFieldAdapter extends RecyclerView.Adapter<IsoFieldAdapter.ViewHolder> {

    private final List<IsoFieldValue> fields = new ArrayList<>();

    public void setFields(List<IsoFieldValue> newFields) {
        fields.clear();
        if (newFields != null) {
            fields.addAll(newFields);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_iso_field, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IsoFieldValue fVal = fields.get(position);
        holder.tvFieldNum.setText(String.format(Locale.US, "Field %d", fVal.getSpec().getFieldNumber()));
        holder.tvFieldName.setText(fVal.getSpec().getName());
        holder.tvFieldVal.setText(fVal.getValue());
        holder.tvFieldDesc.setText(fVal.getSpec().getDescriptionTr());
    }

    @Override
    public int getItemCount() {
        return fields.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFieldNum, tvFieldName, tvFieldVal, tvFieldDesc;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFieldNum = itemView.findViewById(R.id.tvFieldNum);
            tvFieldName = itemView.findViewById(R.id.tvFieldName);
            tvFieldVal = itemView.findViewById(R.id.tvFieldVal);
            tvFieldDesc = itemView.findViewById(R.id.tvFieldDesc);
        }
    }
}
