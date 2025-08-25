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

import com.mihir.alzheimerscaregiver.caretaker.data.entity.ReminderEntity;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog;
import android.util.Log;
import java.util.Calendar;
import java.util.Locale;


public class AddMedicationActivity extends AppCompatActivity {

    private EditText medicationNameEditText, dosageEditText, timeEditText, scheduledDateEditText;
    private Button saveButton, cancelButton;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medication);

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
        medicationNameEditText = findViewById(R.id.medicationNameEditText);
        dosageEditText = findViewById(R.id.dosageEditText);
        timeEditText = findViewById(R.id.timeEditText);
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
        String medicationName = medicationNameEditText.getText().toString().trim();
        String dosage = dosageEditText.getText().toString().trim();
        String time = timeEditText.getText().toString().trim();

        // Validate input
        if (TextUtils.isEmpty(medicationName)) {
            medicationNameEditText.setError("Medication name is required");
            medicationNameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(dosage)) {
            dosageEditText.setError("Dosage is required");
            dosageEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(time)) {
            timeEditText.setError("Time is required");
            timeEditText.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        // Parse the scheduled date and time to create proper timestamp
        Long scheduledTime = parseScheduledDateTime();
        if (scheduledTime == null) {
            Toast.makeText(this, "Please select a valid date and time", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Create medication reminder entity (using ReminderEntity for consistency with patient app)
        // Combine medication name and dosage as title, time as description
        String reminderTitle = medicationName + " - " + dosage;
        String reminderDescription = "Take at " + time;
        
        ReminderEntity medicationReminder = new ReminderEntity(
            reminderTitle,
            reminderDescription,
            scheduledTime, // Use the parsed scheduled time
            false // isCompleted
        );
        
        // Add a field to indicate this needs notification scheduling
        // The patient app should check for this field and schedule notifications accordingly

        // Save to Firestore - use 'reminders' collection to match patient app
        // Add a special field to indicate this needs notification scheduling
        db.collection("patients")
                .document(patientId)
                .collection("reminders")
                .add(medicationReminder)
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.firestore.DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.firestore.DocumentReference> task) {
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(AddMedicationActivity.this, 
                                R.string.medication_added_successfully, Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddMedicationActivity.this, 
                                "Failed to add medication: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    
    private void setupTimeAndDatePickers() {
        // Time picker
        timeEditText.setOnClickListener(v -> {
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
                    timeEditText.setText(timeString);
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
        String timeText = timeEditText.getText().toString().trim();
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
