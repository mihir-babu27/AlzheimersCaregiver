package com.mihir.alzheimerscaregiver.caretaker;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.mihir.alzheimerscaregiver.caretaker.data.entity.TaskEntity;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.util.Log;
import java.util.Calendar;
import java.util.Locale;


public class AddTaskActivity extends AppCompatActivity {

    private EditText taskNameEditText, taskDescriptionEditText, dueTimeEditText, scheduledDateEditText;
    private Button saveButton, cancelButton;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Get patient ID from intent
        patientId = getIntent().getStringExtra("patientId");
        if (patientId == null) {
            Toast.makeText(this, "Error: Patient ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        taskNameEditText = findViewById(R.id.taskNameEditText);
        taskDescriptionEditText = findViewById(R.id.taskDescriptionEditText);
        dueTimeEditText = findViewById(R.id.dueTimeEditText);
        scheduledDateEditText = findViewById(R.id.scheduledDateEditText);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        progressBar = findViewById(R.id.progressBar);

        // Set click listeners
        saveButton.setOnClickListener(v -> attemptSave());
        cancelButton.setOnClickListener(v -> finish());
        
        // Set up time and date pickers
        setupTimeAndDatePickers();
    }

    private void attemptSave() {
        String taskName = taskNameEditText.getText().toString().trim();
        String taskDescription = taskDescriptionEditText.getText().toString().trim();
        String dueTime = dueTimeEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(taskName)) {
            taskNameEditText.setError("Task name is required");
            taskNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(taskDescription)) {
            taskDescriptionEditText.setError("Task description is required");
            taskDescriptionEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(dueTime)) {
            dueTimeEditText.setError("Due time is required");
            dueTimeEditText.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(true);

        // Parse the scheduled date and time to create proper timestamp
        Long scheduledTime = parseScheduledDateTime();
        if (scheduledTime == null) {
            Toast.makeText(this, "Please select a valid date and time", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create task entity
        TaskEntity task = new TaskEntity(
            taskName,
            taskDescription,
            false, // isCompleted
            "General", // category
            scheduledTime, // Use the parsed scheduled time
            false, // isRecurring
            null // recurrenceRule
        );
        
        // Add a field to indicate this needs notification scheduling
        // The patient app should check for this field and schedule notifications accordingly

        // Save to Firestore
        // Add a special field to indicate this needs notification scheduling
        db.collection("patients")
                .document(patientId)
                .collection("tasks")
                .add(task)
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.firestore.DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.firestore.DocumentReference> task) {
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(AddTaskActivity.this, 
                                R.string.task_added_successfully, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddTaskActivity.this, 
                                "Failed to add task: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    
    private void setupTimeAndDatePickers() {
        // Time picker
        dueTimeEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minuteOfDay) -> {
                    String timeString = String.format(Locale.getDefault(), "%02d:%02d %s", 
                        hourOfDay > 12 ? hourOfDay - 12 : hourOfDay,
                        minuteOfDay,
                        hourOfDay >= 12 ? "PM" : "AM");
                    dueTimeEditText.setText(timeString);
                },
                hour, minute, false);
            timePickerDialog.show();
        });
        
        // Date picker
        scheduledDateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String dateString = String.format(Locale.getDefault(), "%d-%02d-%02d", 
                        selectedYear, selectedMonth + 1, selectedDay);
                    scheduledDateEditText.setText(dateString);
                },
                year, month, day);
            datePickerDialog.show();
        });
    }
    
    private Long parseScheduledDateTime() {
        String timeText = dueTimeEditText.getText().toString().trim();
        String dateText = scheduledDateEditText.getText().toString().trim();
        
        if (timeText.isEmpty() || dateText.isEmpty()) {
            return null;
        }
        
        try {
            // Parse date (format: YYYY-MM-DD)
            String[] dateParts = dateText.split("-");
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1; // Calendar months are 0-based
            int day = Integer.parseInt(dateParts[2]);
            
            // Parse time (format: HH:MM AM/PM)
            String[] timeParts = timeText.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1].split(" ")[0]);
            boolean isPM = timeParts[1].contains("PM");
            
            if (isPM && hour != 12) hour += 12;
            if (!isPM && hour == 12) hour = 0;
            
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, hour, minute, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            return calendar.getTimeInMillis();
        } catch (Exception e) {
            return null;
        }
    }
}
