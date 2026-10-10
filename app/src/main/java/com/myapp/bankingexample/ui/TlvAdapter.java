package com.myapp.bankingexample.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.bankingexample.R;
import com.myapp.bankingexample.emv.TlvElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TlvAdapter extends RecyclerView.Adapter<TlvAdapter.ViewHolder> {

    private final List<TlvElement> elements = new ArrayList<>();

    public void setElements(List<TlvElement> newElements) {
        elements.clear();
        if (newElements != null) {
            flattenAndAdd(newElements, 0);
        }
        notifyDataSetChanged();
    }

    private void flattenAndAdd(List<TlvElement> list, int depth) {
        for (TlvElement elem : list) {
            elements.add(elem);
            if (elem.getChildren() != null && !elem.getChildren().isEmpty()) {
                flattenAndAdd(elem.getChildren(), depth + 1);
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tlv_node, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TlvElement elem = elements.get(position);
        holder.tvTagHex.setText(elem.getTagHex());
        holder.tvTagName.setText(elem.getEmvTag().getName());
        holder.tvTagLen.setText(String.format(Locale.US, "%d Bayt", elem.getLength()));
        holder.tvTagValHex.setText(elem.getValueHex());

        String exp = elem.getExplanation();
        if (exp != null && !exp.trim().isEmpty()) {
            holder.tvExplanation.setVisibility(View.VISIBLE);
            holder.tvExplanation.setText(exp);
        } else {
            holder.tvExplanation.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTagHex, tvTagName, tvTagLen, tvTagValHex, tvExplanation;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTagHex = itemView.findViewById(R.id.tvTagHex);
            tvTagName = itemView.findViewById(R.id.tvTagName);
            tvTagLen = itemView.findViewById(R.id.tvTagLen);
            tvTagValHex = itemView.findViewById(R.id.tvTagValHex);
            tvExplanation = itemView.findViewById(R.id.tvExplanation);
        }
    }
}
