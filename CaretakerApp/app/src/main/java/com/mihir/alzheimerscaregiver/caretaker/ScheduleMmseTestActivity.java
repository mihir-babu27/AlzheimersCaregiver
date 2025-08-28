package com.mihir.alzheimerscaregiver.caretaker;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mihir.alzheimerscaregiver.caretaker.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScheduleMmseTestActivity extends AppCompatActivity {
    private TextView dateText, timeText;
    private Button pickDateButton, pickTimeButton, scheduleButton;
    private ProgressBar progressBar;
    private String patientId;
    private Calendar selectedDateTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_mmse_test);

        dateText = findViewById(R.id.dateText);
        timeText = findViewById(R.id.timeText);
        pickDateButton = findViewById(R.id.pickDateButton);
        pickTimeButton = findViewById(R.id.pickTimeButton);
        scheduleButton = findViewById(R.id.scheduleButton);
        progressBar = findViewById(R.id.progressBar);

        patientId = getIntent().getStringExtra("patientId");
        selectedDateTime = Calendar.getInstance();

        pickDateButton.setOnClickListener(v -> showDatePicker());
        pickTimeButton.setOnClickListener(v -> showTimePicker());
        scheduleButton.setOnClickListener(v -> scheduleTest());
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDateTime.set(Calendar.YEAR, year);
            selectedDateTime.set(Calendar.MONTH, month);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            dateText.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute);
            selectedDateTime.set(Calendar.SECOND, 0);
            timeText.setText(String.format("%02d:%02d", hourOfDay, minute));
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
    }

    private void scheduleTest() {
        if (patientId == null) {
            Toast.makeText(this, "No patient selected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dateText.getText().toString().isEmpty() || timeText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        String scheduleId = UUID.randomUUID().toString();
        String caretakerId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        Map<String, Object> data = new HashMap<>();
        data.put("scheduledBy", caretakerId);
        data.put("scheduledFor", patientId);
        data.put("datetime", new Timestamp(selectedDateTime.getTime()));
        data.put("status", "pending");
        FirebaseFirestore.getInstance()
                .collection("patients").document(patientId)
                .collection("mmse_schedule").document(scheduleId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "MMSE Test scheduled!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to schedule: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
