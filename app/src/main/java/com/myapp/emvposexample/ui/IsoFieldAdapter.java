package com.myapp.emvposexample.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.myapp.emvposexample.R;
import com.myapp.emvposexample.iso8583.IsoFieldValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Educational UI Adapter: RecyclerView Adapter for ISO 8583 Data Elements.
 *
 * <p>Binds unpacked ISO 8583 field values to card list views, formatting field number badges,
 * field name labels, value payloads, and technical specification descriptions.</p>
 *
 * @author Android UI & Adapters Group
 */
public class IsoFieldAdapter extends RecyclerView.Adapter<IsoFieldAdapter.ViewHolder> {

    private final List<IsoFieldValue> fields = new ArrayList<>();

    /**
     * Updates adapter dataset with a new list of populated ISO 8583 field values.
     *
     * @param newFields List of populated {@link IsoFieldValue} entries.
     */
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
        // Step 1: Inflate ISO field item card view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_iso_field, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Step 2: Retrieve ISO field value model
        IsoFieldValue fVal = fields.get(position);

        // Step 3: Bind Field Number badge, Name, Value, and Specification description
        holder.tvFieldNum.setText(String.format(Locale.US, "Field %d", fVal.getSpec().getFieldNumber()));
        holder.tvFieldName.setText(fVal.getSpec().getName());
        holder.tvFieldVal.setText(fVal.getValue());
        holder.tvFieldDesc.setText(fVal.getSpec().getDescriptionEn());
    }

    @Override
    public int getItemCount() {
        return fields.size();
    }

    /**
     * ViewHolder caching UI widgets for ISO field item cards.
     */
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
