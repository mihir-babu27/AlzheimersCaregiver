package com.mihir.alzheimerscaregiver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mihir.alzheimerscaregiver.data.entity.ReminderEntity;
import com.mihir.alzheimerscaregiver.ui.reminders.ReminderEntityAdapter;
import com.mihir.alzheimerscaregiver.ui.viewmodel.ReminderViewModel;
import com.mihir.alzheimerscaregiver.notifications.ReminderScheduler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RemindersActivity extends AppCompatActivity implements ReminderEntityAdapter.OnReminderInteractionListener {

    private ImageButton backButton;
    private RecyclerView remindersRecyclerView;
    private FloatingActionButton addReminderFab;

    private ReminderEntityAdapter adapter;
    private ReminderViewModel viewModel;

    public static final String EXTRA_MEDICATION_MODE = "EXTRA_MEDICATION_MODE";
    private boolean medicationMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);

        backButton = findViewById(R.id.backButton);
        remindersRecyclerView = findViewById(R.id.remindersRecyclerView);
        addReminderFab = findViewById(R.id.addReminderFab);

        remindersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReminderEntityAdapter();
        adapter.setListener(this);
        remindersRecyclerView.setAdapter(adapter);

        medicationMode = getIntent().getBooleanExtra(EXTRA_MEDICATION_MODE, false);

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                onItemSwipedToDelete(adapter.getItem(position), position);
            }
        });
        helper.attachToRecyclerView(remindersRecyclerView);

        viewModel = new ViewModelProvider(this).get(ReminderViewModel.class);
        viewModel.getAllReminders().observe(this, reminders -> {
            if (!medicationMode) {
                adapter.submitList(reminders);
            } else {
                java.util.List<com.mihir.alzheimerscaregiver.data.entity.ReminderEntity> meds = new java.util.ArrayList<>();
                if (reminders != null) {
                    for (com.mihir.alzheimerscaregiver.data.entity.ReminderEntity r : reminders) {
                        if (r.priority >= 2) { // Treat High priority as medication
                            meds.add(r);
                        }
                    }
                }
                adapter.submitList(meds);
            }
        });

        backButton.setOnClickListener(v -> finish());
        addReminderFab.setOnClickListener(v -> showAddOrEditDialog(null));
    }

    @Override
    public void onCompletionToggled(ReminderEntity reminder) {
        viewModel.markCompleted(reminder.id, reminder.isCompleted);
    }

    @Override
    public void onItemClicked(ReminderEntity reminder) {
        showAddOrEditDialog(reminder);
    }

    @Override
    public void onItemSwipedToDelete(ReminderEntity reminder, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete reminder")
                .setMessage("Are you sure you want to delete this reminder?")
                .setPositiveButton("Delete", (d, w) -> viewModel.delete(reminder))
                .setNegativeButton("Cancel", (d, w) -> adapter.notifyItemChanged(position))
                .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                .show();
    }

    private void showAddOrEditDialog(ReminderEntity existing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_reminder, null, false);
        EditText inputTitle = view.findViewById(R.id.inputTitle);
        EditText inputDescription = view.findViewById(R.id.inputDescription);
        EditText inputDateTime = view.findViewById(R.id.inputDateTime);
        Spinner spinnerPriority = view.findViewById(R.id.spinnerPriority);
        CheckBox checkCompleted = view.findViewById(R.id.checkCompleted);

        final Long[] scheduledAt = {null};

        // Occurrence for tasks, or before/after food for meds using same spinner
        if (medicationMode) {
            spinnerPriority.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Before Food", "After Food"}));
            spinnerPriority.setSelection(0);
            inputTitle.setHint("Medicine (e.g., Aspirin)");
            inputDescription.setHint("Dosage (e.g., 1 tablet)");
        } else {
            spinnerPriority.setAdapter(new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Once", "Daily", "Weekly"}));
            spinnerPriority.setSelection(1);
            inputTitle.setHint("Task");
            inputDescription.setHint("Notes (optional)");
        }
        if (existing != null) {
            inputTitle.setText(existing.title);
            inputDescription.setText(existing.description);
            checkCompleted.setChecked(existing.isCompleted);
            if (existing.scheduledTimeEpochMillis != null) {
                scheduledAt[0] = existing.scheduledTimeEpochMillis;
                inputDateTime.setText(new SimpleDateFormat("EEE, MMM d h:mm a", Locale.getDefault())
                        .format(new Date(existing.scheduledTimeEpochMillis)));
            }
            spinnerPriority.setSelection(Math.max(0, Math.min(2, existing.priority)));
        }

        inputDateTime.setOnClickListener(v -> pickDateTime(scheduledAt, inputDateTime));

        new AlertDialog.Builder(this)
                .setTitle(existing == null ? "Add reminder" : "Edit reminder")
                .setView(view)
                .setPositiveButton(existing == null ? "Add" : "Save", (dialog, which) -> {
                    String title = inputTitle.getText().toString().trim();
                    if (TextUtils.isEmpty(title)) { toast("Title required"); return; }
                    String desc = emptyToNull(inputDescription.getText().toString().trim());
                    int priority = spinnerPriority.getSelectedItemPosition();
                    boolean completed = checkCompleted.isChecked();

                    if (existing == null) {
                        ReminderEntity entity = new ReminderEntity(title, desc, scheduledAt[0], completed, priority);
                        viewModel.insert(entity);
                        if (!completed && scheduledAt[0] != null) { ReminderScheduler.schedule(this, scheduledAt[0], title, desc); }
                    } else {
                        existing.title = title;
                        existing.description = desc;
                        existing.scheduledTimeEpochMillis = scheduledAt[0];
                        existing.isCompleted = completed;
                        existing.priority = priority;
                        viewModel.update(existing);
                        if (!completed && scheduledAt[0] != null) { ReminderScheduler.schedule(this, scheduledAt[0], title, desc); }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void pickDateTime(Long[] scheduledAt, EditText input) {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(Calendar.YEAR, year);
            selected.set(Calendar.MONTH, month);
            selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            TimePickerDialog timePicker = new TimePickerDialog(this, (timeView, hour, minute) -> {
                selected.set(Calendar.HOUR_OF_DAY, hour);
                selected.set(Calendar.MINUTE, minute);
                selected.set(Calendar.SECOND, 0);
                selected.set(Calendar.MILLISECOND, 0);
                scheduledAt[0] = selected.getTimeInMillis();
                input.setText(new SimpleDateFormat("EEE, MMM d h:mm a", Locale.getDefault()).format(selected.getTime()));
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false);
            timePicker.show();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private String emptyToNull(String s) { return TextUtils.isEmpty(s) ? null : s; }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
}


