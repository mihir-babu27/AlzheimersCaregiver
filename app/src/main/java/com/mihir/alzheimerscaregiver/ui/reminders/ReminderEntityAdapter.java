package com.mihir.alzheimerscaregiver.ui.reminders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mihir.alzheimerscaregiver.R;
import com.mihir.alzheimerscaregiver.data.entity.ReminderEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderEntityAdapter extends RecyclerView.Adapter<ReminderEntityAdapter.ReminderViewHolder> {

    public interface OnReminderInteractionListener {
        void onCompletionToggled(ReminderEntity reminder);
        void onItemClicked(ReminderEntity reminder);
        void onItemSwipedToDelete(ReminderEntity reminder, int position);
    }

    private final List<ReminderEntity> reminders = new ArrayList<>();
    private OnReminderInteractionListener listener;

    public void setListener(OnReminderInteractionListener listener) { this.listener = listener; }

    public void submitList(List<ReminderEntity> list) {
        reminders.clear();
        if (list != null) reminders.addAll(list);
        notifyDataSetChanged();
    }

    public ReminderEntity getItem(int position) { return reminders.get(position); }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new ReminderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        holder.bind(reminders.get(position));
    }

    @Override
    public int getItemCount() { return reminders.size(); }

    class ReminderViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox taskCheckBox;
        private final TextView taskStatusText;

        ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);
            taskStatusText = itemView.findViewById(R.id.taskStatusText);
        }

        void bind(ReminderEntity r) {
            String subtitle = r.scheduledTimeEpochMillis == null ? "" :
                    new SimpleDateFormat("EEE, MMM d h:mm a", Locale.getDefault()).format(new Date(r.scheduledTimeEpochMillis));
            String title = r.title + (subtitle.isEmpty() ? "" : (" • " + subtitle));
            taskCheckBox.setText(title);
            taskCheckBox.setChecked(r.isCompleted);
            updateStatus(r);

            taskCheckBox.setOnCheckedChangeListener(null);
            taskCheckBox.setOnCheckedChangeListener((b, checked) -> {
                r.isCompleted = checked;
                updateStatus(r);
                if (listener != null) listener.onCompletionToggled(r);
            });
            itemView.setOnClickListener(v -> { if (listener != null) listener.onItemClicked(r); });
        }

        private void updateStatus(ReminderEntity r) {
            if (r.isCompleted) {
                taskStatusText.setText("✓ Completed");
                taskStatusText.setTextColor(itemView.getContext().getResources().getColor(R.color.success));
            } else {
                taskStatusText.setText("Scheduled");
                taskStatusText.setTextColor(itemView.getContext().getResources().getColor(R.color.text_secondary));
            }
        }
    }
}


