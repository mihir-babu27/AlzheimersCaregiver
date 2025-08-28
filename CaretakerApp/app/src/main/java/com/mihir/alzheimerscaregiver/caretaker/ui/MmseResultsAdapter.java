package com.mihir.alzheimerscaregiver.caretaker.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mihir.alzheimerscaregiver.caretaker.MmseResultsActivity;
import com.mihir.alzheimerscaregiver.caretaker.R;
import com.mihir.alzheimerscaregiver.caretaker.data.entity.MmseResult;

import java.util.List;

public class MmseResultsAdapter extends RecyclerView.Adapter<MmseResultsAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(MmseResult item);
    }

    private List<MmseResult> items;
    private final OnItemClickListener listener;

    public MmseResultsAdapter(List<MmseResult> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateData(List<MmseResult> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public List<MmseResult> getItems() {
        return items == null ? java.util.Collections.emptyList() : new java.util.ArrayList<>(items);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mmse_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MmseResult item = items.get(position);
        holder.dateText.setText(MmseResultsActivity.formatDate(item.dateTaken));
        holder.scoreText.setText(String.valueOf(item.totalScore != null ? item.totalScore : 0));
        holder.interpretationText.setText(item.interpretation != null ? item.interpretation : "");
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView dateText;
        final TextView scoreText;
        final TextView interpretationText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            scoreText = itemView.findViewById(R.id.scoreText);
            interpretationText = itemView.findViewById(R.id.interpretationText);
        }
    }
}


